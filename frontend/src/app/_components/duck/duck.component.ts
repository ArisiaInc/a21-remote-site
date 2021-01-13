import { Component, OnInit, Input } from '@angular/core';
import { Duck } from '@app/_models';
import { DuckService } from '@app/_services/duck.service';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-duck',
  templateUrl: './duck.component.html',
  styleUrls: ['./duck.component.scss']
})
export class DuckComponent implements OnInit {
  @Input() duckId!: number;
  duck$!: Observable<Duck|undefined>;

  constructor(private duckService: DuckService,
    private router: Router) { }

  ngOnInit(): void {
    console.log('duck is init!')
    this.duck$ = this.duckService.get_duck(this.duckId).pipe(
      tap(d => console.log(d))
    );
  }

  awardDuck(id: number, link: string) {
    this.duckService.award_duck(id).subscribe(r => console.log('duck got awarded?'));
    this.router.navigate([link]);
  }

}
