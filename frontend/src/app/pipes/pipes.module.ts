import { NgModule } from '@angular/core';

import { ProgramItemPanelistsPipe } from './program-item-panelists.pipe';
import { SanitizeHtmlPipe } from './sanitize-html.pipe';
import { LinkifyPipe } from './linkify.pipe';

@NgModule({
  declarations: [
    ProgramItemPanelistsPipe,
    SanitizeHtmlPipe,
    LinkifyPipe,
  ],
  providers: [
    ProgramItemPanelistsPipe,
    SanitizeHtmlPipe,
    LinkifyPipe,
  ],
  exports: [
    ProgramItemPanelistsPipe,
    SanitizeHtmlPipe,
    LinkifyPipe,
  ],
  imports: [
  ]
})
export class PipesModule {}
