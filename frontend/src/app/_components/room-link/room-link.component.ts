import { Component, OnInit, Input } from '@angular/core';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-room-link',
  templateUrl: './room-link.component.html',
  styleUrls: ['./room-link.component.scss']
})
export class RoomLinkComponent implements OnInit {
  @Input() id!: string;
  environment = environment;

  constructor() { }

  ngOnInit(): void {
  }

}
