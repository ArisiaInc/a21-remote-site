import { Pipe, PipeTransform } from '@angular/core';
import { ScheduleEvent, SchedulePerson } from '@app/_services';

@Pipe({
  name: 'programItemPanelists'
})
export class ProgramItemPanelistsPipe implements PipeTransform {

  transform(event: ScheduleEvent): string {
    return event.people.map(
      (personData: {person: SchedulePerson, isModerator: boolean}) =>
        personData.person.name + (personData.isModerator ? ' (moderator)' : '')
    ).join(', ');
  }

}
