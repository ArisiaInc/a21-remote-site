import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { ButtonComponent } from './button/button.component';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';
import { LinkComponent } from './link/link.component';
import { CreatorCardComponent } from './creator-card/creator-card.component';
import { CreatorListComponent } from './creator-list/creator-list.component';
import { RouterModule } from '@angular/router';
import { CreatorPageComponent } from './creator-page/creator-page.component';
import { CalloutBoxComponent } from './callout-box/callout-box.component';
import { SettingsComponent } from './settings/settings.component';
import { StarComponent } from './star/star.component';

@NgModule({
  declarations: [
    ButtonComponent,
    BreadcrumbComponent,
    LinkComponent,
    CreatorCardComponent,
    CreatorListComponent,
    CreatorPageComponent,
    CalloutBoxComponent,
    SettingsComponent,
    StarComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    NgbModule,
  ],
  exports: [
    ButtonComponent,
    BreadcrumbComponent,
    LinkComponent,
    CreatorCardComponent,
    CreatorListComponent,
    CreatorPageComponent,
    CalloutBoxComponent,
    SettingsComponent,
    StarComponent,
  ]
})
export class SharedModule { }
