import { Component, OnChanges, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'app-navigation-link',
    templateUrl: './navigation-link.component.html',
    styleUrls: ['./navigation-link.component.scss']
})
export class NavigationLinkComponent implements OnChanges {
  @Input() link?: string = undefined;
  @Input() title!: string;
  @Input() returnUrl = '';
  @Input() submenu = false
  @Input() selectedKey = '';
  @Input() id?: string;
  selected = false;

  @Output() activated: EventEmitter<void> = new EventEmitter();

  constructor() {
  }

  ngOnChanges(): void {
    if (!this.id) {
      this.id = this.link;
    }
    if (this.selectedKey && this.id && this.selectedKey.startsWith(this.id)) {
      this.selected = true;
    } else {
      this.selected = false;
    }
    // assert (this.link !== undefined);
    // assert (this.title !== undefined);
  }
}
