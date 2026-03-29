export interface FilterCriteria {
  field: string;
  operator: 'EQUAL' | 'NOT_EQUAL' | 'LIKE' | 'GREATER_THAN' | 'LESS_THAN' | 'BETWEEN' | 'IN';
  value: any;
  valueTo?: any;
}

export interface SortCriteria {
  field: string;
  direction: 'ASC' | 'DESC';
}

export interface FilterRequest {
  filters: FilterCriteria[];
  sorts: SortCriteria[];
  page: number;
  size: number;
  cursor?: string;
}

export interface ColumnDef {
  field: string;
  header: string;
  pipe?: string;
  sortable?: boolean;
  width?: string;
}

export interface FilterField {
  field: string;
  label: string;
  type: 'text' | 'select' | 'date' | 'daterange';
  options?: { value: any; label: string }[];
}
