import { Directive, Input, HostListener } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { AccountService } from '@app/_services';
import { DoorDragonComponent } from './door-dragon/door-dragon.component';

@Directive({
  selector: '[appLoginGuardedLink]'
})
export class LoginGuardedLinkDirective {
  private commands: any[] = [];

  /**
   * Commands to pass to {@link Router#createUrlTree Router#createUrlTree}.
   *   - **array**: commands to pass to {@link Router#createUrlTree Router#createUrlTree}.
   *   - **string**: shorthand for array of commands with just the string, i.e. `['/route']`
   *   - **null|undefined**: shorthand for an empty array of commands, i.e. `[]`
   * @see {@link Router#createUrlTree Router#createUrlTree}
   */
  @Input()
  set appLoginGuardedLink(commands: any[]|string|null|undefined) {
    if (commands != null) {
      this.commands = Array.isArray(commands) ? commands : [commands];
    } else {
      this.commands = [];
    }
  }

  @Input('appDoorDragonStyle') style?: string;

  constructor(private accountService: AccountService,
              private router: Router,
              private modalService: NgbModal) {
  }

  /** @nodoc */
  @HostListener('click')
  onClick(): boolean {
    if (this.accountService.user) {
      this.router.navigateByUrl(this.urlTree);
    } else {
      const modalRef = this.modalService.open(DoorDragonComponent, {size: 'lg'});
      const doorDragon = modalRef.componentInstance as DoorDragonComponent;
      doorDragon.style = this.style;
      doorDragon.url = this.urlTree.toString();
    }
    return true;
  }

  get urlTree(): UrlTree {
    return this.router.createUrlTree(this.commands);
  }
}
