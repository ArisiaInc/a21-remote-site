import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from './button/button.component';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';
import { LinkComponent } from './link/link.component';



@NgModule({
  declarations: [
    ButtonComponent,
    BreadcrumbComponent,
    LinkComponent,
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ButtonComponent,
    BreadcrumbComponent,
    LinkComponent
  ]
})
export class SharedModule { }
