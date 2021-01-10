import { Component, OnChanges, Input } from '@angular/core';
import { Observable } from 'rxjs';

import { Initial } from '@app/_services';

interface Link {
  link: string;
  searchCompare: string;
  display: string;
  active: boolean;
}

@Component({
  selector: 'app-alphabet-links',
  templateUrl: './alphabet-links.component.html',
  styleUrls: ['./alphabet-links.component.scss']
})
export class AlphabetLinksComponent implements OnChanges {
  @Input() initials?: Initial[];
  @Input() search: string = '';
  @Input() includeGoh = false;
  @Input() urlPrefix = '/';
  links?: Link[] = [];

  constructor() { }

  ngOnChanges(): void {
    if (this.initials) {
      this.links = [];
      this.links.push({link: this.urlPrefix, searchCompare: '', display: 'All', active: true});
      if (this.includeGoh) {
        this.links.push({link: `${this.urlPrefix}/goh`, searchCompare: 'goh', display: 'GOH', active: true});
      }
      this.links = this.links.concat(
        (this.initials || []).map(
          ({lower, upper, active}) => ({link: `${this.urlPrefix}/${lower}`, searchCompare: lower, display: upper, active})));
    } else {
      this.links = undefined;
    }
  }

}
