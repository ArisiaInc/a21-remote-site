import { Pipe, PipeTransform } from '@angular/core';

const URL_REGEXP = RegExp(
  '(https?://' +
    '(?:[a-z0-9.~_!$&\'()*+,;=-]|%[0-9a-f][0-9a-f])+' + // hostname characters, including hex codes.
    '(?:/(?:[a-z0-9.~_!$&\'()*+,;=:@-]|%[0-9a-f][0-9a-f])*)*' + // path components
    '(?:[a-z0-9~_/]))' + // People often follow URLs with punctuation. This will prevent that from getting consumed.
    '|([a-z~_*+=-]+(?:\\.[a-z0-9~_*+=-]+)*\\.(?:com|net|org))' + // Loose hostname. Much stricter list of accepted characters.
    '|([a-z0-9+_~.-]+@[a-z~_*+=-]+(?:\\.[a-z0-9~_*+=-]+)*\\.(?:com|net|org))', // Email addresses. Much stricter list of accepted characters.
  'gi');

function replacer(match: string, p1?: string, p2?: string, p3?:string) {
  const url = p1 || (p2 && `http://${p2}`) || (p3 && `mailto:${p3}`);
  return `<a href="${url}" target="_blank">${match}</a>`;
}

@Pipe({
  name: 'linkify'
})
export class LinkifyPipe implements PipeTransform {
  transform(value: string): string {
    return value.replace(URL_REGEXP, replacer);
  }
}
