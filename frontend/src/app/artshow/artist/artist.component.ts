import { Component, OnInit } from '@angular/core';
import { Creator } from '@app/_models';
import { Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { CreatorService } from '@app/_services';
import { pluck, flatMap, map } from 'rxjs/operators';

@Component({
  selector: 'app-artist',
  templateUrl: './artist.component.html',
  styleUrls: ['./artist.component.scss']
})
export class ArtistComponent implements OnInit {
  artist$!: Observable<Creator | undefined>;

  constructor(private route: ActivatedRoute,
    private creatorService: CreatorService) { }

  ngOnInit(): void {
    this.artist$ = this.route.params.pipe(
      pluck('id'),
      flatMap(id => this.creatorService.get_artists([id])),
      map(x => x.length ? x[0] : undefined)
    );
  }

}
