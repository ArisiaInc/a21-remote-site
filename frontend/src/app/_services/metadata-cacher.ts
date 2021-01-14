import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ReplaySubject } from 'rxjs';

export class MetadataCacher<DataT> {
  private data: DataT;
  data$ = new ReplaySubject<DataT>(1);

  constructor(private http: HttpClient, private url: string, def: DataT) {
    this.data = def;
    this.load();
  }

  private load(): void {
    this.http.get<DataT>(this.url).subscribe(
      response => this.handleResponse(response),
      error => this.handleError(error)
    );
  }

  private handleResponse(response: DataT): void {
    this.data = response;
    this.data$.next(this.data);
  }

  private handleError(error: HttpErrorResponse) {
    console.log(error);
  }

  updateData(mapFn: (t: DataT) => DataT): void {
    this.data = mapFn(this.data);
    this.data$.next(this.data);
  }
}
