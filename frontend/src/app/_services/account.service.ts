import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, map } from 'rxjs/operators';
import { of, Observable, BehaviorSubject } from 'rxjs';

import { environment } from '@environments/environment';
import { User } from '@app/_models';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  user?: User = undefined;
  readonly user$ = new BehaviorSubject<User | undefined>(this.user);
  readonly loggedIn$!: Observable<boolean>;

  constructor( private http: HttpClient,) {
    this.http.get<User>(`${environment.backend}/me`, {withCredentials: true}).pipe(
      tap(user => {
        this.user = user;
        this.user$.next(this.user);
      }),
    );
    this.loggedIn$ =this.user$.pipe(
      map(user => !!user),
    );
  }

  // TODO: stop sending password in plain text
  login(id: string, password: string) {
    return this.http.post<User>(`${environment.backend}/login`, {id, password}, {withCredentials: true}).pipe(
      tap(user => {
        this.user = user;
        this.user$.next(this.user);
      }),
    );
  }

  logout() {
    return this.http.post(`${environment.backend}/logout`, {}, {withCredentials: true}).pipe(
      tap(_ => {
        this.user = undefined;
        this.user$.next(this.user);
      }),
    );
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
