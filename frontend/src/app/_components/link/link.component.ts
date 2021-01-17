import { Component, OnInit, Input, HostBinding } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-link',
  templateUrl: './link.component.html',
  styleUrls: ['./link.component.scss']
})
export class LinkComponent implements OnInit {
  @Input() color = "white";
  @Input() backgroundColor = "#c4c4c4";
  @Input() backgroundImage?: string;
  @Input() centerLine = "50%";
  @Input() horizontalNudge = "";
  @Input() url!: string;
  @HostBinding('class.circle') @Input() circle = false;
  @HostBinding('style') style = '';

  nudgeStyle = '';

  constructor(private router: Router) { }

  ngOnInit(): void {
    const background = this.backgroundImage ?
      `background:url(${this.backgroundImage}) no-repeat;background-size:contain;background-position:center` :
      `background-color:${this.backgroundColor}`;
    this.style = `${background};color:${this.color}`;
    this.nudgeStyle = this.horizontalNudge ? `;left:${this.horizontalNudge}` : '';
  }

  navigate() {
    if (this.url) {
      this.router.navigateByUrl(this.url);
    }
  }


}
