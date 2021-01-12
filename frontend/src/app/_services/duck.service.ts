import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { Observable, of } from 'rxjs';
import { Duck } from '@app/_models';
import { tap, catchError, mapTo } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class DuckService {

  constructor(private http: HttpClient) {
  }

  get_ducks() : Observable<Duck[]> {
    return this.http.get<Duck[]>(`${environment.backend}/ducks`);
  }

  get_duck(id: number) : Observable<Duck | undefined> {
    return this.http.get<Duck|undefined>(`${environment.backend}/ducks/${id}`);
  }

  award_duck(id: number) {
    return this.http.post<any>(`${environment.backend}/ducks/${id}`, {}, {withCredentials: true}).pipe(
      catchError(e => of(false))
    );
  }
}
