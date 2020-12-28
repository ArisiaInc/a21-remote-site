import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject, Observable, of, zip, OperatorFunction, timer } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every, switchMap } from 'rxjs/operators';
import { ProgramItem, ProgramPerson, ProgramFilter, Room } from '@app/_models';

import { program, people } from "test_data/konopas"
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { environment } from '@environments/environment';

export enum ScheduleState {
  IDLE,
  LOADING,
  READY,
  ERROR,
}

export interface ScheduleStatus {
  state: ScheduleState;
  error?: HttpErrorResponse;
  lastUpdate?: Date;
}

export interface ScheduleData {
  program: ProgramItem[],
  people: ProgramPerson[]
}

export interface StructuredScheduleItems {
  [date: string]: {[time: string]: ProgramItem[]}
}

const RELOAD_TIMER = 10 * 1000;
const USE_FAKE_DATA = false;

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  private etag?: string;
  private loading = false;
  private inited = false;

  private schedule?: ScheduleData;
  schedule$ = new ReplaySubject<ScheduleData>(1);

  private status: ScheduleStatus = {state: ScheduleState.IDLE};
  status$ = new BehaviorSubject<ScheduleStatus>(this.status);

  constructor(private http: HttpClient) {
    this.init();
  }

  private reload(): void {
    if (this.loading) {
      // Notice if the update has been going for a long time.
      return;
    }
    this.loading = true;
    if (this.status.state === ScheduleState.IDLE) {
      this.status.state = ScheduleState.LOADING;
      this.status$.next(this.status);
    }
    this.http.get<ScheduleData>
      (`${environment.backend}/schedule`,
       {observe: 'response',
        headers: this.etag !== undefined ? {ETag: this.etag} : {}
       }
      ).subscribe({
        next: (response) => this.handleResponse(response),
        error: (error) => this.handleError(error)
      });
  }

  private handleResponse(response: HttpResponse<ScheduleData>): void {
    if (response.body === null) {
      this.handleError();
      return;
    }

    const schedule = response.body;

    this.status.state = ScheduleState.READY;
    this.status.lastUpdate = new Date();
    this.status.error = undefined;
    this.status$.next(this.status);

    this.schedule = schedule;
    this.schedule$.next(this.schedule);
    this.etag = response.headers.get('etag') || undefined;
    this.loading = false;
  }

  private handleError(error?: HttpErrorResponse): void {
    if (error && !(error.error instanceof ErrorEvent) && error.status === 304) {
      this.status.state = ScheduleState.READY;
      this.status.lastUpdate = new Date();
      this.status.error = undefined;
      this.status$.next(this.status);
    } else {
      this.status.state = ScheduleState.ERROR;
      this.status.error = error;
      this.status$.next(this.status);
    }
    this.loading = false;
  }

  init() {
    if (this.inited) {
      return;
    }
    if (USE_FAKE_DATA) {
      this.status.state = ScheduleState.READY;
      this.status$.next(this.status);
      this.schedule = {program: (program as ProgramItem[]), people: (people as ProgramPerson[])};
      this.schedule$.next(this.schedule);
    } else {
      timer(0, RELOAD_TIMER).subscribe(() => this.reload());
    }
    this.inited = true;
  }

  get_schedule(filters: ProgramFilter = {}): Observable<StructuredScheduleItems> {
    const munged_filters: ((programItem: ProgramItem) => boolean)[] = [];
    if (filters.tags && filters.tags.length > 0) {
      const tags = filters.tags;
      munged_filters.push(programItem => tags.some(filterString => programItem.tags.includes(filterString)))
    }
    if (filters.loc && filters.loc.length > 0) {
      const loc = filters.loc;
      munged_filters.push(programItem => loc.some(filterString => programItem.loc.includes(filterString)))
    }
    if (filters.date && filters.date.length > 0) {
      const date = filters.date;
      munged_filters.push(programItem => date.some(filterString => programItem.date.includes(filterString)))
    }
    if (filters.id && filters.id.length > 0) {
      const id = filters.id;
      munged_filters.push(programItem => id.some(filterString => programItem.id.includes(filterString)))
    }
    return this.schedule$.pipe(
      pluck('program'),
      map(items => {
        const structured: StructuredScheduleItems = {};
        items.forEach((item) => {
          if (munged_filters.every(filter => filter(item))) {
            structured[item.date] = structured[item.date] || {};
            structured[item.date][item.time] = structured[item.date][item.time] || [];
            structured[item.date][item.time].push(item);
          }
        });
        return structured;
      }),
    );
  }

  get_people(): Observable<ProgramPerson[]> {
    return this.schedule$.pipe(
      pluck('people'),
    );
  }

  get_person(id:string): Observable<ProgramPerson | undefined> {
    return this.get_people().pipe(
      map(people => people.find(p => p.id === id)),
    );
  }

  private get_items_for_person(person?: ProgramPerson): Observable<[ProgramPerson | undefined, StructuredScheduleItems | undefined]> {
    if (person) {
      return this.get_schedule({id: person.prog}).pipe(
        map(items => [person, items])
      );
    } else {
      return of([undefined, undefined]);
    }
  }

  get_person_with_items(id: string): Observable<ProgramPerson | undefined> {
    return this.get_person(id).pipe(
      switchMap(person => this.get_items_for_person(person)),
      map(([person, items]) => { if (person) { person.items = items; } return person; }),
    );
  }

  get_featured_events(): Observable<ProgramItem[]> {
    // for testing:
    return of(['13','16','29']).pipe(
    //return this.http.get<String[]>(`${environment.backend}/schedule/featured`).pipe(
      flatMap(ids => this.schedule$.pipe(
        pluck('program'),
        flatMap(x => of(...x)),
        filter(p => ids.includes(p.id)),
        toArray()
      )),
    );
  }

  get_rooms(): Observable<Room[]> {
    // Fake data
    const currentEvent = {
      id: 'a',
      title: 'Panel XYZ',
      people: [{name: 'Panelist A'}, {name: 'Panelist B'}, {name: 'Panelist C'}, {name: 'Panelist D'}, {name: ['Panelist E', 'Other name']}],
      desc: 'This is the panel description. It would be longer and more interesting in real life, and hopefully get you excited about the topic being discussed! I’m going to write a couple more sentences so we get a more realistic sense of how much space this might take up. The quick brown fox jumps over the lazy dog. Bowties are cool. Live long and prosper. May the Force be with you.',
      tags: [],
      date: '',
      time: '',
      mins: '',
      loc: [],
    };
    return of([new Room('room_1', 'Room 1', currentEvent),
               new Room('room_2', 'Room 2', currentEvent),
               new Room('room_3', 'Room 3', currentEvent),
               new Room('room_4', 'Room 4', currentEvent),
               new Room('room_5', 'Room 5', currentEvent)]);
  }

  get_room(id: string): Observable<Room | undefined> {
    return this.get_rooms().pipe(
      map(rooms => rooms.find(room => room.id === id)),
    );
  }
}
