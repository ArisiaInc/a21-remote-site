import { Component, OnInit, Input } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { Router } from '@angular/router';

import { Duck } from '@app/_models';
import { DuckService } from '@app/_services/duck.service';
import { AccountService } from '@app/_services/account.service';

@Component({
  selector: 'app-duck',
  templateUrl: './duck.component.html',
  styleUrls: ['./duck.component.scss']
})
export class DuckComponent implements OnInit {
  @Input() duckId!: number;
  @Input() hideIfFound = true;
  show$!: Observable<boolean>;
  duck$: Observable<Duck|undefined>;

  constructor(private accountService: AccountService,
              private duckService: DuckService,
              private router: Router) {
    console.log('duck is init!')
    this.duck$ = this.duckService.getDuck(this.duckId).pipe(
      tap(d => console.log(d))
    );
  }

  ngOnInit(): void {
    if (this.hideIfFound) {
      this.show$ = this.accountService.user$.pipe(
        map(user => !(user && user.ducks.includes(this.duckId))),
      );
    } else {
      this.show$ = of(true);
    }
  }

  awardDuck(id: number, link: string) {
    this.accountService.awardDuck(id).subscribe(result => console.log(`duck got awarded: ${result}`));
    this.router.navigate([link]);
  }

}
