import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { ButtonComponent } from './button/button.component';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';
import { LinkComponent } from './link/link.component';
import { SettingsComponent } from './settings/settings.component';

@NgModule({
  declarations: [
    ButtonComponent,
    BreadcrumbComponent,
    LinkComponent,
    SettingsComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
  ],
  exports: [
    ButtonComponent,
    BreadcrumbComponent,
    LinkComponent,
    SettingsComponent,
  ]
})
export class SharedModule { }
