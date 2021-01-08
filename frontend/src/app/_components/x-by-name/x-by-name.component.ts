import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable, ReplaySubject, of } from 'rxjs';
import { map, pluck, switchMap, tap } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { XByNameService } from '@app/_services';

export interface Link {
  link: string;
  searchCompare: string;
  display: string;
  active: boolean;
}


@Component({
  selector: 'app-x-by-name',
  templateUrl: './x-by-name.component.html',
  styleUrls: ['./x-by-name.component.scss']
})
export class XByNameComponent<T> implements OnInit {
  @Input() linkPrefix: string = '';
  @Input() noun: string = 'Nouns';
  @Input() service!: XByNameService<T>;
  links$!: Observable<Link[]>;
  search$!: Observable<string>

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.links$ = this.service.initials$.pipe(
      map(initials =>
        [{link: '', searchCompare: ' ', display: 'All', active: true},
         {link: 'goh', searchCompare: 'goh', display: 'GOH', active: true}].
        concat(Object.values(initials).map(({lower, upper, active}) => ({link: lower, searchCompare: lower, display: upper, active})))
         ),
      tap(initials => console.log('initials', initials))
    );
    this.search$ = this.route.params.pipe(
      pluck('search'),
      map(search => search && search.toLowerCase()),
      tap(search => console.log('search', search))
    );
    this.service.display$ = this.search$.pipe( 
      tap(search => console.log('trying to emit something', search)),
      switchMap(search => {
        if (search) {
          return this.service.search(search);
        } else {
          return this.service.all$;
        }
      })
    );
  }

}
