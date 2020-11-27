import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LandingComponent } from './landing/landing.component';
import { AuthGuard } from './_helpers';
import { ProgrammingComponent } from './programming/programming.component';
import { GamingComponent } from './gaming/gaming.component';

const accountModule = () => import('./account/account.module').then(x => x.AccountModule);
const scheduleModule = () => import('./schedule/schedule.module').then(x => x.ScheduleModule);
const mapModule = () => import('./map/map.module').then(x => x.MapModule);

const routes: Routes = [
  {path: '', component: LandingComponent, canActivate: [AuthGuard]},
  {path: 'account', loadChildren: accountModule},
  {path: 'schedule', loadChildren: scheduleModule, canActivate: [AuthGuard]},
  {path: 'map', loadChildren: mapModule, canActivate: [AuthGuard]},
  {path: 'programming', component: ProgrammingComponent, canActivate: [AuthGuard]},
  {path: 'gaming', component: GamingComponent, canActivate: [AuthGuard]},

  //redirect home
  {path: '**', redirectTo: '/map'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
