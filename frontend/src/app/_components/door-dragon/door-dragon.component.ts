import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-door-dragon',
  templateUrl: './door-dragon.component.html',
  styleUrls: ['./door-dragon.component.scss']
})
export class DoorDragonComponent {
  @Input() style?: string;
  actionButtonActivated = true;
  linkText!: string;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit() {
    switch (this.style) {
      case 'programming':
      case 'social':
      case 'login-for-zoom':
        this.linkText = 'Log in to Arisia';
        break;
      case 'discord':
        this.linkText = 'Continue to Discord';
        break;
      case 'reset':
        this.linkText = 'Continue to Registration page';
        break;
      case 'zoom-room-closed':
        this.actionButtonActivated = false;
        break;
      default:
        this.linkText = '';
        break;
    }
  }
}
