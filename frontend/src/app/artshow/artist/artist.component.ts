import { Component, OnInit } from '@angular/core';
import { Creator } from '@app/_models';
import { Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { ArtistService } from '@app/_services';
import { pluck, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-artist',
  templateUrl: './artist.component.html',
  styleUrls: ['./artist.component.scss']
})
export class ArtistComponent implements OnInit {
  artist$!: Observable<Creator | undefined>;

  constructor(private route: ActivatedRoute,
    private artistService: ArtistService) { }

  ngOnInit(): void {
    this.artist$ = this.route.params.pipe(
      pluck('id'),
      switchMap(id => this.artistService.getCreator(id)),
    );
  }

}
