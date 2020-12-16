import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LandingComponent } from './landing/landing.component';
import { SharedModule } from './_components';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ProgrammingComponent } from './programming/programming.component';
import { GamingComponent } from './gaming/gaming.component';
import { NavigationComponent } from './navigation/navigation.component';
import { NavigationLinkComponent } from './navigation/navigation-link/navigation-link.component';
import { InfodeskComponent } from './infodesk/infodesk.component';
import { SafetyComponent } from './safety/safety.component';
import { IrtComponent } from './irt/irt.component';

@NgModule({
  declarations: [
    AppComponent,
    LandingComponent,
    ProgrammingComponent,
    GamingComponent,
    NavigationComponent,
    NavigationLinkComponent,
    InfodeskComponent,
    SafetyComponent,
    IrtComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    SharedModule,
    HttpClientModule,
    NgbModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [InfodeskComponent, SafetyComponent, IrtComponent]
})
export class AppModule { }
