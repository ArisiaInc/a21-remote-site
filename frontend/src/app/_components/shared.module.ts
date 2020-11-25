import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from './button/button.component';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';



@NgModule({
  declarations: [
    ButtonComponent,
    BreadcrumbComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ButtonComponent,
    BreadcrumbComponent
  ]
})
export class SharedModule { }
