import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { ScheduleService } from '@app/_services';
import { ProgramFilter } from '@app/_models';

enum Property {
  LOC,
};

@Component({
  selector: 'app-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss']
})
export class FiltersComponent implements OnInit {
  filters: ProgramFilter = {};
  dateFilters: number[] = [];

  @Output() filtersChanged = new EventEmitter<ProgramFilter>();
  LOC = Property.LOC;

  constructor(public scheduleService: ScheduleService) { }

  ngOnInit(): void {
  }

  onChange(event:MouseEvent, prop:Property, value:string) {
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
    console.log(this.filters)
    console.log(target.classList)
    this.filtersChanged.emit(this.filters);
  }
  onChangeDate(event:MouseEvent, value: number) {
    this.dateFilters = this.dateFilters || [];
    const target = (event.target as Element);
    const index = this.dateFilters.indexOf(value);
    if(index > -1) {
      this.dateFilters.splice(index, 1);
      target.classList.remove('active');
    } else {
      this.dateFilters.push(value);
      target.classList.add('active');
    }
    this.filters.date = this.dateFilters.map(date => ({start: new Date(2021, 0, date), end: new Date(2021, 0, date + 1)}));
    this.filtersChanged.emit(this.filters);
  }

}
