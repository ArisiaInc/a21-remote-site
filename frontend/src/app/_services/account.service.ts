import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, tap, shareReplay, catchError } from 'rxjs/operators';
import { of, Observable, BehaviorSubject, ReplaySubject } from 'rxjs';

import { environment } from '@environments/environment';
import { User } from '@app/_models';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  me?: User = undefined;
  readonly me$ = new ReplaySubject<User | undefined>(1);
  user?: User = undefined;
  readonly user$ = new ReplaySubject<User | undefined>(1);
  readonly loggedIn$!: Observable<boolean>;

  get loggedIn(): boolean {
    return !!this.me;
  }

  constructor( private http: HttpClient,) {
    this.http.get<User>(`${environment.backend}/me`, {withCredentials: true}).subscribe(
      me => {
        this.me = me;
        this.me.self = true;
        this.me$.next(this.me);
      },
      err => {
        this.me = undefined;
        this.me$.next(this.me);
      });
    this.me$.subscribe(me => {
      if (me) {
        this.loadUser();
      } else {
        this.user = undefined;
        this.user$.next(this.user);
      }
    });
    this.loggedIn$ = this.me$.pipe(
      map(me => !!me),
    );
  }

  loadUser() {
    return this.http.get<User>(`${environment.backend}/user`, {withCredentials: true}).subscribe(
      response => {
        this.user = response;
        this.user.self = true;
        this.user$.next(this.user);
      },
      err => {
        this.user = undefined;
        this.user$.next(this.user);
      });
  }

  // TODO: stop sending password in plain text
  login(id: string, password: string) {
    const loginRequest = this.http.post<User>(`${environment.backend}/login`, {id, password}, {withCredentials: true}).pipe(
      shareReplay(),
    );
    loginRequest.subscribe(
      me => {
        this.me = me;
        this.me.self = true;
        this.me$.next(this.me);
      });
    return loginRequest;
  }

  logout() {
    const loginRequest = this.http.post(`${environment.backend}/logout`, {}, {withCredentials: true}).pipe(
      shareReplay(),
    );
    loginRequest.subscribe(
      me => {
        this.me = undefined;
        this.me$.next(this.me);
      });
    return loginRequest;
  }

  getUser(badgeNumber: string) : Observable<User | undefined> {
    if (this.user?.badgeNumber === badgeNumber) {
      return this.user$;
    } else {
      return this.http.get<User>(`${environment.backend}/user/${badgeNumber}`, {withCredentials: true}).pipe(
        tap(user => user.self = false),
        catchError(_ => of(undefined)),
      );
    }
  }

  awardDuck(id: number): Observable<boolean> {
//    if (!this.user) {
//      return of(false);
//    }
//    if(this.user.ducks.includes(id)) {
//      return of(true);
//    }
//    this.user.ducks.push(id);
//    this.user$.next(this.user);
    return this.http.post<any>(`${environment.backend}/ducks/${id}`, {}, {withCredentials: true}).pipe(
      map(_ => true),
      catchError(e => of(false))
    );
  }
}
