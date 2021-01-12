import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { Duck } from '@app/_models';
import { DuckService } from '@app/_services/duck.service';
import { map, tap } from 'rxjs/operators';

@Component({
  selector: 'app-duck-list',
  templateUrl: './duck-list.component.html',
  styleUrls: ['./duck-list.component.scss']
})
export class DuckListComponent implements OnInit {
  ducks$!: Observable<Duck[]>;
  @Input() duckIds: number[] = [];
  @Input() isSelf: boolean = false;


  constructor(private duckService: DuckService) { }

  ngOnInit(): void {
    this.ducks$ = this.duckService.get_ducks().pipe(
      map( ducks => ducks.map(duck => {
        duck.hidden = !this.duckIds.includes(duck.id);
        return duck;
      }).filter(duck => this.isSelf || !duck.hidden)),
    );
  }

}
