import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject, Observable, of, zip, OperatorFunction, timer, pipe } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every, switchMap } from 'rxjs/operators';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { ProgramItem, ProgramPerson, ProgramFilter, Room } from '@app/_models';
import { SettingsService } from './settings.service';
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

const tzoffset: number = new Date().getTimezoneOffset();

let hour12: boolean = false;
let hour12ConstSet$!: Observable<boolean>;

export class ScheduleEvent {
  constructor (item: ProgramItem) {
    this.id = item.id;
    this.title = item.title;
    this.description = item.desc;
    this.tags = item.tags || [];
    this.start = new Date(item.timestamp);
    this.mins = parseInt(item.mins, 10);
    this.location = item.loc;
    this.people = [];
    this.tempPeople = item.people;
  }

  link(peopleMap: {[_: string]: SchedulePerson}): void {
    if (this.tempPeople) {
      this.people = this.tempPeople.
        map(({id, name}) => ({person: peopleMap[id], isModerator: name.endsWith('(moderator)')})).
        filter(({person}) => person);
    }
    this.tempPeople = undefined;
  }

  getDateString(): string {
    if (!this.dateString) {
      this.dateString = this.start.toLocaleDateString(undefined, {'weekday': 'long', month: 'long', day: 'numeric', year: 'numeric'});
    }
    return this.dateString;
  }

  getTimeString(): string {
    const localTime = this.start.toLocaleTimeString(undefined, {hour:'numeric', minute: 'numeric', hour12: hour12});
    // Check only works when outside daylight savings time. We could
    // change to always generate the EST time and compare the
    // strings instead, but that would be slower.
    if (tzoffset === 5 * 60) {
      return localTime;
    } else {
      const estTime = this.start.toLocaleTimeString(undefined, {hour: 'numeric', minute: 'numeric', hour12: hour12, timeZone: 'EST'});
      return `${localTime} (${estTime} EST)`;
    }
  }

  id: string;
  title: string;
  description: string;
  tags: string[];
  start: Date;
  mins: number;
  location: string[];
  people: {person: SchedulePerson, isModerator: boolean}[];

  private dateString?: string;

  private tempPeople?: {id: string, name: string}[];
}

export interface EventsAtTime {
  timeString: string,
  events: ScheduleEvent[],
}
export interface DayOfEvents {
  dateString: string,
  times: EventsAtTime[],
}

export type StructuredEvents = DayOfEvents[];

function buildStructuredEvents(sortedEvents: ScheduleEvent[]): StructuredEvents {
  const result: StructuredEvents = [];
  let endOfDay: Date | undefined = undefined;
  let currentTime: number | undefined = undefined;
  let currentDayOfEvents: DayOfEvents | undefined = undefined;
  let currentEventsAtTime: EventsAtTime | undefined = undefined;

  sortedEvents.forEach((event) => {
    if (!endOfDay || !currentDayOfEvents || event.start >= endOfDay) {
      endOfDay = new Date(event.start.getFullYear(), event.start.getMonth(), event.start.getDate() + 1);
      currentDayOfEvents = {
        dateString: event.getDateString(),
        times: []
      };
      result.push(currentDayOfEvents);
    }
    if (!currentTime || !currentEventsAtTime || currentTime != event.start.getTime()) {
      currentTime = event.start.getTime();
      currentEventsAtTime = {
        timeString: '',
        events: []
      };
      currentDayOfEvents.times.push(currentEventsAtTime);
    }
    currentEventsAtTime.events.push(event);
  });
  return result;
}

function relabelStructuredEvents(events: StructuredEvents) {
  events.forEach(dayOfEvents => dayOfEvents.times.forEach(eventsAtTime => eventsAtTime.timeString = eventsAtTime.events[0].getTimeString()));
}

function relabelStructuredEventsAsNeeded() {
  return pipe(
    switchMap((structuredEvents: StructuredEvents) => {
      return hour12ConstSet$.pipe(
        map(_ => {
          relabelStructuredEvents(structuredEvents);
          return structuredEvents;
        }),
      );
    }),
  );
}

export class SchedulePerson {
  constructor (person: ProgramPerson) {
    this.id = person.id;
    this.name = Array.isArray(person.name) ? person.name.join(' ') : person.name;
    this.tags = person.tags || [];
    this.bio = person.bio;
    this.tempProg = person.prog;
  }

  link(eventMap: {[_: string]: ScheduleEvent}): void {
    if (this.tempProg) {
      this.events_ = this.tempProg.
        map(id => eventMap[id]).
        filter(event => event).
        sort((a, b) => a.start.getTime() - b.start.getTime());
    }
  }

  get events$() {
    if (!this.structuredEvents$) {
      const structuredEvents = buildStructuredEvents(this.events_);
      const structuredEvents$ = new BehaviorSubject<StructuredEvents>(structuredEvents);
      this.structuredEvents$ = structuredEvents$.pipe(
        relabelStructuredEventsAsNeeded(),
      );
    }
    return this.structuredEvents$;
  }
  id: string;
  name: string;
  tags: string[];
  links?: object;
  bio?: string;
  private structuredEvents$?: Observable<StructuredEvents>;
  private events_: ScheduleEvent[] = [];
  private tempProg?: string[];
}

interface ProgramData {
  program: ProgramItem[],
  people: ProgramPerson[]
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

  private events: ScheduleEvent[] = [];
  events$ = new ReplaySubject<ScheduleEvent[]>(1);

  private people: SchedulePerson[] = [];
  people$ = new ReplaySubject<SchedulePerson[]>(1);

  private peopleMap: {[id: string]: SchedulePerson} = {};
  peopleMap$ = new ReplaySubject<{[id: string]: SchedulePerson}>(1);

  private locations: string[] = [];
  locations$ = new ReplaySubject<string[]>(1);

  private schedule: StructuredEvents = [];
  private scheduleWithoutRelabeling$ = new ReplaySubject<StructuredEvents>(1);
  private schedule$: Observable<StructuredEvents>;

  private status: ScheduleStatus = {state: ScheduleState.IDLE};
  status$ = new BehaviorSubject<ScheduleStatus>(this.status);

  constructor(private http: HttpClient, private settingsService: SettingsService) {
    this.init();
    hour12ConstSet$ = settingsService.hour12$.pipe(tap(value => hour12 = value));
    hour12ConstSet$.subscribe();

    this.schedule$ = this.scheduleWithoutRelabeling$.pipe(relabelStructuredEventsAsNeeded());
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
    this.http.get<ProgramData>
      (`${environment.backend}/schedule`,
       {observe: 'response',
        headers: this.etag !== undefined ? {'If-None-Match': this.etag} : {}
       }
      ).subscribe({
        next: (response) => this.handleResponse(response),
        error: (error) => this.handleError(error)
      });
  }

  private handleResponse(response: HttpResponse<ProgramData>): void {
    if (response.body === null) {
      this.handleError();
      return;
    }

    const program = response.body;

    this.peopleMap = {};
    const eventMap: {[_: string]: ScheduleEvent} = {};

    this.events = program.program.map((item) => {
      const event = new ScheduleEvent(item);
      eventMap[event.id] = event;
      return event;
    });
    this.events.sort((a, b) => a.start.getTime() - b.start.getTime());

    this.people = program.people.map((item) => {
      const person = new SchedulePerson(item);
      this.peopleMap[person.id] = person;
      return person;
    });
    this.people.sort((a, b) => (a.name.localeCompare(b.name)));

    const locations: {[_:string]: string} = {};

    this.events.forEach((event) => {
      event.link(this.peopleMap);
      event.location.forEach(location => locations[location] = location);
    });

    this.people.forEach((person) => {
      person.link(eventMap);
    });

    this.locations = Object.values(locations);
    this.locations.sort();

    this.schedule = buildStructuredEvents(this.events);

    this.events$.next(this.events);
    this.people$.next(this.people);
    this.peopleMap$.next(this.peopleMap);
    this.locations$.next(this.locations);
    this.scheduleWithoutRelabeling$.next(this.schedule);

    this.status.state = ScheduleState.READY;
    this.status.lastUpdate = new Date();
    this.status.error = undefined;
    this.status$.next(this.status);

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
    timer(0, RELOAD_TIMER).subscribe(() => this.reload());
    this.inited = true;
  }

  getSchedule(filters?: ProgramFilter): Observable<StructuredEvents> {
    if (filters === undefined) {
      return this.schedule$;
    }

    let dateRanges: {start: Date, end: Date}[] | undefined;

    const munged_filters: ((scheduleEvent: ScheduleEvent) => boolean)[] = [];
    if (filters.tags && filters.tags.length > 0) {
      const tags = filters.tags;
      munged_filters.push(scheduleEvent => tags.some(filterString => scheduleEvent.tags.includes(filterString)))
    }
    if (filters.loc && filters.loc.length > 0) {
      const loc = filters.loc;
      munged_filters.push(scheduleEvent => loc.some(filterString => scheduleEvent.location.includes(filterString)))
    }
    if (filters.id && filters.id.length > 0) {
      const id = filters.id;
      munged_filters.push(scheduleEvent => id.some(filterString => scheduleEvent.location.includes(filterString)))
    }

    if (filters.date && filters.date.length > 0) {
      dateRanges = [...filters.date].
        sort((a, b) => a.start.getTime()-b.start.getTime());
    }

    if (munged_filters.length === 0 && (!dateRanges || dateRanges.length === 0)) {
      return this.schedule$;
    }

    return this.events$.pipe(
      map(events => {
        let dateFilteredEvents = events;
        if (dateRanges) {
          let i = 0;
          const eventCount = events.length;
          dateFilteredEvents = [];
          dateRanges.forEach(dateRange => {
            for (; i < eventCount && events[i].start < dateRange.start; i++) {}
            for (; i < eventCount && events[i].start < dateRange.end; i++) {
              dateFilteredEvents.push(events[i]);
            }
          });
        }
        return buildStructuredEvents(dateFilteredEvents.filter((event) => munged_filters.every(filter => filter(event))));
      }),
      relabelStructuredEventsAsNeeded(),
    );
  }

  getPerson(id:string): Observable<SchedulePerson | undefined> {
    return this.peopleMap$.pipe(
      map(peopleMap => peopleMap[id]),
    );
  }

  get_featured_events(): Observable<StructuredEvents> {
    // for testing:
    // return this.getSchedule({id: ['23', '45', '17']});
    return this.getSchedule({tags: ['featured']});
  }

  get_rooms(): Observable<Room[]> {
    // Fake data
    const currentEvent = {
      id: 'a',
      title: 'Panel XYZ',
      people: [{id: "1", name: 'Panelist A'}, {id: "2", name: 'Panelist B'}, {id: "3", name: 'Panelist C'}, {id: "4", name: 'Panelist D'}, {id: "5", name: 'Panelist E'}],
      desc: 'This is the panel description. It would be longer and more interesting in real life, and hopefully get you excited about the topic being discussed! I’m going to write a couple more sentences so we get a more realistic sense of how much space this might take up. The quick brown fox jumps over the lazy dog. Bowties are cool. Live long and prosper. May the Force be with you.',
      tags: [],
      date: '',
      time: '',
      timestamp: '',
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
