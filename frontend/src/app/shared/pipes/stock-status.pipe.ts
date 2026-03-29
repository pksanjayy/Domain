import { Pipe, PipeTransform } from '@angular/core';

const STATUS_LABELS: Record<string, string> = {
  'IN_TRANSIT': 'In Transit',
  'IN_STOCK': 'In Stock',
  'PDI_PENDING': 'PDI Pending',
  'PDI_PASSED': 'PDI Passed',
  'PDI_FAILED': 'PDI Failed',
  'AVAILABLE': 'Available',
  'RESERVED': 'Reserved',
  'SOLD': 'Sold',
  'DELIVERED': 'Delivered',
};

@Pipe({ name: 'stockStatus' })
export class StockStatusPipe implements PipeTransform {
  transform(value: string): string {
    return STATUS_LABELS[value] || value?.replace(/_/g, ' ') || '';
  }
}
