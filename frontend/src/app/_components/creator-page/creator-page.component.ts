import { Component, OnInit, Input } from '@angular/core';
import { Creator, PreferredLink } from '@app/_models';
import { Image as CarouselImage } from '@app/_components/carousel/carousel.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-creator-page',
  templateUrl: './creator-page.component.html',
  styleUrls: ['./creator-page.component.scss']
})
export class CreatorPageComponent implements OnInit {
  @Input() creator$!: Observable<Creator>;
  carouselImages: CarouselImage[] = [];

  constructor() { }

  ngOnInit(): void {
    // TODO check for undefined creator & handle. 404?
    this.creator$.subscribe(c => {
      // i know this is not the right way to do this
      this.carouselImages = c.images.map(i => ({src: i.url, caption: i.title}))
    });
  }

  nameFromLinkType(linkType: string) {
    switch(linkType) {
      case 'etsy':
        return 'Etsy Shop';
      case 'youtube':
        return 'YouTube';
      case 'facebook':
        return 'Facebook';
      case 'insta':
        return 'Instagram';
      default:
        return 'Website';
    }
  }

  // There's gotta be a better way to do this
  // this is a messy messy sad typecast nightmare
  linksToDisplay(creator : Creator) {
    if(creator && creator.links) {
      const result : {[_: string]: string | undefined} = Object();
      for (let k of Object.keys(creator.links) as unknown as PreferredLink) {
        if (k !== "preferred") result[k] = creator.links[k as PreferredLink];
      }
      return result;
    }
    return false;
  }

}
