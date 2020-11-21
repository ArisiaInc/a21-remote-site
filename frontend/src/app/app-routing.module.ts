import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LandingComponent } from './landing/landing.component';
import { AuthGuard } from './_helpers';

const accountModule = () => import('./account/account.module').then(x => x.AccountModule);
const scheduleModule = () => import('./schedule/schedule.module').then(x => x.ScheduleModule);
const mapModule = () => import('./map/map.module').then(x => x.MapModule);

const routes: Routes = [
  {path: '', component: LandingComponent, canActivate: [AuthGuard]},
  {path: 'account', loadChildren: accountModule},
  {path: 'schedule', loadChildren: scheduleModule, canActivate: [AuthGuard]},
  {path: 'map', loadChildren: mapModule, canActivate: [AuthGuard]},

  //redirect home
  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
