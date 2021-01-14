import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { LayoutComponent } from './layout/layout.component';
import { AccountRoutingModule } from './account-routing.module';

import { SharedModule } from '@app/_components';
import { ForgotComponent } from './forgot/forgot.component';
import { PipesModule } from '../pipes/pipes.module';

@NgModule({
  declarations: [
    LoginComponent,
    LayoutComponent, ForgotComponent
  ],
  imports: [
    CommonModule,
    AccountRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    PipesModule
  ]
})
export class AccountModule { }
