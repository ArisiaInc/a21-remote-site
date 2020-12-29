import { Component, OnInit, Input } from '@angular/core';
import { Creator } from '@app/_models';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-creator-card',
  templateUrl: './creator-card.component.html',
  styleUrls: ['./creator-card.component.scss']
})
export class CreatorCardComponent implements OnInit {
  @Input() creator!: Creator;
  component!: string;

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.component = this.activatedRoute.snapshot.url[0].path;
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
