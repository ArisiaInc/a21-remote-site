import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

import { AccountService } from './account.service';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {

  settings: {[key: string]: string} = {};

  hour12$ = new BehaviorSubject(this.hour12);

  get hour12(): boolean {
    return !(this.settings["hour12"] === '0');
  }

  set hour12(value: boolean) {
    this.setValue('hour12', value ? '1' : '0');
    this.hour12$.next(value);
  }

  timeOffset$ = new BehaviorSubject(this.timeOffset);

  get timeOffset(): number {
    return this.settings["timeOffset"] ? parseInt(this.settings["timeOffset"], 10) : 0;
  }

  set timeOffset(value: number) {
    this.setValue('timeOffset', value.toString());
    this.timeOffset$.next(value);
  }

  get currentTime(): Date {
    const offset = this.timeOffset;
    return offset ? new Date(new Date().getTime() + offset) : new Date();
  }

  set currentTime(value: Date) {
    this.timeOffset = value.getTime() - new Date().getTime();
  }


  constructor(private accountService: AccountService,
              private http: HttpClient,) {
    this.accountService.loggedIn$.subscribe(
      loggedIn => {
        if (loggedIn) {
          this.http.get<{[key: string]: string}>(`${environment.backend}/settings`, {withCredentials: true}).subscribe(
            settings => {
              this.settings = settings;
              this.hour12$.next(this.hour12);
              this.timeOffset$.next(this.timeOffset);
            });
        } else {
          this.settings = {};
          this.hour12$.next(this.hour12);
          this.timeOffset$.next(this.timeOffset);
        }
      });
  }

  setValue(key: string, value: string): void {
    this.settings[key] = value;
    if (this.accountService.loggedIn) {
      this.http.post(`${environment.backend}/settings`, {[key]: value}, {withCredentials: true}).subscribe();
    }
  }
}
