import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { tap, map } from 'rxjs/operators';
import { User } from '@app/_models';
import { of, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  public user: User;

  // for testing
  public badgeMap : {[_:string]: number[]};

  constructor( private http: HttpClient,) {
    this.badgeMap = {}
   }

  loggedIn() {
    if (this.user) {
      return of(true);
    }
    return this.http.get<User>(`${environment.backend}/me`, {withCredentials: true}).pipe(
      tap(u => this.user = u),
      map(u => u.id ? true : false)
    );
  }

  // todo stop sending password in plain text
  login(id: string, password: string) {
    return this.http.post<User>(`${environment.backend}/login`, {id, password}, {withCredentials: true}).pipe(
      tap(u => this.user = u),
    );
  }

  logout() {
    return this.http.post(`${environment.backend}/logout`, {}, {withCredentials: true}).pipe(
      tap(d => this.user = undefined)
    );
  }

  getUser(id: string) : Observable<User> {
    // this is for testing
    if (!this.badgeMap[id]) {
      this.badgeMap[id] = [8,5,10,15];
    }
    return of({id: 'joe', name: 'Joe', badges: this.badgeMap['joe']});
    // below is the real one
    //return this.http.get<User>(`${environment.backend}/user/${id}`, {withCredentials: true});
  }

  giveBadge(userId: string, badgeId: number) : Observable<User> {
    // this is for testing
    if (!this.badgeMap[userId]) {
      this.badgeMap[userId] = [];
    }
    this.badgeMap[userId].push(badgeId)
    return of({id: userId, name: 'Joe', badges: this.badgeMap[userId]})
    // below is the real one
    //return this.http.post<User>(`${environment.backend}/user/${userId}/badge/${badgeId}`, {}, {withCredentials: true});
  }

}
