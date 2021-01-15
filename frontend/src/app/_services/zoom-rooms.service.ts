import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ZoomRoomsService {

  constructor(private http: HttpClient) {
  }

  checkRoom(room: string): Observable<boolean> {
    return this.http.get<{running: boolean}>(`/api/room/open/${room}`).pipe(
      map(({running}) => running),
      catchError(_ => of(false)),
    );
  }

  getRoomUrl(room: string) {
    return `/api/room/enter/${room}`;
  }
}
