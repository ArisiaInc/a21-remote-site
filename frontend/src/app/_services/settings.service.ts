import { Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {

  settings: {[key: string]: string} = {};

  hour12$ = new ReplaySubject<boolean>(1);

  get hour12(): boolean {
    return !(this.settings["hour12"] === '0');
  }

  set hour12(value: boolean) {
    this.settings["hour12"] = value ? '1' : '0';
    this.hour12$.next(value);
  }

  timeOffset$ = new ReplaySubject<number>(1);

  get timeOffset(): number {
    return this.settings["timeOffset"] ? parseInt(this.settings["timeOffset"], 10) : 0;
  }

  set timeOffset(value: number) {
    this.settings["timeOffset"] = value.toString();
    this.timeOffset$.next(value);
  }

  get currentTime(): Date {
    const offset = this.timeOffset;
    return offset ? new Date(new Date().getTime() + offset) : new Date();
  }

  constructor() {
    this.hour12$.next(this.hour12);
    this.timeOffset$.next(this.timeOffset);
  }
}
