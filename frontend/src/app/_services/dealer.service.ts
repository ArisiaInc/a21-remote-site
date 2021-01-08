import { Injectable } from '@angular/core';
import { CreatorService } from './creator.service';
import { HttpClient } from '@angular/common/http';
import { OperatorFunction, pipe } from 'rxjs';
import { Creator } from '@app/_models';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DealerService extends CreatorService{

  constructor(http: HttpClient) {
    super(http);
   }

  goh_filter() : OperatorFunction<Creator[], Creator[]> {
    // dealers doesn't have a goh
    return pipe()
  }

  get_data(local: boolean) {
    if (local) {
      return this.http.get<Creator[]>(`assets/data/dealers.json`);
    }
    return this.http.get<Creator[]>(`${environment.backend}/dealers`);
  }
}
