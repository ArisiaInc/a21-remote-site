import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { InfodeskComponent } from '../infodesk/infodesk.component';
import { SafetyComponent } from '../safety/safety.component';
import { IrtComponent } from '../irt/irt.component';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {

    constructor(private modalService: NgbModal) { }

    ngOnInit(): void {
    }

    openInfodesk(): void {
	const modalRef = this.modalService.open(InfodeskComponent);
    }

    openSafety(): void {
	const modalRef = this.modalService.open(SafetyComponent);
	modalRef.closed.toPromise().then((result) => { if (result == "openIrt") { this.openIrt(); } });
    }

    openIrt(): void {
	const modalRef = this.modalService.open(IrtComponent);
	modalRef.closed.toPromise().then((result) => { if (result == "openSafety") { this.openSafety(); } });
    }
}
