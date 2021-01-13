import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, map, switchMap } from 'rxjs/operators';

import { Creator } from '@app/_models';
import { ArtistService } from '@app/_services';
import { Crumb } from '@app/_components';

@Component({
  selector: 'app-artshow',
  templateUrl: './artshow.component.html',
  styleUrls: ['./artshow.component.scss']
})
export class ArtshowComponent implements OnInit {
  artists$!: Observable<Creator[]>;
  search$!: Observable<string>;
  crumbsOverride$!: Observable<Crumb[]>;

  constructor(
    private route: ActivatedRoute,
    public artistService: ArtistService) {}

  ngOnInit(): void {
    this.search$ = this.route.params.pipe(
      pluck('search'),
      map(search => (search || '').toLowerCase())
    );

    this.artists$ = this.search$.pipe(
      switchMap(search => this.artistService.search(search)),
    );

    this.crumbsOverride$ = this.search$.pipe(
      map(search => {
        const crumbs = [
          {path: '/map', label: 'Lobby'},
          {path: '/artshow', label: 'Art Show'},
        ];
        if (search == 'goh') {
          crumbs.push({path: '/artshow/search/goh', label: 'Guest of Honor'});
        } else if (search) {
          crumbs.push({path: `/artshow/search/${search}`, label: search});
        }
        return crumbs;
      }),
    );
  }
}
