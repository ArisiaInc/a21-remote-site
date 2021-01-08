import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Creator } from '@app/_models';
import { ArtistService } from '@app/_services';

@Component({
  selector: 'app-artshow',
  templateUrl: './artshow.component.html',
  styleUrls: ['./artshow.component.scss']
})
export class ArtshowComponent implements OnInit {
  artists$!: Observable<Creator[]>;

  constructor(public artistService: ArtistService) { }

  ngOnInit(): void {
    console.log('artshow init')
    this.artists$ = this.artistService.get_creators();
    let subsc = this.artistService.display$.subscribe(artists => {
      // this line never returns anything
      // also this subscription doesn't seem to exist.
      console.log(artists && artists.length ? artists[0].id : 'no artist');
    });
    console.log(subsc);
    console.log('artist service display', this.artistService.display$)
  }

}
