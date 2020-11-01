import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { ProgramFilter } from '@app/_models';

@Component({
  selector: 'app-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss']
})
export class FiltersComponent implements OnInit {
  filters: ProgramFilter = new ProgramFilter();
  @Output() filtersChanged = new EventEmitter<ProgramFilter>();

  constructor() { }

  ngOnInit(): void {
  }

  onChange(event, prop, value) {
    this.filters[prop] = this.filters[prop] || []
    const index = this.filters[prop].indexOf(value)
    if(index > -1) {
      this.filters[prop].splice(index, 1)
      if (this.filters[prop].length == 0) {
        delete this.filters[prop]
      }
      event.target.classList.remove('active')
    } else {
      this.filters[prop].push(value);
      event.target.classList.add('active')
    }
    console.log(this.filters)
    console.log(event.target.classList)
    this.filtersChanged.emit(this.filters);
  }

}
