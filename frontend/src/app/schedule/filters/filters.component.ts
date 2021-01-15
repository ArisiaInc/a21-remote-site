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
  trackFilters: string[] = [];
  typeFilters: string[] = [];
  dateFilters: number[] = [];
  expanded = false;
  nowFilter = false;
  featuredFilter = false;
  captionedFilter = false;
  anyActive = false;
  collapsedFilters: {name: string, toggle: () => void, active?: boolean, always?: boolean}[] = [];

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
    this.update();
  }

  onTrack(value:string, event?: Event): void {
    event && event.stopPropagation();
    const index = this.trackFilters.indexOf(value);
    if(index > -1) {
      this.trackFilters.splice(index, 1);
    } else {
      this.trackFilters.push(value);
    }
    this.updateTagFilters();
  }

  clearTrack(event?: Event): void {
    event && event.stopPropagation();
    this.trackFilters.length = 0;
    this.updateTagFilters();
  }

  onType(value:string, event?: Event): void {
    event && event.stopPropagation();
    const index = this.typeFilters.indexOf(value);
    if(index > -1) {
      this.typeFilters.splice(index, 1);
    } else {
      this.typeFilters.push(value);
    }
    this.updateTagFilters();
  }

  clearType(event?: Event): void {
    event && event.stopPropagation();
    this.typeFilters.length = 0;
    this.updateTagFilters();
  }

  onDate(value: number, event?: Event): void {
    event && event.stopPropagation();
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

  onNow(event?: Event): void {
    event && event.stopPropagation();
    this.dateFilters = [];
    this.nowFilter = !this.nowFilter;
    this.updateDateFilters();
  }

  onFeatured(event?: Event): void {
    event && event.stopPropagation();
    this.featuredFilter = !this.featuredFilter;
    this.filters.featuredOnly = this.featuredFilter;
    this.update();
  }

  onCaptioned(event?: Event) {
    event && event.stopPropagation();
    this.captionedFilter = !this.captionedFilter;
    this.filters.captionedOnly = this.captionedFilter;
    this.update();
  }

  clearDate(event?: Event): void {
    event && event.stopPropagation();
    this.dateFilters = [];
    this.nowFilter = false;
    this.updateDateFilters();
  }

  clearAll(event?: Event): void {
    event && event.stopPropagation();
    this.dateFilters = [];
    this.trackFilters = [];
    this.typeFilters = [];
    this.nowFilter = false;
    this.captionedFilter = false;
    this.featuredFilter = false;
    this.filters.featuredOnly = this.featuredFilter;
    this.filters.captionedOnly = this.captionedFilter;
    this.updateDateFilters(false);
    this.updateTagFilters(false);
    this.update();
  }

  updateDateFilters(emit: boolean = true): void {
    if (this.nowFilter) {
      const now = this.settingsService.currentTime;
      this.filters.date=[{start: now, end: new Date(now.getTime() + NOW_HOURS * 60 * 60 * 1000), inclusive: true}];
    } else {
      this.filters.date = this.dateFilters.map(date => ({start: new Date(2021, 0, date), end: new Date(2021, 0, date + 1)}));
    }
    if (emit) {
      this.update();
    }
  }

  updateTagFilters(emit: boolean = true): void {
    this.filters.tracks = this.trackFilters;
    this.filters.types = this.typeFilters;
    if (emit) {
      this.update();
    }
  }

  update(): void {
    this.collapsedFilters = [];
    this.collapsedFilters.push({name: "Captioned", toggle: () => this.onCaptioned(), active: this.captionedFilter, always: true});
    this.collapsedFilters.push({name: "Featured", toggle: () => this.onFeatured(), active: this.featuredFilter, always: true});
    if (this.nowFilter) {
      this.collapsedFilters.push({name: "Now", toggle: () => this.onNow(), active: true});
    }
    for (const day of this.dateFilters) {
      this.collapsedFilters.push({name: this.dayWeekday[day], toggle: () => this.onDate(day), active: true});
    }
    for (const track of this.trackFilters) {
      this.collapsedFilters.push({name: track, toggle: () => this.onTrack(track), active: true});
    }
    for (const type of this.typeFilters) {
      this.collapsedFilters.push({name: type, toggle: () => this.onType(type), active: true});
    }
    this.anyActive = this.collapsedFilters.some(({active}) => active);
    this.filtersChanged.emit(this.filters);
  }
}
