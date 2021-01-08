import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { map, pluck, switchMap, tap } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { XByNameService, Named } from '@app/_services';

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
export class XByNameComponent<T extends Named> implements OnInit {
  @Input() linkPrefix: string = '';
  @Input() noun: string = 'Nouns';
  @Input() service!: XByNameService<T>;
  @Input() goh: boolean = true;
  links$!: Observable<Link[]>;
  search$!: Observable<string>

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    console.log('x by name init');
    let toc = [{link: '', searchCompare: ' ', display: 'All', active: true}];
    if (this.goh) {
        toc = toc.concat({link: 'goh', searchCompare: 'goh', display: 'GOH', active: true});
    }
    this.links$ = this.service.initials$.pipe(
      map(initials =>
        toc.concat(Object.values(initials).map(({lower, upper, active}) => ({link: lower, searchCompare: lower, display: upper, active})))
         ),
         tap(ls => console.log("links", ls))
    );
    this.search$ = this.route.params.pipe(
      pluck('search'),
      map(search => search && search.toLowerCase()),
      tap(search => console.log("search", search))
    );
    console.log('xbyname service display$', this.service.display$)
    this.service.display$ = this.search$.pipe( 
      tap(search => console.log('updating display', search)),
      switchMap(search => {
        if (search) {
          return this.service.search(search);
        } else {
          return this.service.all$;
        }
      })
    );
    // this line does return things
    this.service.display$.subscribe( f => console.log(f && f.length ? f[0].id : 'no item'));
  }

}
