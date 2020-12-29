import { Component, OnInit, Input } from '@angular/core';
import { Creator, PreferredLink } from '@app/_models';

@Component({
  selector: 'app-creator-page',
  templateUrl: './creator-page.component.html',
  styleUrls: ['./creator-page.component.scss']
})
export class CreatorPageComponent implements OnInit {
  @Input() creator!: Creator;

  constructor() { }

  ngOnInit(): void {
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
  linksToDisplay() {
    if(this.creator && this.creator.links) {
      const result : {[_: string]: string | undefined} = Object();
      for (let k of Object.keys(this.creator.links) as unknown as PreferredLink) {
        if (k !== "preferred") result[k] = this.creator.links[k as PreferredLink];
      }
      return result;
    }
    return false;
  }

}
