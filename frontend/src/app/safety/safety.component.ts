import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-safety',
  templateUrl: './safety.component.html',
  styleUrls: ['./safety.component.scss']
})
export class SafetyComponent implements OnInit {
    constructor(public activeModal: NgbActiveModal) { }

    ngOnInit(): void {
    }

    switchToIrt(): void {
	this.activeModal.close("openIrt");
    }

    openTheWatch(): void {
	window.open("https://arisia.org/TheWatch", "_blank");
    }
}
