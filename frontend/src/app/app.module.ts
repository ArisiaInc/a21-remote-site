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
import { RoomComponent } from './programming/room/room.component';
import { PipesModule } from './pipes/pipes.module';
import { ArtshowComponent } from './artshow/artshow.component';
import { ArtistComponent } from './artshow/artist/artist.component';
import { DealersComponent } from './dealers/dealers.component';
import { DealerComponent } from './dealers/dealer/dealer.component';
import { TabletopComponent } from './gaming/tabletop/tabletop.component';
import { LarpComponent } from './gaming/larp/larp.component';
import { VideoComponent } from './gaming/video/video.component';
import { SpecialComponent } from './gaming/special/special.component';
import { SignupComponent } from './gaming/signup/signup.component';
import { DropinComponent } from './gaming/dropin/dropin.component';
import { UserComponent } from './user/user.component';
import { HelpComponent } from './help/help.component';
import { FooterComponent } from './footer/footer.component';
import { SetupComponent } from './help/setup/setup.component';
import { PerformanceComponent } from './performance/performance.component';
import { SocialComponent } from './social/social.component';
import { DuckListComponent } from './user/duck-list/duck-list.component';
import { ProfileDuckComponent } from './user/profile-duck/profile-duck.component';

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
    RoomComponent,
    ArtshowComponent,
    ArtistComponent,
    DealersComponent,
    DealerComponent,
    TabletopComponent,
    LarpComponent,
    VideoComponent,
    SpecialComponent,
    SignupComponent,
    DropinComponent,
    UserComponent,
    HelpComponent,
    FooterComponent,
    SetupComponent,
    PerformanceComponent,
    SocialComponent,
    DuckListComponent,
    ProfileDuckComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    SharedModule,
    HttpClientModule,
    NgbModule,
    PipesModule,
  ],
  bootstrap: [AppComponent],
  entryComponents: [InfodeskComponent, SafetyComponent, IrtComponent],
})
export class AppModule { }
