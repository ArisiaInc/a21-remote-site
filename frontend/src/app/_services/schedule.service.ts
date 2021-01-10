import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject, Observable, of, zip, OperatorFunction, timer, pipe, defer, concat } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every, switchMap, repeat, ignoreElements, shareReplay } from 'rxjs/operators';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Initial, createInitials, searchPrefixCaseInsensitive } from '@app/_helpers/utils';
import { ProgramItem, ProgramPerson, ProgramFilter, DateRange } from '@app/_models';
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

export interface GamingMeta {
  id: string;
  loc: string[];
}

/** Takes a set of dateRanges sorted by start time. */
function filterEventsByDate(events: ScheduleEvent[], dateRanges: DateRange[], count?: number): ScheduleEvent[] {
  if (count === 0) {
    return [];
  }
  let i = 0;
  const eventCount = events.length;
  let filteredEvents: ScheduleEvent[] = [];
  for (const dateRange of dateRanges) {
    for (; i < eventCount && (!count || filteredEvents.length < count) && events[i].start < dateRange.start; i++) {
      if (dateRange.inclusive && events[i].start.getTime() + events[i].mins * 60 * 1000 >= dateRange.start.getTime()) {
        filteredEvents.push(events[i]);
      }
    }
    if (!dateRange.end) {
      filteredEvents = filteredEvents.concat(events.slice(i, count ? i + count - filteredEvents.length : undefined));
      break;
    }
    for (; i < eventCount && (!count || filteredEvents.length < count) && events[i].start < dateRange.end; i++) {
      filteredEvents.push(events[i]);
    }
  }
  return filteredEvents;
}

export interface RunningEvents {
  current?: ScheduleEvent;
  next?: ScheduleEvent;
}

export class Room {
  runningEvents$: Observable<RunningEvents>
  constructor(public id: string,
              public name: string,
              public art: string,
              private scheduleService: ScheduleService) {
    this.runningEvents$ = scheduleService.getNextEvents(name);
  }
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

  peopleInitials$: Observable<Initial[]>;

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

    this.peopleInitials$ = this.people$.pipe(
      createInitials(),
      shareReplay(1),
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
    this.eventsMap = {};

    this.events = program.program.map(item => new ScheduleEvent(item, this.starsService));
    this.events.forEach(event => this.eventsMap[event.id] = event);
    this.events.sort((a, b) => a.start.getTime() - b.start.getTime());

    this.people = program.people.map((item) => new SchedulePerson(item));
    this.people.sort(SchedulePerson.compare);
    this.people.forEach(person => {
      this.peopleMap[person.id] = person;
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
        const dateFilteredEvents = dateRanges ? filterEventsByDate(events, dateRanges) : events;
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
      return this.people$.pipe(
        searchPrefixCaseInsensitive(search),
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

  getNextEvents(roomName: string): Observable<RunningEvents> {
    let currentTime = this.settingsService.currentTime;
    return this.events$.pipe(
      map(events => events.filter(event => event.location.includes(roomName))),
      switchMap(events =>
        /* Calculate next two events based on current time. */
        defer(() => of(filterEventsByDate(events, [{start: (currentTime = this.settingsService.currentTime), inclusive: true}], 2))).pipe(
          /* Map to our RunningEvents structure. */
          map(events => {
            if (events.length == 0) {
              return {};
            }
            if (events[0].start > this.settingsService.currentTime) {
              return {next: events[0]};
            } else {
              return {current: events[0], next: events[1]};
            }
          }),
          /* emit runningEvents, and then calculate the time (based on the current time) until the next change and wait that long (but don't emit the timer event) */
          mergeMap(runningEvents => concat(of(runningEvents), defer(() => {
            const nextStartTime = runningEvents.next ? runningEvents.next.start.getTime() - currentTime.getTime() : Infinity;
            const nextEndTime = runningEvents.current ? runningEvents.current.start.getTime() + runningEvents.current.mins * 60 * 1000 - currentTime.getTime() : Infinity;
            const delayTime = Math.min(nextStartTime, nextEndTime);
            return timer(delayTime).pipe(ignoreElements());
          }))),
          /* Repeat */
          repeat(),
        )),
    );
  }

  get_featured_events(): Observable<StructuredEvents> {
    // for testing:
    // return this.getSchedule({id: ['23', '45', '17']});
    return this.getSchedule({tags: [['featured']]});
  }


  // TODO merge this in with the program data from the db ?
  get_gaming_meta(local: false): Observable<GamingMeta[]> {
    if (local) {
      return this.http.get<GamingMeta[]>('assets/data/gaming_11_1.json');
    }
    return this.http.get<GamingMeta[]>(`${environment.backend}/schedule/gamingmeta`);
  }

  getRooms(): Observable<Room[]> {
    return of([{ id: 'zoom_room_1', name: 'Zoom Room 1', art: 'room-1.png' },
               { id: 'zoom_room_2', name: 'Zoom Room 2', art: 'room-2.png'  },
               { id: 'zoom_room_3', name: 'Zoom Room 3', art: 'room-3.png'  },
               { id: 'zoom_room_4', name: 'Zoom Room 4', art: 'room-4.png'  },
               { id: 'zoom_room_5', name: 'Zoom Room 5', art: 'room-5.png'  },
               { id: 'zoom_room_6', name: 'Zoom Room 6', art: 'room-6.png'  },
               { id: 'zoom_room_7', name: 'Zoom Room 7', art: 'room-7.png'  },
               { id: 'zoom_room_fasttrack', name: 'FastTrack Zoom', art: 'room-8.png'  },
              ].map(({id, name, art}) => new Room(id, name, `/assets/images/programming/${art}`, this)));
  }

  getRoom(id: string): Observable<Room | undefined> {
    return this.getRooms().pipe(
      map(rooms => rooms.find(room => room.id === id)),
    );
  }
}
