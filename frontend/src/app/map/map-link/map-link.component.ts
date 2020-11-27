import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-map-link',
  templateUrl: './map-link.component.html',
  styleUrls: ['./map-link.component.scss']
})
export class MapLinkComponent implements OnInit {
  @Input() height = 1;
  @Input() circle = false;

  constructor() { }

  ngOnInit(): void {
  }

}
