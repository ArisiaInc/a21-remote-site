import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-irt',
  templateUrl: './irt.component.html',
  styleUrls: ['./irt.component.scss']
})
export class IrtComponent implements OnInit {
    constructor(public activeModal: NgbActiveModal) { }

    ngOnInit(): void {
    }
}
