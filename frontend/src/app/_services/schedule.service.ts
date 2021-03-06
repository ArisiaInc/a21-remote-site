import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject, Subject, Observable, of, zip, OperatorFunction, timer, pipe, defer, concat, merge } from 'rxjs';
import { map, groupBy, mergeMap, toArray, filter, tap, flatMap, pluck, every, switchMap, repeat, ignoreElements, shareReplay, take } from 'rxjs/operators';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { PerformanceService } from './performance.service';
import { Initial, createInitials, searchPrefixCaseInsensitive } from '@app/_helpers/utils';
import { ProgramItem, ProgramPerson, ProgramFilter, DateRange, Performance } from '@app/_models';
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

export enum KidsStatus {
  UNKNOWN,
  WELCOME,
  DIRECTED
}

const tzoffset: number = new Date().getTimezoneOffset();

let hour12: boolean = false;
let hour12ConstSet$!: Observable<boolean>;

export class ScheduleEvent {
  constructor (item: ProgramItem, private starsService: StarsService) {
    this.id = item.id;
    this.title = item.title;
    this.description = item.desc;
    for (const tag of item.tags) {
      switch(tag) {
        case 'Featured':
          this.featured = true;
          break;
        case 'Captioned':
          this.captioned = true;
          break;
        default:
          const split = tag.split(':', 2);
          if (split.length == 2) {
            switch(split[0]) {
              case 'kids':
                switch (split[1]) {
                  case 'Welcome':
                    this.kids = KidsStatus.WELCOME;
                    break;
                  case 'Directed':
                    this.kids = KidsStatus.DIRECTED;
                    break;
                }
                break;
              case 'type':
                this.type = split[1];
                break;
              case 'track':
                this.track = split[1];
                break;
            }
          }
      }
    }
    this.start = new Date(item.timestamp);
    this.mins = parseInt(item.mins, 10);
    this.location = item.loc;
    this.people = [];
    this.tempPeople = item.people;
    this.starCache = this.starsService.has(this.id);
    if (item.doorsOpen && item.doorsClose) {
      this.doors = {start: new Date(item.doorsOpen), end: new Date(item.doorsClose)};
    } else {
      this.doors = {start: new Date(this.start.getTime() - 5 * 60 * 1000), end: new Date(this.start.getTime() + this.mins * 60 * 1000)};
    }
  }

  compare(other: ScheduleEvent) {
    const timeDiff = this.start.getTime() - other.start.getTime();
    if (timeDiff) {
      return timeDiff;
    }
    if (this.location.length > 0 && other.location.length > 0) {
      const locationCompare = this.location[0].localeCompare(other.location[0]);
      if (locationCompare) {
        return locationCompare;
      }
    }
    return this.title.localeCompare(other.title);
  }

  featured = false;
  captioned = false;
  kids = KidsStatus.UNKNOWN;
  type = '';
  track = '';

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
  start: Date;
  mins: number;
  location: string[];
  people: {person: SchedulePerson, isModerator: boolean}[];

  doors: DateRange;

  performance?: Performance;

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
        sort((a, b) => a.compare(b));
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
  started?: boolean;
  current?: ScheduleEvent;
  next?: ScheduleEvent;
}

export class Room {
  runningEvents$: Observable<RunningEvents>
  constructor(public id: string,
              public name: string,
              public art: string,
              public doorArt: string,
              private scheduleService: ScheduleService) {
    this.runningEvents$ = scheduleService.getNextEvents(name);
  }
}

const RELOAD_SECONDS = 60;
const RELOAD_TIMER = RELOAD_SECONDS * 1000;
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

  public openDoors$: Observable<Set<string>>;

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

  private update$ = new Subject<1>();

  private status: ScheduleStatus = {state: ScheduleState.IDLE};
  status$ = new BehaviorSubject<ScheduleStatus>(this.status);

  performances?: Performance[];

  constructor(private http: HttpClient,
              private settingsService: SettingsService,
              private starsService: StarsService,
              private performanceService: PerformanceService,) {
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


    performanceService.performances$.subscribe(performances => {
      this.performances = performances;
      this.combinePerformances(true);
    });

    this.openDoors$ = defer((): Observable<Set<string>>  => {
      const currentTime = settingsService.currentTime.getTime();
      const openDoors = new Set<string>();
      let delay = Infinity;
      for (const event of this.events) {
        if (event.doors) {
          const startDelay = event.doors.start.getTime() - currentTime;
          const endDelay = event.doors.end ? event.doors.end.getTime() - currentTime : Infinity;
          if (startDelay <= 0 && endDelay >= 0) {
            openDoors.add(event.id);
          }
          if (startDelay > 0) {
            delay = Math.min(delay, startDelay);
          }
          if (endDelay >= 0) {
            delay = Math.min(delay, endDelay);
          }
        }
      }
      if (delay === Infinity) {
        return concat(of(openDoors), ignoreElements()(this.update$.pipe(take(1))));
      } else {
        return concat(of(openDoors), ignoreElements()(merge(this.update$, timer(delay)).pipe(take(1))));
      }
    }).pipe(
      repeat(),
      shareReplay(1)
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
    this.events.sort((a, b) => a.compare(b));

    this.people = program.people.map((item) => new SchedulePerson(item));
    this.people.sort(SchedulePerson.compare);
    this.people.forEach(person => {
      this.peopleMap[person.id] = person;
    });

    const tracks = new Set<string>();
    const types = new Set<string>();

    this.events.forEach((event) => {
      event.link(this.peopleMap);
      if (event.track) {
        tracks.add(event.track);
      }
      if (event.type) {
        types.add(event.type);
      }
    });

    this.people.forEach((person) => {
      person.link(this.eventsMap);
    });

    this.tracks = [...tracks];
    this.tracks.sort();

    this.types = [...types];
    this.types.sort();

    this.schedule = buildStructuredEvents(this.events);

    this.combinePerformances(false);

    this.events$.next(this.events);
    this.eventsMap$.next(this.eventsMap);
    this.people$.next(this.people);
    this.peopleMap$.next(this.peopleMap);
    this.tracks$.next(this.tracks);
    this.types$.next(this.types);
    this.update$.next(1);
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

  private combinePerformances(emit: boolean) {
    if (this.events.length > 0 && this.performances) {
      this.performances.forEach(performance => {
        const event = this.eventsMap[performance.sessionId];
        if (event) {
          event.performance = performance;
        }
      });
      if (emit) {
        this.events$.next(this.events);
        this.eventsMap$.next(this.eventsMap);
      }
    }
  }

  init() {
    if (this.inited) {
      return;
    }
    timer(0, RELOAD_TIMER).subscribe(() => this.reload());
    this.inited = true;
  }

  getFilteredEvents(filters?: ProgramFilter): Observable<ScheduleEvent[]> {
    if (filters === undefined) {
      return this.events$;
    }

    let dateRanges: DateRange[] | undefined;

    const munged_filters: ((scheduleEvent: ScheduleEvent) => boolean)[] = [];
    if (filters.types && filters.types.length > 0) {
      const types = filters.types;
      munged_filters.push(
        scheduleEvent => types.some(
          filterString => scheduleEvent.type === filterString));
    }
    if (filters.tracks && filters.tracks.length > 0) {
      const tracks = filters.tracks;
      munged_filters.push(
        scheduleEvent => tracks.some(
          filterString => scheduleEvent.track === filterString));
    }
    if (filters.captionedOnly) {
      munged_filters.push(scheduleEvent => scheduleEvent.captioned);
    }
    if (filters.featuredOnly) {
      munged_filters.push(scheduleEvent => scheduleEvent.featured);
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
      return this.events$;
    }

    let eventsObservable;

    if (filters.id && filters.id.length > 0) {
      const id = filters.id;
      eventsObservable = this.eventsMap$.pipe(
        // It's okay to sort in place since filters.id.map() has created a new array.
        map(eventsMap => id.
          map(id => eventsMap[id]).
          filter(event => event).
          sort((a, b) => a.compare(b))),
      );
    } else {
      eventsObservable = this.events$;
    }

    return eventsObservable.pipe(
      map(events => {
        const dateFilteredEvents = dateRanges ? filterEventsByDate(events, dateRanges) : events;
        return dateFilteredEvents.filter((event) => munged_filters.every(filter => filter(event)));
      }),
    );
  }

  getSchedule(filters?: ProgramFilter): Observable<StructuredEvents> {
    if (filters === undefined) {
      return this.schedule$;
    }

    let dateRanges: DateRange[] | undefined;

    const munged_filters: ((scheduleEvent: ScheduleEvent) => boolean)[] = [];
    if (filters.types && filters.types.length > 0) {
      const types = filters.types;
      munged_filters.push(
        scheduleEvent => types.some(
          filterString => scheduleEvent.type === filterString));
    }
    if (filters.tracks && filters.tracks.length > 0) {
      const tracks = filters.tracks;
      munged_filters.push(
        scheduleEvent => tracks.some(
          filterString => scheduleEvent.track === filterString));
    }
    if (filters.captionedOnly) {
      munged_filters.push(scheduleEvent => scheduleEvent.captioned);
    }
    if (filters.featuredOnly) {
      munged_filters.push(scheduleEvent => scheduleEvent.featured);
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

  getEvent(id: string): Observable<ScheduleEvent | undefined> {
    return this.eventsMap$.pipe(
      map(eventsMap => eventsMap[id]),
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
          scheduleFilters.types = filters.types;
          scheduleFilters.tracks = filters.tracks;
          scheduleFilters.captionedOnly = filters.captionedOnly;
          scheduleFilters.featuredOnly = filters.featuredOnly;
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
            if (events[0].doors.start > currentTime) {
              return {next: events[0]};
            } else {
              return {started: events[0].start <= currentTime, current: events[0], next: events[1]};
            }
          }),
          /* emit runningEvents, and then calculate the time (based on the current time) until the next change and wait that long (but don't emit the timer event) */
          mergeMap(runningEvents => concat(of(runningEvents), defer(() => {
            const nextStartTime = runningEvents.started ? (runningEvents.next ? runningEvents.next.doors.start.getTime() - currentTime.getTime() : Infinity) :
              (runningEvents.current ? runningEvents.current.start.getTime() - currentTime.getTime() : Infinity);
            const nextEndTime = (runningEvents.current && runningEvents.current.doors.end) ? runningEvents.current.doors.end.getTime() - currentTime.getTime() : Infinity;
            const delayTime = Math.min(nextStartTime, nextEndTime);
            return timer(delayTime).pipe(ignoreElements());
          }))),
          /* Repeat */
          repeat(),
        )),
    );
  }

  getNextEventsList(events: Observable<ScheduleEvent[]>, count: number): Observable<ScheduleEvent[]> {
    let currentTime;
    const DELAY = 20;
    return events.pipe(
      switchMap(events =>
        defer(() => {
          const filtered = filterEventsByDate(events, [{start: new Date((currentTime = (this.settingsService.currentTime.getTime() - DELAY * 60 * 1000)))}], count);
          if (filtered.length > 0) {
            let delayTime = Infinity;
            for (let event of filtered) {
              const startTime = event.start.getTime() + DELAY * 60 * 1000;
              if (event.start.getTime() >= currentTime) {
                delayTime = startTime - currentTime;
                break;
              }
            }
            return concat(
              of(filtered), ignoreElements()(timer(delayTime))
            );
          } else {
            return timer(Infinity).pipe(ignoreElements());
          }
        })),
      repeat(),
    );
  }

  getPerformanceEvents(): Observable<RunningEvents> {
    return this.getNextEvents('Performance Hall');
  }

  get_featured_events(count = 3): Observable<ScheduleEvent[]> {
    // for testing:
    // return this.getSchedule({id: ['23', '45', '17']});
    return this.getNextEventsList(this.getFilteredEvents({featuredOnly: true}), count);
  }


  // TODO merge this in with the program data from the db ?
  get_gaming_meta(local: false): Observable<GamingMeta[]> {
    if (local) {
      return this.http.get<GamingMeta[]>('assets/data/gaming_11_1.json');
    }
    return this.http.get<GamingMeta[]>(`${environment.backend}/schedule/gamingmeta`);
  }

  getRooms(): Observable<Room[]> {
    return of([{ id: 'zoom_room_1', name: 'Zoom Room 1', idx: 1},
               { id: 'zoom_room_2', name: 'Zoom Room 2', idx: 2  },
               { id: 'zoom_room_3', name: 'Zoom Room 3', idx: 3  },
               { id: 'zoom_room_4', name: 'Zoom Room 4', idx: 4  },
               { id: 'zoom_room_5', name: 'Zoom Room 5', idx: 5  },
               { id: 'zoom_room_6', name: 'Zoom Room 6', idx: 6  },
               { id: 'zoom_room_7', name: 'Zoom Room 7', idx: 7  },
               { id: 'fast_track', name: 'Fast Track', idx: 8  },
              ].map(({id, name, idx}) => new Room(id, name, `/assets/images/programming/room-${idx}.png`, `/assets/images/programming/door-${idx}.png`, this)));
  }

  getRoom(id: string): Observable<Room | undefined> {
    return this.getRooms().pipe(
      map(rooms => rooms.find(room => room.id === id)),
    );
  }
}
