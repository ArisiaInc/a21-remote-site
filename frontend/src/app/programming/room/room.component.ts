import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { pluck, flatMap } from 'rxjs/operators'

import { ScheduleService, Room } from '@app/_services';

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.scss']
})
export class RoomComponent implements OnInit {
  room$!: Observable<Room | undefined>;

  constructor(private route: ActivatedRoute, private scheduleService: ScheduleService) {
  }

  ngOnInit(): void {
    this.room$ = this.route.params.pipe(
      pluck('id'),
      flatMap(id => this.scheduleService.getRoom(id)),
    );
  }
}
