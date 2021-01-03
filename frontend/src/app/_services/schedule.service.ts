import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject, Observable, of, zip, OperatorFunction, timer, pipe } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every, switchMap } from 'rxjs/operators';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { ProgramItem, ProgramPerson, ProgramFilter, Room, DateRange } from '@app/_models';
import { SettingsService } from './settings.service';
import { StarsService } from './stars.service';
import { environment } from '@environments/environment';

export enum ScheduleState {
  IDLE,
  LOADING,
  READY,
  ERROR,
}

const TRACK_TAG = 'track:';
const TYPE_TAG = 'type:';

export interface ScheduleStatus {
  state: ScheduleState;
  error?: HttpErrorResponse;
  lastUpdate?: Date;
}

const tzoffset: number = new Date().getTimezoneOffset();

let hour12: boolean = false;
let hour12ConstSet$!: Observable<boolean>;

export class ScheduleEvent {
  constructor (item: ProgramItem, private starsService: StarsService) {
    this.id = item.id;
    this.title = item.title;
    this.description = item.desc;
    this.tags = item.tags || [];
    this.start = new Date(item.timestamp);
    this.mins = parseInt(item.mins, 10);
    this.location = item.loc;
    this.people = [];
    this.tempPeople = item.people;
    this.starCache = this.starsService.has(this.id);
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

  get starred(): boolean {
    return this.starCache;
  }

  set starred(value: boolean) {
    const serviceValue = this.starsService.has(this.id);
    if (value === serviceValue) {
      this.starCache = serviceValue;
    } else if (value) {
      this.starsService.add(this.id);
      this.starCache = value;
    } else {
      this.starsService.delete(this.id);
      this.starCache = value;
    }
  }

  updateStar(): void {
    this.starCache = this.starsService.has(this.id);
  }

  private starCache: boolean;

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

function restarStructuredEvents(events: StructuredEvents) {
  events.forEach(dayOfEvents => dayOfEvents.times.forEach(eventsAtTime => eventsAtTime.events.forEach(event => event.updateStar())));
}

function restarStructuredEventsAsNeeded() {
  return pipe(
    switchMap((structuredEvents: StructuredEvents) => {
      return hour12ConstSet$.pipe(
        map(_ => {
          restarStructuredEvents(structuredEvents);
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

  static compare(a: SchedulePerson, b: SchedulePerson) {
    return a.name.localeCompare(b.name);
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
        restarStructuredEventsAsNeeded(),
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

export interface Initial {
  lower: string;
  upper: string;
  active: boolean;
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

  private eventsMap: {[id: string]: ScheduleEvent} = {};
  eventsMap$ = new ReplaySubject<{[id: string]: ScheduleEvent}>(1);

  private people: SchedulePerson[] = [];
  people$ = new ReplaySubject<SchedulePerson[]>(1);

  private peopleMap: {[id: string]: SchedulePerson} = {};
  peopleMap$ = new ReplaySubject<{[id: string]: SchedulePerson}>(1);

  private peopleInitials: {[lower: string]: Initial} = {};
  peopleInitials$ = new ReplaySubject<{[lower: string]: Initial}>(1);

  private tracks: string[] = [];
  tracks$ = new ReplaySubject<string[]>(1);

  private types: string[] = [];
  types$ = new ReplaySubject<string[]>(1);

  private schedule: StructuredEvents = [];
  private scheduleWithoutRelabeling$ = new ReplaySubject<StructuredEvents>(1);
  private schedule$: Observable<StructuredEvents>;

  private status: ScheduleStatus = {state: ScheduleState.IDLE};
  status$ = new BehaviorSubject<ScheduleStatus>(this.status);

  constructor(private http: HttpClient,
              private settingsService: SettingsService,
              private starsService: StarsService,) {
    this.init();
    hour12ConstSet$ = settingsService.hour12$.pipe(tap(value => hour12 = value));
    hour12ConstSet$.subscribe();

    this.schedule$ = this.scheduleWithoutRelabeling$.pipe(
      relabelStructuredEventsAsNeeded(),
      restarStructuredEventsAsNeeded(),
    );
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
    this.peopleInitials = {};
    this.eventsMap = {};

    for(let letter = 1; letter <= 26; letter++) {
      const lower = String.fromCharCode(letter + 96);
      const upper = String.fromCharCode(letter + 64);
      this.peopleInitials[lower] = {lower, upper, active: false};
    }

    this.events = program.program.map(item => new ScheduleEvent(item, this.starsService));
    this.events.forEach(event => this.eventsMap[event.id] = event);
    this.events.sort((a, b) => a.start.getTime() - b.start.getTime());

    this.people = program.people.map((item) => new SchedulePerson(item));
    this.people.sort(SchedulePerson.compare);
    this.people.forEach(person => {
      this.peopleMap[person.id] = person;
      const lower = person.name[0].toLowerCase();
      if (this.peopleInitials[lower]) {
        this.peopleInitials[lower].active = true;
      } else {
        const upper = person.name[0].toUpperCase();
        this.peopleInitials[lower] = {lower, upper, active: true};
      }
    });

    const tracks = new Set<string>();
    const types = new Set<string>();

    this.events.forEach((event) => {
      event.link(this.peopleMap);
      event.tags.forEach(tag => {
        if (tag.startsWith(TRACK_TAG)) {
          tracks.add(tag.substring(TRACK_TAG.length));
        }
        if (tag.startsWith(TYPE_TAG)) {
          types.add(tag.substring(TYPE_TAG.length));
        }
      });
    });

    this.people.forEach((person) => {
      person.link(this.eventsMap);
    });

    this.tracks = [...tracks];
    this.tracks.sort();

    this.types = [...types];
    this.types.sort();

    this.schedule = buildStructuredEvents(this.events);

    this.events$.next(this.events);
    this.eventsMap$.next(this.eventsMap);
    this.people$.next(this.people);
    this.peopleMap$.next(this.peopleMap);
    this.peopleInitials$.next(this.peopleInitials);
    this.tracks$.next(this.tracks);
    this.types$.next(this.types);
    this.scheduleWithoutRelabeling$.next(this.schedule);

    this.status.state = ScheduleState.READY;
    this.status.lastUpdate = this.settingsService.currentTime;
    this.status.error = undefined;
    this.status$.next(this.status);

    this.etag = response.headers.get('etag') || undefined;
    this.loading = false;
  }

  private handleError(error?: HttpErrorResponse): void {
    if (error && !(error.error instanceof ErrorEvent) && error.status === 304) {
      this.status.state = ScheduleState.READY;
      this.status.lastUpdate = this.settingsService.currentTime;
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

    let dateRanges: DateRange[] | undefined;

    const munged_filters: ((scheduleEvent: ScheduleEvent) => boolean)[] = [];
    if (filters.tags && filters.tags.length > 0) {
      const tags = filters.tags;
      munged_filters.push(
        scheduleEvent => tags.every(
          category => category.length === 0 || category.some(
            filterString => scheduleEvent.tags.includes(filterString))))
    }
    if (filters.loc && filters.loc.length > 0) {
      const loc = filters.loc;
      munged_filters.push(
        scheduleEvent => loc.some(
          filterString => scheduleEvent.location.includes(filterString)))
    }

    if (filters.date && filters.date.length > 0) {
      dateRanges = [...filters.date].
        sort((a, b) => a.start.getTime()-b.start.getTime());
    }

    if (munged_filters.length === 0 && (!dateRanges || dateRanges.length === 0) && (!filters.id || filters.id.length === 0)) {
      return this.schedule$;
    }

    let eventsObservable;

    if (filters.id && filters.id.length > 0) {
      const id = filters.id;
      eventsObservable = this.eventsMap$.pipe(
        // It's okay to sort in place since filters.id.map() has created a new array.
        map(eventsMap => id.
          map(id => eventsMap[id]).
          filter(event => event).
          sort((a, b) => a.start.getTime()-b.start.getTime())),
      );
    } else {
      eventsObservable = this.events$;
    }

    return eventsObservable.pipe(
      map(events => {
        let dateFilteredEvents = events;
        if (dateRanges) {
          let i = 0;
          const eventCount = events.length;
          dateFilteredEvents = [];
          dateRanges.forEach(dateRange => {
            for (; i < eventCount && events[i].start < dateRange.start; i++) {
              if (dateRange.inclusive && events[i].start.getTime() + events[i].mins * 60 * 1000 >= dateRange.start.getTime()) {
                dateFilteredEvents.push(events[i]);
              }
            }
            for (; i < eventCount && events[i].start < dateRange.end; i++) {
              dateFilteredEvents.push(events[i]);
            }
          });
        }
        return buildStructuredEvents(dateFilteredEvents.filter((event) => munged_filters.every(filter => filter(event))));
      }),
      relabelStructuredEventsAsNeeded(),
      restarStructuredEventsAsNeeded(),
    );
  }

  getPerson(id: string): Observable<SchedulePerson | undefined> {
    return this.peopleMap$.pipe(
      map(peopleMap => peopleMap[id]),
    );
  }

  getPeople(search: string): Observable<SchedulePerson[]> {
    if (search === 'goh') {
      return this.peopleMap$.pipe(
        // Sort in place is okay since map creates a new array.
        map(peopleMap => ['7116', '106213', '112042'].
          map(id => peopleMap[id]).
          filter(person => person).
          sort(SchedulePerson.compare)),
      );
    } else {
      const regexp = new RegExp('^'+search, 'i');
      return this.people$.pipe(
        map(people => people.filter(person => person.name.match(regexp))),
      );
    }
  }

  getStarredEvents(filters?: ProgramFilter): Observable<StructuredEvents> {
    return this.starsService.observable$.pipe(
      switchMap(starsService => {
        const scheduleFilters: ProgramFilter = {id: [...starsService]};
        if (filters) {
          scheduleFilters.loc = filters.loc;
          scheduleFilters.date = filters.date;
          scheduleFilters.tags = filters.tags
        }
        return this.getSchedule(scheduleFilters)
      }),
    );
  }

  get_featured_events(): Observable<StructuredEvents> {
    // for testing:
    // return this.getSchedule({id: ['23', '45', '17']});
    return this.getSchedule({tags: [['featured']]});
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
