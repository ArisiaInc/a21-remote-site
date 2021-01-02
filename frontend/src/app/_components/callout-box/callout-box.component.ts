import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-callout-box',
  templateUrl: './callout-box.component.html',
  styleUrls: ['./callout-box.component.scss'],
  encapsulation: ViewEncapsulation.ShadowDom
})
export class CalloutBoxComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
