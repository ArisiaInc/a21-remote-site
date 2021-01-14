import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { DuckState } from '@app/_models';
import { DuckService } from '@app/_services/duck.service';
import { map, tap } from 'rxjs/operators';

@Component({
  selector: 'app-duck-list',
  templateUrl: './duck-list.component.html',
  styleUrls: ['./duck-list.component.scss']
})
export class DuckListComponent implements OnInit {
  ducks$!: Observable<DuckState[]>;
  @Input() duckIds: number[] = [];
  @Input() isSelf: boolean = false;


  constructor(private duckService: DuckService) { }

  ngOnInit(): void {
    this.ducks$ = this.duckService.getDuckStates(new Set(this.duckIds), {includeHidden: this.isSelf});
  }
}
