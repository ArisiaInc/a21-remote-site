import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { ProgramFilter } from '@app/_models';

enum Property {
  LOC,
  DATE,
};

@Component({
  selector: 'app-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss']
})
export class FiltersComponent implements OnInit {
  filters: ProgramFilter = {};
  @Output() filtersChanged = new EventEmitter<ProgramFilter>();
  DATE = Property.DATE;
  LOC = Property.LOC;

  constructor() { }

  ngOnInit(): void {
  }

  onChange(event:MouseEvent, prop:Property, value:string) {
    let propList;
    switch (prop) {
      case Property.LOC:
        this.filters.loc = this.filters.loc || [];
        propList = this.filters.loc;
        break;
      case Property.DATE:
        this.filters.date = this.filters.date || []
        propList = this.filters.date;
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

}
