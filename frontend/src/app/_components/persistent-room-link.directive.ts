import { Directive, Input, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { AccountService, ZoomRoomsService } from '@app/_services';
import { DoorDragonComponent } from './door-dragon/door-dragon.component';

@Directive({
  selector: '[appPersistentRoomLink]'
})
export class PersistentRoomLinkDirective {
  @Input('appPersistentRoomLink') room = '';

  constructor(private accountService: AccountService,
              private zoomRoomsService: ZoomRoomsService,
              private router: Router,
              private modalService: NgbModal) {
  }

  /** @nodoc */
  @HostListener('click')
  onClick(): boolean {
    if (this.accountService.user) {
      this.zoomRoomsService.checkRoom(this.room).subscribe(
        running => {
          if (running) {
            window.open(this.zoomRoomsService.getRoomUrl(this.room), '_blank');
          } else {
            const modalRef = this.modalService.open(DoorDragonComponent, {size: 'lg'});
            const doorDragon = modalRef.componentInstance as DoorDragonComponent;
            doorDragon.style = 'zoom-room-closed';
          }
        });
    } else {
      const modalRef = this.modalService.open(DoorDragonComponent, {size: 'lg'});
      const doorDragon = modalRef.componentInstance as DoorDragonComponent;
      doorDragon.style = 'social';
      modalRef.closed.subscribe(
        result => {
          if (result == "success") {
            this.router.navigate(['/account/login'], {queryParams: {returnUrl: this.router.url}});
          }
        });
    }
    return true;
  }
}
