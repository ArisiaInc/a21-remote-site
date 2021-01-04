import { Component, OnInit } from '@angular/core';
import { NgbCarousel, NgbSlideEvent } from '@ng-bootstrap/ng-bootstrap';

import { Image } from '@app/_components/carousel/carousel.component';

@Component({
  selector: 'app-hall-costumes',
  templateUrl: './hall-costumes.component.html',
  styleUrls: ['./hall-costumes.component.scss']

})
export class HallCostumesComponent implements OnInit {
  images: Image[] = [];

  constructor() { }

  ngOnInit(): void {
    this.images = [{src: 'https://coolbackgrounds.io/images/backgrounds/white/pure-white-background-85a2a7fd.jpg', caption: 'This Image Is Totally White'}].
      concat([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20].
        map((n) => ({src: `https://picsum.photos/id/${n}/900/500`, caption: `Image number ${n}`})));
  }
}
