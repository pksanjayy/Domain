import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { AdminService } from '../../services/admin.service';
import { AuditLogDto, ADMIN_MODULES } from '../../models/admin.model';

@Component({
  selector: 'app-audit-log',
  templateUrl: './audit-log.component.html',
  styleUrls: ['./audit-log.component.scss'],
})
export class AuditLogComponent implements OnInit {
  displayedColumns = [
    'performedAt', 'performedByUsername', 'entityName', 'action',
    'entityId', 'changes'
  ];
  dataSource = new MatTableDataSource<AuditLogDto>([]);
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  isLoading = false;

  filterForm!: FormGroup;
  modules = ADMIN_MODULES;
  actionTypes = ['CREATE', 'UPDATE', 'DELETE', 'READ'];

  expandedRow: AuditLogDto | null = null;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.filterForm = this.fb.group({
      search: [''],
      from: [null],
      to: [null],
      module: [''],
      action: [''],
      userId: [null],
    });
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.isLoading = true;
    const filters = this.filterForm.value;
    const params: any = {
      page: this.pageIndex,
      size: this.pageSize,
    };

    if (filters.from) {
      params.from = new Date(filters.from).toISOString();
    }
    if (filters.to) {
      params.to = new Date(filters.to).toISOString();
    }
    if (filters.search) params.search = filters.search;
    if (filters.module) params.module = filters.module;
    if (filters.action) params.action = filters.action;
    if (filters.userId) params.userId = filters.userId;

    this.adminService.getAuditLogs(params).subscribe({
      next: (res) => {
        this.dataSource.data = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
      },
      error: () => (this.isLoading = false),
    });
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadAuditLogs();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.loadAuditLogs();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAuditLogs();
  }

  toggleRow(row: AuditLogDto): void {
    this.expandedRow = this.expandedRow === row ? null : row;
  }

  getActionColor(action: string): string {
    const map: Record<string, string> = {
      CREATE: '#2e7d32',
      UPDATE: '#1565c0',
      DELETE: '#c62828',
      READ: '#616161',
    };
    return map[action] || '#666';
  }

  getActionIcon(action: string): string {
    const map: Record<string, string> = {
      CREATE: 'add_circle',
      UPDATE: 'edit',
      DELETE: 'delete',
      READ: 'visibility',
    };
    return map[action] || 'info';
  }

  exportToExcel(): void {
    const filters = this.filterForm.value;
    const params: any = {};
    if (filters.from) params.from = new Date(filters.from).toISOString();
    if (filters.to) params.to = new Date(filters.to).toISOString();
    if (filters.module) params.module = filters.module;
    if (filters.action) params.action = filters.action;

    this.adminService.exportAuditLogs(params).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `audit-log-${new Date().toISOString().slice(0, 10)}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
    });
  }

  parseJson(value: string | null): any {
    if (!value) return null;
    try {
      return JSON.parse(value);
    } catch {
      return value;
    }
  }

  getChangedKeys(oldVal: string | null, newVal: string | null): string[] {
    const oldObj = this.parseJson(oldVal);
    const newObj = this.parseJson(newVal);
    if (!oldObj || !newObj || typeof oldObj !== 'object' || typeof newObj !== 'object') return [];

    const keys = new Set([...Object.keys(oldObj), ...Object.keys(newObj)]);
    return Array.from(keys).filter(k => JSON.stringify(oldObj[k]) !== JSON.stringify(newObj[k]));
  }
}
