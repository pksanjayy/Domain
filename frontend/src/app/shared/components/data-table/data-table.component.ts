import { Component, Input, Output, EventEmitter, OnInit, OnChanges, OnDestroy, SimpleChanges, ViewChild, ContentChild, TemplateRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ColumnDef, FilterField, FilterCriteria, SortCriteria, FilterRequest, ApiResponse, PageResponse } from '../../../core/models';
import { BranchContextService } from '../../../core/services/branch-context.service';

@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss'],
})
export class DataTableComponent implements OnInit, OnChanges, OnDestroy {
  @Input() columns: ColumnDef[] = [];
  @Input() filterConfig: FilterField[] = [];
  @Input() apiUrl: string = '';
  @Input() defaultSort: SortCriteria = { field: 'id', direction: 'DESC' };

  @Output() rowClick = new EventEmitter<any>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ContentChild('actionTemplate') actionTemplate!: TemplateRef<any>;

  dataSource = new MatTableDataSource<any>([]);
  displayedColumns: string[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;
  isEmpty = false;

  private currentFilters: FilterCriteria[] = [];
  private currentSorts: SortCriteria[] = [];
  private destroy$ = new Subject<void>();

  constructor(private http: HttpClient, private branchContext: BranchContextService) {}

  ngOnInit(): void {
    this.displayedColumns = this.columns.map((c) => c.field);
    this.currentSorts = [this.defaultSort];
    this.loadData();

    // Reload data when branch context changes
    this.branchContext.activeBranchId$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadData();
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['apiUrl'] && !changes['apiUrl'].firstChange) {
      this.loadData();
    }
  }

  onFilterChange(filters: FilterCriteria[]): void {
    this.currentFilters = filters;
    this.pageIndex = 0;
    this.loadData();
  }

  onSortChange(sort: Sort): void {
    if (sort.direction) {
      this.currentSorts = [
        { field: sort.active, direction: sort.direction.toUpperCase() as 'ASC' | 'DESC' },
      ];
    } else {
      this.currentSorts = [this.defaultSort];
    }
    this.loadData();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadData();
  }

  onRowClicked(row: any): void {
    this.rowClick.emit(row);
  }

  loadData(): void {
    if (!this.apiUrl) return;

    this.isLoading = true;

    // Build filters with branch context
    const filters = [...this.currentFilters];
    const branchId = this.branchContext.getActiveBranchId();
    if (branchId !== null) {
      // Remove any existing branchId filter
      const idx = filters.findIndex(f => f.field === 'branch.id');
      if (idx >= 0) filters.splice(idx, 1);
      filters.push({ field: 'branch.id', operator: 'EQUAL', value: String(branchId) });
    }

    const request: FilterRequest = {
      filters: filters,
      sorts: this.currentSorts,
      page: this.pageIndex,
      size: this.pageSize,
    };

    this.http
      .post<ApiResponse<PageResponse<any>>>(this.apiUrl, request)
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.data.content;
          this.totalElements = response.data.totalElements;
          this.isEmpty = response.data.content.length === 0;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.isEmpty = true;
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
