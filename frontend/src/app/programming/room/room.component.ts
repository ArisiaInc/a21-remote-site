import { Component, OnInit } from '@angular/core';
import { ActivatedRoute} from '@angular/router';
import { ProgramItem, ProgramPerson, Room } from '@app/_models';
import { ScheduleService } from '@app/_services';

import { pluck, flatMap } from 'rxjs/operators'

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.scss']
})
export class RoomComponent implements OnInit {
  room?: Room;
  loading = true;

  constructor(private route: ActivatedRoute, private scheduleService: ScheduleService) {
  }

  ngOnInit(): void {
    this.route.params.pipe(
      pluck('id'),
      flatMap(id => this.scheduleService.get_room(id)),
    ).subscribe(room => {
      this.loading = false;
      this.room = room;
    });
  }
}
