import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Creator } from '@app/_models';
import { CreatorService } from '@app/_services';

@Component({
  selector: 'app-artshow',
  templateUrl: './artshow.component.html',
  styleUrls: ['./artshow.component.scss']
})
export class ArtshowComponent implements OnInit {
  artists$!: Observable<Creator[]>;

  constructor(private creatorService: CreatorService) { }

  ngOnInit(): void {
    this.artists$ = this.creatorService.get_artists();
  }

}
