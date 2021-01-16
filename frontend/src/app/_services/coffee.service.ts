import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { CreatorImage } from '@app/_models';
import { environment } from '@environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CoffeeService {

  constructor(private http: HttpClient) { }

  get_costumes() : Observable<CreatorImage[]> {
    return this.http.get<CreatorImage[]>(`${environment.backend}/metadata/coffee`);
  }
}
