import { pipe, OperatorFunction } from 'rxjs';
import { map } from 'rxjs/operators';

export interface Initial {
  lower: string;
  upper: string;
  active: boolean;
}

export interface HasName {
  name: string;
}

export function createInitials(): OperatorFunction<HasName[], Initial[]> {
  return map((source: HasName[]) => {
    const initialMap: {[lower: string]: Initial} = {};

    for(let letter = 1; letter <= 26; letter++) {
      const lower = String.fromCharCode(letter + 96);
      const upper = String.fromCharCode(letter + 64);
      initialMap[lower] = {lower, upper, active: false};
    }

    source.forEach(person => {
      const lower = person.name[0].toLowerCase();
      if (initialMap[lower]) {
        initialMap[lower].active = true;
      } else {
        const upper = person.name[0].toUpperCase();
        initialMap[lower] = {lower, upper, active: true};
      }
    });

    const result = Object.values(initialMap);
    result.sort((a, b) => a.lower.localeCompare(b.lower));
    return result;
  });
}


export function searchPrefixCaseInsensitive<T extends HasName>(search: string) : OperatorFunction<T[], T[]> {
  const regexp = new RegExp('^'+search, 'i');
  return map(list => list.filter(item => item.name.match(regexp)));
}
