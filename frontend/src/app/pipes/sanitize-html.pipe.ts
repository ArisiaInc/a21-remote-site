import { Pipe, PipeTransform } from '@angular/core';

function getText(node: Node): string {
  switch (node.nodeType) {
    case Node.ELEMENT_NODE:
      const result = Array.prototype.map.call(node.childNodes, getText).join('');
      const tagName = (node as Element).tagName
      if (tagName === 'I') {
        return `<i>${result}</i>`;
      } else if (tagName === 'B') {
        return `<b>${result}</b>`;
      } else {
        return result;
      }
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
      return node.nodeValue || '';
    default:
      return '';
  }
}

@Pipe({
  name: 'sanitizeHtml'
})
export class SanitizeHtmlPipe implements PipeTransform {
  transform(value?: string): string {
    if (!value) {
      return '';
    }
    const tmp = document.createElement('DIV');
    tmp.innerHTML = value;
    return getText(tmp);
  }
}
