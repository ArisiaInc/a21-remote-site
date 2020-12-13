import { Component, OnInit, Input, HostBinding } from '@angular/core';

@Component({
  selector: 'app-map-link',
  templateUrl: './map-link.component.html',
  styleUrls: ['./map-link.component.scss'],
  host: {'class.px-2':'true'}
})
export class MapLinkComponent implements OnInit {
  @Input() height = 1;
  @Input() color = "#c4c4c4";
  @HostBinding('class.circle') @Input() circle = false;
  @HostBinding('style') style = '';

  @HostBinding('class') class = 'm-2';

  constructor() { }

  ngOnInit(): void {
    this.style =  `background-color:${this.color};height:${10*this.height-1+'rem'}`
  }
}
