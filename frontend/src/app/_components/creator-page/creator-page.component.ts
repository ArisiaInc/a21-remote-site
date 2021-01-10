import { Component, OnChanges, Input } from '@angular/core';
import { Creator, PreferredLink } from '@app/_models';

@Component({
  selector: 'app-creator-page',
  templateUrl: './creator-page.component.html',
  styleUrls: ['./creator-page.component.scss']
})
export class CreatorPageComponent implements OnChanges {
  @Input() creator!: Creator;
  linksToDisplay: {[_: string]: string | undefined} = {};

  constructor() { }

  ngOnChanges(): void {
    // TODO check for undefined creator & handle. 500 error?

    // There's gotta be a better way to do this
    // this is a messy messy sad typecast nightmare
    if(this.creator && this.creator.links) {
      for (let k of Object.keys(this.creator.links) as unknown as PreferredLink) {
        if (k !== "preferred") this.linksToDisplay[k] = this.creator.links[k as PreferredLink];
      }
    }
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


}
