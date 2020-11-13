import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { tap } from 'rxjs/operators';

interface LoginRequest {
  id: string;
  password: string
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  public userValue: boolean;

  constructor( private http: HttpClient) { }

  // todo stop sending password in plain text
  login(id: string, password: string) {
    return this.http.post(`${environment.backend}api/login`, {id, password}).pipe(
      tap(d => console.log(d))
    );
  }
}
