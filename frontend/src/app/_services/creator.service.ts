import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { Observable, of, pipe, OperatorFunction } from 'rxjs';
import { Creator } from '@app/_models';
import { flatMap, filter, toArray } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CreatorService {

  constructor( private http: HttpClient) { }

  get_dealer_data(local: boolean) {
    if (local) {
      return this.http.get<Creator[]>(`assets/data/dealers_18_1.json`);
    }
    return this.http.get<Creator[]>(`${environment.backend}/dealers`);
  }

  get_artist_data(local: boolean) {
    if (local) {
      return this.http.get<Creator[]>(`assets/data/artists_18_1.json`);
    }
    return this.http.get<Creator[]>(`${environment.backend}/artists`);
  }

  make_id_filter(ids?: string[]) : OperatorFunction<Creator[], Creator[]> {
    if (ids) {
      return pipe(
        flatMap(x => of(...x)),
        filter((c: Creator) => ids.includes(c.id)),
        toArray()
      )
    }
    return pipe()
  }

  get_artists(ids?: string[]) : Observable<Creator[]>{
    return this.get_artist_data(true).pipe(
      this.make_id_filter(ids)
    );
  }

  get_dealers(ids?: string[]) : Observable<Creator[]>{
    // change the below value to false to get from the server
    return this.get_dealer_data(true).pipe(
      this.make_id_filter(ids)
    );
  }
}
