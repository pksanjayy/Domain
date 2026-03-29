import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'mask' })
export class MaskPipe implements PipeTransform {
  transform(value: string, visibleChars: number = 4, maskChar: string = '*'): string {
    if (!value || value.length <= visibleChars) return value;

    const visible = value.slice(-visibleChars);
    const masked = maskChar.repeat(value.length - visibleChars);
    return masked + visible;
  }
}
