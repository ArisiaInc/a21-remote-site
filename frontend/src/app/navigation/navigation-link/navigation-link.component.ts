import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'app-navigation-link',
    templateUrl: './navigation-link.component.html',
    styleUrls: ['./navigation-link.component.scss']
})
export class NavigationLinkComponent implements OnInit {
  @Input() link?: string = undefined;
  @Input() title!: string;
  @Input() returnUrl = '';

  @Output() activated: EventEmitter<void> = new EventEmitter();

  constructor() {
  }

  ngOnInit(): void {
    // assert (this.link !== undefined);
    // assert (this.title !== undefined);
  }
}
