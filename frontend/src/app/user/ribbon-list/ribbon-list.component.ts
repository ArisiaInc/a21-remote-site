import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { Ribbon } from '@app/_models/ribbon';
import { RibbonService } from '@app/_services/ribbon.service';

@Component({
  selector: 'app-ribbon-list',
  templateUrl: './ribbon-list.component.html',
  styleUrls: ['./ribbon-list.component.scss']
})
export class RibbonListComponent implements OnInit {
  ribbons$!: Observable<Ribbon[]>;
  @Input() ribbonIds: number[] = [];

  constructor(private ribbonService: RibbonService) { }

  ngOnInit(): void {
    this.ribbons$ = this.ribbonService.getRibbonsById(this.ribbonIds);
  }

}
