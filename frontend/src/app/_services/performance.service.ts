import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { ReplaySubject } from 'rxjs';

import { Performance } from '@app/_models';

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {
  private performances: Performance[] = [];
  performances$ = new ReplaySubject<Performance[]>(1);

  constructor(private http: HttpClient) {
    this.http.get<Performance[]>
      ('/assets/data/performances.json').subscribe({
        next: (response) => this.handleResponse(response),
        error: (error) => this.handleError(error)
      });
  }

  handleResponse(response: Performance[]): void {
    this.performances = response;
    this.performances$.next(this.performances);
  }
  private handleError(error?: HttpErrorResponse): void {
  }
}
