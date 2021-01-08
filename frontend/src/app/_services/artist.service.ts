import { Injectable } from '@angular/core';
import { CreatorService } from './creator.service';
import { HttpClient } from '@angular/common/http';
import { Creator } from '@app/_models';
import { environment } from '@environments/environment';
import { pipe, OperatorFunction } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ArtistService extends CreatorService{

  constructor(http: HttpClient) { 
    super(http);
  }

  goh_filter() : OperatorFunction<Creator[], Creator[]> {
    // lol we don't actually have hannibal yet.
    return pipe()
  }

  get_data(local: boolean) {
    if (local) {
      return this.http.get<Creator[]>(`assets/data/artists.json`);
    }
    return this.http.get<Creator[]>(`${environment.backend}/artists`);
  }
}
