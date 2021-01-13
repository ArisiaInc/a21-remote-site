import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, defer, of, timer, concat } from 'rxjs';
import { repeat, ignoreElements } from 'rxjs/operators';

import { AccountService } from './account.service';
import { environment } from '@environments/environment';
import { DateRange } from '@app/_models';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {

  settings: {[key: string]: string} = {};

  hour12$ = new BehaviorSubject(this.hour12);

  get hour12(): boolean {
    // Default to true
    return !(this.settings["hour12"] === '0');
  }

  set hour12(value: boolean) {
    this.setValue('hour12', value ? '1' : '0');
    this.hour12$.next(value);
  }

  disableAnimations$ = new BehaviorSubject(this.disableAnimations);

  get disableAnimations(): boolean {
    // Default to false
    return this.settings["disableAnimations"] === '1';
  }

  set disableAnimations(value: boolean) {
    this.setValue('disableAnimations', value ? '1' : '0');
    this.disableAnimations$.next(value);
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

  isInDateRange(dateRange: DateRange): Observable<boolean> {
    return defer(() => {
      const startTime = dateRange.start.getTime() - this.currentTime.getTime();
      const endTime = dateRange.end ? dateRange.end.getTime() - this.currentTime.getTime() : Infinity;
      if (startTime > 0) {
        return concat(of(false), timer(startTime).pipe(ignoreElements()));
      } else if (endTime > 0) {
        return concat(of(true), timer(endTime).pipe(ignoreElements()));
      } else {
        return concat(of(false), timer(Infinity).pipe(ignoreElements()));
      }
    }).pipe(repeat());
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
              this.disableAnimations$.next(this.disableAnimations);
            });
        } else {
          this.settings = {};
          this.hour12$.next(this.hour12);
          this.timeOffset$.next(this.timeOffset);
          this.disableAnimations$.next(this.disableAnimations);
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
