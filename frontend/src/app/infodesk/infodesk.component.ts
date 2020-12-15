import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-infodesk',
  templateUrl: './infodesk.component.html',
  styleUrls: ['./infodesk.component.scss']
})
export class InfodeskComponent implements OnInit {
    constructor(public activeModal: NgbActiveModal) { }

    ngOnInit(): void {
    }
}
