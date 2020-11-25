import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { LayoutComponent } from './layout/layout.component';
import { AccountRoutingModule } from './account-routing.module';

import { SharedModule } from '@app/_components';

@NgModule({
  declarations: [
    LoginComponent, 
    LayoutComponent,
  ],
  imports: [
    CommonModule,
    AccountRoutingModule,
    SharedModule
  ]
})
export class AccountModule { }
