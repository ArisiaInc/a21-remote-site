import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '@environments/environment';
import { CreatorService } from './creator.service';
import { Creator } from '@app/_models';

@Injectable({
  providedIn: 'root'
})
export class ArtistService extends CreatorService {

  constructor(http: HttpClient) {
    super (http);
  }

  dataUrl(local: boolean): string {
    if (local) {
      return `assets/data/artists.json`;
    } else {
      return `${environment.backend}/metadata/artshow`;
    }
  }

  protected gohSearch(): Observable<Creator[]> {
    return this.getCreatorsById(["112042"]);
  }
}
