import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { pluck, map, switchMap } from 'rxjs/operators';


import { Creator } from '@app/_models';
import { ArtistService } from '@app/_services';
import { Crumb } from '@app/_components';

@Component({
  selector: 'app-artist',
  templateUrl: './artist.component.html',
  styleUrls: ['./artist.component.scss']
})
export class ArtistComponent implements OnInit {
  artist$!: Observable<Creator | undefined>;
  crumbsOverride$!: Observable<Crumb[]>;

  constructor(private route: ActivatedRoute,
    private artistService: ArtistService) { }

  ngOnInit(): void {
    this.artist$ = this.route.params.pipe(
      pluck('id'),
      switchMap(id => this.artistService.getCreator(id)),
    );
    this.crumbsOverride$ = this.artist$.pipe(
      map(artist => {
        const crumbs = [
          {path: '/map', label: 'Lobby'},
          {path: '/artshow', label: 'Art Show'},
        ];
        if (artist) {
          crumbs.push({path: '/artshow/${artist.id}', label: artist.name});
        } else {
          crumbs.push({path: '/artshow', label: 'Missing id'});
        }
        return crumbs;
      }),
    );
  }

}
