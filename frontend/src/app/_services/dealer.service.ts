import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '@environments/environment';
import { CreatorService } from './creator.service';

@Injectable({
  providedIn: 'root'
})
export class DealerService extends CreatorService {

  constructor(http: HttpClient) {
    super (http);
  }

  dataUrl(local: boolean): string {
    if (local) {
      return 'assets/data/dealers.json';
    } else {
      return `${environment.backend}/metadata/dealers`;
    }
  }
}
