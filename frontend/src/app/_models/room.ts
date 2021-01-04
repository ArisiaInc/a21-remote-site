import { Observable, BehaviorSubject } from 'rxjs';

import { ProgramItem } from "@app/_models";

export class Room {
  private currentEvent: ProgramItem | undefined;
  private nextEvent: ProgramItem | undefined;

  id: string;
  name: string;
  currentEvent$: BehaviorSubject<ProgramItem | undefined>;
  nextEvent$: BehaviorSubject<ProgramItem | undefined>

  constructor(id: string, name: string, currentEvent?: ProgramItem, nextEvent?: ProgramItem) {
    this.id = id;
    this.name = name;
    this.currentEvent = currentEvent;
    this.nextEvent = nextEvent;

    this.currentEvent$ = new BehaviorSubject(this.currentEvent);
    this.nextEvent$ = new BehaviorSubject(nextEvent);
  }

  setCurrentEvent(currentEvent: ProgramItem | undefined) {
    this.currentEvent = currentEvent;
    this.currentEvent$.next(this.currentEvent);
  }

  setNextEvent(nextEvent: ProgramItem | undefined) {
    this.nextEvent = nextEvent;
    this.nextEvent$.next(this.nextEvent);
  }
}
