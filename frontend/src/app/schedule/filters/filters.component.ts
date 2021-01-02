import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { ScheduleService, SettingsService } from '@app/_services';
import { ProgramFilter } from '@app/_models';

enum Property {
  LOC,
};

const NOW_HOURS = 12;

@Component({
  selector: 'app-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss']
})
export class FiltersComponent implements OnInit {
  filters: ProgramFilter = {};
  dateFilters: number[] = [];
  nowFilter = false;

  @Output() filtersChanged = new EventEmitter<ProgramFilter>();
  LOC = Property.LOC;

  constructor(public scheduleService: ScheduleService, private settingsService: SettingsService) { }

  days: number[] = [];
  dayWeekday: {[_: string]: string} = {
    15: 'Friday',
    16: 'Saturday',
    17: 'Sunday',
    18: 'Monday',
    19: 'Tuesday'};

  ngOnInit(): void {
    const tzoffset: number = new Date().getTimezoneOffset();
    // Allows filtering for events from 2020-01-15T17:30 EST to
    // 2020-01-18T15:30 EST
    this.days = [16, 17, 18];
    if (tzoffset >= -1.5 * 60) {
      this.days.unshift(15);
    }
    if (tzoffset <= -4.5 * 60) {
      this.days.push(19);
    }
  }

  onChange(event:MouseEvent, prop:Property, value:string): void {
    let propList;
    switch (prop) {
      case Property.LOC:
        this.filters.loc = this.filters.loc || [];
        propList = this.filters.loc;
        break;
    }
    const target = (event.target as Element);
    const index = propList.indexOf(value);
    if(index > -1) {
      propList.splice(index, 1);
      target.classList.remove('active');
    } else {
      propList.push(value);
      target.classList.add('active');
    }
    this.filtersChanged.emit(this.filters);
  }
  onDate(value: number): void {
    this.dateFilters = this.dateFilters || [];
    const index = this.dateFilters.indexOf(value);
    if(index > -1) {
      this.dateFilters.splice(index, 1);
    } else {
      this.dateFilters.push(value);
    }
    this.nowFilter = false;
    this.updateDateFilters();
  }

  onNow(): void {
    this.dateFilters.length = 0;
    this.nowFilter = !this.nowFilter;
    this.updateDateFilters();
  }

  updateDateFilters(): void {
    if (this.nowFilter) {
      const now = this.settingsService.currentTime;
      this.filters.date=[{start: now, end: new Date(now.getTime() + NOW_HOURS * 60 * 60 * 1000), inclusive: true}];
    } else {
      this.filters.date = this.dateFilters.map(date => ({start: new Date(2021, 0, date), end: new Date(2021, 0, date + 1)}));
    }
    this.filtersChanged.emit(this.filters);
  }

}
