import { Component, OnInit, Input, HostBinding } from '@angular/core';

@Component({
  selector: 'app-link',
  templateUrl: './link.component.html',
  styleUrls: ['./link.component.scss']
})
export class LinkComponent implements OnInit {
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
