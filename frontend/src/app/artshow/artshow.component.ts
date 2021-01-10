import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, map, switchMap } from 'rxjs/operators';

import { Creator } from '@app/_models';
import { ArtistService } from '@app/_services';

@Component({
  selector: 'app-artshow',
  templateUrl: './artshow.component.html',
  styleUrls: ['./artshow.component.scss']
})
export class ArtshowComponent implements OnInit {
  artists$!: Observable<Creator[]>;
  search$!: Observable<string>;

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
  }

}
