import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { Ribbon } from '@app/_models/ribbon';

@Component({
  selector: 'app-ribbon-modal',
  templateUrl: './ribbon-modal.component.html',
  styleUrls: ['./ribbon-modal.component.scss']
})
export class RibbonModalComponent implements OnInit {
  ribbon$!: Observable<Ribbon>;

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {

  }

}
