import { Pipe, PipeTransform } from '@angular/core';
import {ProgramItem, ProgramPerson} from '@app/_models';

@Pipe({
  name: 'programItemPanelists'
})
export class ProgramItemPanelistsPipe implements PipeTransform {

  transform(event: ProgramItem): string {
    return event.people.map((obj) => {
      const person = obj as ProgramPerson;
      if (typeof(person.name) === 'string') {
        return person.name;
      } else {
        return person.name.join(' ');
      }
    }).join(', ');
  }

}
