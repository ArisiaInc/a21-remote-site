import { Component, OnInit } from '@angular/core';
import { Creator } from '@app/_models';
import { Observable } from 'rxjs';
import { CreatorService } from '@app/_services';

@Component({
  selector: 'app-dealers',
  templateUrl: './dealers.component.html',
  styleUrls: ['./dealers.component.scss']
})
export class DealersComponent implements OnInit {
  dealers$!: Observable<Creator[]>;

  constructor(private creatorService: CreatorService) { }

  ngOnInit(): void {
    this.dealers$ = this.creatorService.get_dealers();
  }

}
