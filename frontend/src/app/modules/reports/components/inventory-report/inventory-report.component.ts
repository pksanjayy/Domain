import { Component, OnInit } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';
import { ReportsService } from '../../services/reports.service';

@Component({
  selector: 'app-inventory-report',
  template: `
    <div class="report-container">
      <!-- Summary Cards -->
      <div class="summary-grid">
        <div class="summary-card">
          <div class="summary-icon total"><mat-icon>inventory_2</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">Total Vehicles</span>
            <span class="summary-value">{{ data?.totalStock ?? 0 }}</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon available"><mat-icon>check_circle</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">Available</span>
            <span class="summary-value">{{ data?.available ?? 0 }}</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon hold"><mat-icon>pause_circle</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">On Hold</span>
            <span class="summary-value">{{ data?.onHold ?? 0 }}</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon booked"><mat-icon>bookmark</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">Booked</span>
            <span class="summary-value">{{ data?.booked ?? 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="charts-row">
        <div class="chart-card">
          <div class="chart-header">
            <h3><mat-icon>donut_large</mat-icon> Status Distribution</h3>
          </div>
          <div class="chart-body" *ngIf="statusChartData.labels && statusChartData.labels.length > 0">
            <canvas baseChart [data]="statusChartData" [options]="doughnutOptions" [type]="'doughnut'"></canvas>
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3><mat-icon>timeline</mat-icon> Stock Ageing</h3>
          </div>
          <div class="chart-body" *ngIf="ageingChartData.labels && ageingChartData.labels.length > 0">
            <canvas baseChart [data]="ageingChartData" [options]="barOptions" [type]="'bar'"></canvas>
          </div>
        </div>
      </div>

      <!-- Branch Table -->
      <div class="chart-card" *ngIf="data?.branchDistribution?.length">
        <div class="chart-header">
          <h3><mat-icon>store</mat-icon> Branch-wise Stock</h3>
        </div>
        <div class="chart-body">
          <canvas baseChart [data]="branchChartData" [options]="hBarOptions" [type]="'bar'"></canvas>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .report-container { display: flex; flex-direction: column; gap: 20px; }

    .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

    .summary-card {
      background: var(--dms-card); border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius); padding: 20px;
      display: flex; align-items: center; gap: 16px; box-shadow: var(--dms-card-shadow);
    }

    .summary-icon {
      width: 48px; height: 48px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      mat-icon { font-size: 24px; width: 24px; height: 24px; }

      &.total { background: #ede9fe; color: #7c3aed; }
      &.available { background: #dcfce7; color: #16a34a; }
      &.hold { background: #fef3c7; color: #d97706; }
      &.booked { background: #dbeafe; color: #2563eb; }
    }

    .summary-info { display: flex; flex-direction: column; }
    .summary-label { font-size: 12px; color: var(--dms-text-secondary); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }
    .summary-value { font-size: 24px; font-weight: 700; color: var(--dms-text-primary); }

    .charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

    .chart-card {
      background: var(--dms-card); border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius); box-shadow: var(--dms-card-shadow); overflow: hidden;
    }

    .chart-header {
      padding: 20px 24px 0;
      h3 {
        font-size: 16px; font-weight: 600; color: var(--dms-text-primary); margin: 0;
        display: flex; align-items: center; gap: 8px;
        mat-icon { font-size: 20px; width: 20px; height: 20px; color: var(--dms-accent); }
      }
    }

    .chart-body { padding: 16px 24px 24px; height: 300px; }
  `],
})
export class InventoryReportComponent implements OnInit {
  data: any = null;

  statusChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  ageingChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  branchChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  doughnutOptions: ChartOptions<'doughnut'> = {
    responsive: true, maintainAspectRatio: false, cutout: '65%',
    plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, pointStyle: 'circle', font: { size: 12 }, padding: 16 } } },
  };

  barOptions: ChartOptions<'bar'> = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { x: { grid: { display: false } }, y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.06)' }, ticks: { stepSize: 1 } } },
  };

  hBarOptions: ChartOptions<'bar'> = {
    responsive: true, maintainAspectRatio: false, indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: { x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.06)' }, ticks: { stepSize: 1 } }, y: { grid: { display: false } } },
  };

  constructor(private reportsService: ReportsService) {}

  ngOnInit(): void {
    this.reportsService.getInventoryReport().subscribe({
      next: (res) => {
        this.data = res.data;
        this.buildCharts(res.data);
      },
    });
  }

  buildCharts(data: any): void {
    // Status
    if (data.statusBreakdown) {
      const colors: Record<string, string> = {
        IN_TRANSIT: '#8b5cf6', IN_STOCK: '#6366f1', PDI_PENDING: '#f59e0b', PDI_PASSED: '#22c55e',
        PDI_FAILED: '#ef4444', AVAILABLE: '#10b981', RESERVED: '#3b82f6', HOLD: '#f97316',
        SOLD: '#06b6d4', DELIVERED: '#14b8a6', BOOKED: '#8b5cf6', GRN_RECEIVED: '#0ea5e9',
      };
      const entries = Object.entries(data.statusBreakdown).filter(([, c]: any) => c > 0);
      this.statusChartData = {
        labels: entries.map(([s]) => s.replace(/_/g, ' ')),
        datasets: [{ data: entries.map(([, c]: any) => c), backgroundColor: entries.map(([s]) => colors[s] || '#94a3b8'), borderWidth: 0 }],
      };
    }

    // Ageing
    if (data.ageingBuckets?.length) {
      const sColors: Record<string, string> = { green: '#22c55e', amber: '#f59e0b', orange: '#f97316', red: '#ef4444' };
      this.ageingChartData = {
        labels: data.ageingBuckets.map((b: any) => b.range + ' days'),
        datasets: [{ data: data.ageingBuckets.map((b: any) => b.count), backgroundColor: data.ageingBuckets.map((b: any) => sColors[b.severity] || '#6366f1'), borderRadius: 8, borderSkipped: false }],
      };
    }

    // Branch
    if (data.branchDistribution?.length) {
      const bColors = ['#6366f1', '#8b5cf6', '#a855f7', '#c084fc', '#d8b4fe'];
      this.branchChartData = {
        labels: data.branchDistribution.map((b: any) => b.branchName),
        datasets: [{ data: data.branchDistribution.map((b: any) => b.count), backgroundColor: data.branchDistribution.map((_: any, i: number) => bColors[i % bColors.length]), borderRadius: 8, borderSkipped: false }],
      };
    }
  }
}
