import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-room-link-other',
  templateUrl: './room-link-other.component.html',
  styleUrls: ['./room-link-other.component.scss']
})
export class RoomLinkOtherComponent implements OnInit {
  @Input() href!: string;

  constructor() { }

  ngOnInit(): void {
  }

}
