import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { map, shareReplay, catchError } from 'rxjs/operators';

import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DiscordService {

  readonly discriminatorRegexp = /^[0-9]{4}$/;

  constructor(private http: HttpClient) { }

  addArisian(usernameAndDiscriminator: string): Observable<string> {
    const splitString = usernameAndDiscriminator.split('#');
    if (splitString.length != 2 || !splitString[0] ||
      !splitString[1].match(this.discriminatorRegexp)) {
      return throwError("Requires a username followed by a single # followed by 4 digits.");
    }
    const username = splitString[0];
    const discriminator = splitString[1];
    const connectRequest = this.http.post<any>(`${environment.backend}/discord/addArisian`, {username, discriminator}, {withCredentials: true}).pipe(
      shareReplay(),
    );
    connectRequest.subscribe();
    return connectRequest.pipe(
      map(response => ''),
      catchError((err: HttpErrorResponse) => throwError(err?.error?.message)),
    );
  }

  getAssistSecret(): Observable<string> {
    const request = this.http.get(`${environment.backend}/discord/getAssistSecret`, {responseType: 'text', withCredentials: true}).pipe(
      shareReplay(),
    );
    request.subscribe();
    return request.pipe(
      catchError((err: HttpErrorResponse) => throwError(err?.error?.message)),
    );
  }
}
