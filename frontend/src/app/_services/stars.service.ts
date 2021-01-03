import { Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { environment } from '@environments/environment';

// The upstream API looks something like the following. The get method returns a list of strings.
// GET     /api/schedule/stars          arisia.controllers.ScheduleController.getStars()
// PUT     /api/schedule/stars/:which   arisia.controllers.ScheduleController.addStar(which)
// DELETE  /api/schedule/stars/:which   arisia.controllers.ScheduleController.removeStar(which)

type StarData = string[];

@Injectable({
  providedIn: 'root'
})
export class StarsService implements Iterable<string> {
  constructor(private http: HttpClient) {
    this.load();
  }

  private starredIds = new Set<string>();
  observable$ = new ReplaySubject<StarsService>(1);

  load(): void {
    this.http.get<StarData>
      (`${environment.backend}/schedule/stars`).subscribe({
        next: (response) => this.handleResponse(response),
        error: (error) => this.handleError(error)
      });
  }

  handleResponse(response: StarData) {
    this.starredIds = new Set<string>(response);
    this.observable$.next(this);
  }

  handleError(error: HttpErrorResponse) {
    console.log(error);
  }

  add(id: string): void {
    this.starredIds.add(id);
    this.http.put(`${environment.backend}/schedule/stars/${id}`, '').subscribe();
    this.observable$.next(this);
  }

  delete(id: string): void {
    this.starredIds.delete(id);
    this.http.delete(`${environment.backend}/schedule/stars/${id}`).subscribe();
    this.observable$.next(this);
  }

  has(id: string): boolean {
    return this.starredIds.has(id);
  }

  keys() {
    return this.starredIds.keys();
  }

  values() {
    return this.starredIds.keys();
  }

  entries() {
    return this.starredIds.keys();
  }

  forEach(callbackFn: (value: string, value2: string, set: Set<string>) => void, thisArg?: any) {
    this.starredIds.forEach(callbackFn, thisArg);
  }

  [Symbol.iterator](): Iterator<string> {
    return this.starredIds[Symbol.iterator]();
  }
}
