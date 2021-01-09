import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-door-dragon',
  templateUrl: './door-dragon.component.html',
  styleUrls: ['./door-dragon.component.scss']
})
export class DoorDragonComponent {
  @Input() style?: string;
  linkText!: string;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit() {
    switch (this.style) {
      case 'programming':
      case 'social':
        this.linkText = 'Log in to Arisia';
        break;
      case 'discord':
        this.linkText = 'Continue to Discord';
        break;
      case 'reset':
        this.linkText = 'Continue to Registration page';
        break;
      default:
        this.linkText = '';
        break;
    }
  }
}
