import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { tap, map } from 'rxjs/operators';
import { User } from '@app/_models';
import { of } from 'rxjs';

interface LoginRequest {
  id: string;
  password: string
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  public user: User;

  constructor( private http: HttpClient,) { }

  loggedIn() {
    if (this.user) {
      return of(true);
    }
    return this.http.get<User>(`${environment.backend}/me`).pipe(
      tap(u => this.user = u),
      map(u => u ? true : false)
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

}
