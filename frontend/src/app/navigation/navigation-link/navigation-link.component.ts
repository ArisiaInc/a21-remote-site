import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'app-navigation-link',
    templateUrl: './navigation-link.component.html',
    styleUrls: ['./navigation-link.component.scss']
})
export class NavigationLinkComponent implements OnInit {
    @Input() link!: string;
    @Input() title!: string;

    constructor() {
    }

    ngOnInit(): void {
	// assert (this.link !== undefined);
	// assert (this.title !== undefined);
    }
}
