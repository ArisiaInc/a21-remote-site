import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, ReplaySubject } from 'rxjs';
import { Ribbon } from '@app/_models/ribbon';
import { environment } from '@environments/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RibbonService {

  private ribbons: Ribbon[] = [];
  ribbons$ = new ReplaySubject<Ribbon[]>(1);

  private ribbonMap: {[id: number]: Ribbon} = {};
  private ribbonMap$ = new ReplaySubject<{[id: number]: Ribbon}>(1);

  private secretMap: {[secret: string]: Ribbon} = {};
  private secretMap$ = new ReplaySubject<{[secret: string]: Ribbon}>(1);

  constructor(private http: HttpClient) { 
    this.fetchData();
  }

  fetchData() {
    return this.http.get<Ribbon[]>(`${environment.backend}/ribbons`).subscribe(response => this.handleResponse(response));
  }

  handleResponse(response: Ribbon[]) {
    this.ribbons = response;
    this.ribbonMap = {};
    this.secretMap = {};
    this.ribbons.forEach(ribbon => {
      this.ribbonMap[ribbon.ribbonid] = ribbon;
      this.secretMap[ribbon.secret] = ribbon;
    });
    this.ribbons$.next(this.ribbons);
    this.ribbonMap$.next(this.ribbonMap);
    this.secretMap$.next(this.secretMap);
  }

  getRibbonsById(ids: number[]) : Observable<Ribbon[]> {
    return this.ribbonMap$.pipe(
      map(ribbonMap => ids.map(id => ribbonMap[id]).filter(ribbon => ribbon))
    )
  }

  getRibbon(secret: string) : Observable<Ribbon | undefined> {
    return this.secretMap$.pipe(
      map(secretMap => secretMap[secret])
    );
  }

  assignRibbon(id: number, secret: string) {
    return this.http.post(`${environment.backend}/user/ribbons/${id}`, {secret}, {withCredentials: true})
  }

  orderRibbons(ribbonIds: number[]) {
    return this.http.post(`${environment.backend}/user/ribbons`, {ribbonIds}, {withCredentials: true})
  }

}
