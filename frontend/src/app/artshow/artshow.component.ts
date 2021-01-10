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

  constructor(public artistService: ArtistService) {}

  ngOnInit(): void {

    this.artists$ = this.artistService.search('');
  }

}
