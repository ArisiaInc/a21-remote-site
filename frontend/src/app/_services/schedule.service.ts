import { Injectable } from '@angular/core';
import { Observable, of, zip, OperatorFunction } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every } from 'rxjs/operators';
import { ProgramItem, ProgramPerson, ProgramFilter, Room } from '@app/_models';

import { program, people } from "test_data/konopas"
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';

interface ScheduleData {
  program: ProgramItem[],
  people: ProgramPerson[]
}

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor(private http: HttpClient) { }

  get_data() : Observable<ScheduleData>{

    // real is currently broken, we'll start with fake
    return of<ScheduleData>({program: (program as ProgramItem[]), people: (people as ProgramPerson[])});
    // this is real
    // return this.http.get<ScheduleData>(`${environment.backend}/schedule`);
  }

  get_schedule(filters: ProgramFilter = {}): Observable<[string, string, ProgramItem[]]> {
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
    return this.get_data().pipe(
      pluck('program'),
      flatMap(x => of(...x)),
      filter(p => munged_filters.every(k => k(p))),
      groupBy(p => `${p.date}~${p.time}`),
      mergeMap(group => zip(of(group.key.split('~')[0]), of(group.key.split('~')[1]), group.pipe(toArray()))),
    );
  }

  get_people(): Observable<ProgramPerson[]> {
    return this.get_data().pipe(
      pluck('people'),
    );
  }

  get_person(id:string): Observable<ProgramPerson | undefined> {
    return this.get_people().pipe(
      map(people => people.find(p => p.id === id)),
    );
  }

  get_featured_events(): Observable<ProgramItem[]> {
    // for testing:
    return of(['13','16','29']).pipe(
    //return this.http.get<String[]>(`${environment.backend}/schedule/featured`).pipe(
      flatMap(ids => this.get_data().pipe(
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
