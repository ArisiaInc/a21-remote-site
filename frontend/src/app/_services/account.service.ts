import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, map, shareReplay } from 'rxjs/operators';
import { of, Observable, BehaviorSubject, ReplaySubject } from 'rxjs';

import { environment } from '@environments/environment';
import { User } from '@app/_models';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  user?: User = undefined;
  readonly user$ = new ReplaySubject<User | undefined>(1);
  readonly loggedIn$!: Observable<boolean>;

  get loggedIn(): boolean {
    return !!this.user;
  }

  constructor( private http: HttpClient,) {
    this.http.get<User>(`${environment.backend}/me`, {withCredentials: true}).subscribe(
      user => {
        this.user = user;
        this.user$.next(this.user);
      });
    this.loggedIn$ = this.user$.pipe(
      map(user => !!user),
    );
  }

  // TODO: stop sending password in plain text
  login(id: string, password: string) {
    const loginRequest = this.http.post<User>(`${environment.backend}/login`, {id, password}, {withCredentials: true}).pipe(
      shareReplay(),
    );
    loginRequest.subscribe(
      user => {
        this.user = user;
        this.user$.next(this.user);
      });
    return loginRequest;
  }

  logout() {
    const loginRequest = this.http.post(`${environment.backend}/logout`, {}, {withCredentials: true}).pipe(
      shareReplay(),
    );
    loginRequest.subscribe(
      user => {
        this.user = undefined;
        this.user$.next(this.user);
      });
    return loginRequest;
  }

  getUser(badgeNumber: string) : Observable<User> {
    if (this.user?.badgeNumber === badgeNumber) {
      return of(this.user);
    } else {
      // This is for testing.
      return of({id: 'joe', name: 'Joe', badgeNumber: badgeNumber, zoomHost: false});
      // below is the real one
      // this.http.get<User>(`${environment.backend}/user/${id}`, {withCredentials: true})
    }
  }
}
