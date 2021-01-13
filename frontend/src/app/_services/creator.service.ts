import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, pipe, ReplaySubject } from 'rxjs';
import { Creator } from '@app/_models';
import { shareReplay, map } from 'rxjs/operators';

import { searchPrefixCaseInsensitive, Initial, createInitials } from '@app/_helpers/utils';
import { environment } from '@environments/environment';

export abstract class CreatorService {

  private creators: Creator[] = [];
  creators$ = new ReplaySubject<Creator[]>(1);

  private creatorMap: {[id: string]: Creator} = {};
  private creatorMap$ = new ReplaySubject<{[id: string]: Creator}>(1);

  initials$: Observable<Initial[]>;

  constructor(private http: HttpClient) {
    this.initials$ = this.creators$.pipe(
      createInitials(),
      shareReplay(1),
    );

    this.fetchData(environment.local_data);
  }

  abstract dataUrl(local: boolean): string;

  protected gohSearch(): Observable<Creator[]> {
    return of([]);
  }

  fetchData(local: boolean) {
    const url = this.dataUrl(local);
    this.http.get<Creator[]>(url).subscribe(response => this.handleResponse(response));
  }

  handleResponse(response: Creator[]) {
    this.creators = response;
    this.creatorMap = {};
    this.creators.forEach(creator => this.creatorMap[creator.id] = creator);
    this.creators$.next(this.creators);
    this.creatorMap$.next(this.creatorMap);
  }

  getCreatorsById(ids: string[]) : Observable<Creator[]> {
    return this.creatorMap$.pipe(
      map(creatorMap => ids.map(id => creatorMap[id]).filter(creator => creator))
    );
  }

  getCreator(id: string) : Observable<Creator | undefined> {
    return this.creatorMap$.pipe(
      map(creatorMap => creatorMap[id]),
    );
  }

  search(search: string) : Observable<Creator[]> {
    if (search === 'goh') {
      return this.gohSearch();
    } else {
      return this.creators$.pipe(
        searchPrefixCaseInsensitive(search),
      );
    }
  }
}
