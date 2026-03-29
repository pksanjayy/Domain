import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { FilterField, FilterCriteria } from '../../../core/models';

@Component({
  selector: 'app-filter-panel',
  templateUrl: './filter-panel.component.html',
  styleUrls: ['./filter-panel.component.scss'],
})
export class FilterPanelComponent implements OnInit {
  @Input() fields: FilterField[] = [];
  @Output() filterChange = new EventEmitter<FilterCriteria[]>();

  filterForm!: FormGroup;
  isExpanded = false;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    const group: any = {};
    this.fields.forEach((field) => {
      if (field.type === 'daterange') {
        group[field.field + '_from'] = [null];
        group[field.field + '_to'] = [null];
      } else {
        group[field.field] = [''];
      }
    });
    this.filterForm = this.fb.group(group);
  }

  applyFilters(): void {
    const filters: FilterCriteria[] = [];

    this.fields.forEach((field) => {
      if (field.type === 'daterange') {
        const from = this.filterForm.get(field.field + '_from')?.value;
        const to = this.filterForm.get(field.field + '_to')?.value;
        if (from && to) {
          filters.push({
            field: field.field,
            operator: 'BETWEEN',
            value: from,
            valueTo: to,
          });
        }
      } else {
        const value = this.filterForm.get(field.field)?.value;
        if (value !== null && value !== '' && value !== undefined) {
          filters.push({
            field: field.field,
            operator: field.type === 'text' ? 'LIKE' : 'EQUAL',
            value,
          });
        }
      }
    });

    this.filterChange.emit(filters);
  }

  resetFilters(): void {
    this.filterForm.reset();
    this.filterChange.emit([]);
  }

  toggleExpand(): void {
    this.isExpanded = !this.isExpanded;
  }
}
