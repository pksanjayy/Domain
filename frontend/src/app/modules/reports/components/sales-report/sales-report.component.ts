import { Component, OnInit } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';
import { ReportsService } from '../../services/reports.service';

@Component({
  selector: 'app-sales-report',
  template: `
    <div class="report-container">
      <!-- Summary Cards -->
      <div class="summary-grid">
        <div class="summary-card">
          <div class="summary-icon sold"><mat-icon>check_circle</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">Active Bookings</span>
            <span class="summary-value">{{ activeBookings }}</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon delivered"><mat-icon>cancel</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">Cancellations</span>
            <span class="summary-value">{{ cancelledBookings }}</span>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon revenue"><mat-icon>payments</mat-icon></div>
          <div class="summary-info">
            <span class="summary-label">Revenue Collected</span>
            <span class="summary-value">₹{{ revenueCollected | number:'1.0-0' }}</span>
          </div>
        </div>
      </div>

      <!-- Monthly Sales Chart -->
      <div class="chart-card">
        <div class="chart-header" style="display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0;"><mat-icon>show_chart</mat-icon> Monthly Sales Trend</h3>
          <mat-form-field appearance="outline" style="width: 120px; margin-bottom: -1.25em;">
            <mat-label>Year</mat-label>
            <mat-select [(value)]="currentYear" (selectionChange)="onYearChange($event)">
              <mat-option *ngFor="let year of availableYears" [value]="year">{{ year }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
        <div class="chart-body" *ngIf="salesChartData.labels && salesChartData.labels.length > 0">
          <canvas baseChart
            [data]="salesChartData"
            [options]="lineChartOptions"
            [type]="'line'">
          </canvas>
        </div>
        <div class="empty-state" *ngIf="!salesChartData.labels || salesChartData.labels.length === 0">
          <mat-icon>analytics</mat-icon>
          <p>No sales data available for {{ currentYear }}</p>
        </div>
      </div>

      <!-- Sales Table -->
      <div class="chart-card" *ngIf="monthlyData.length > 0">
        <div class="chart-header">
          <h3><mat-icon>table_chart</mat-icon> Monthly Breakdown</h3>
        </div>
        <div class="table-container">
          <table mat-table [dataSource]="monthlyData">
            <ng-container matColumnDef="month">
              <th mat-header-cell *matHeaderCellDef>Month</th>
              <td mat-cell *matCellDef="let row">{{ row.month }}</td>
            </ng-container>
            <ng-container matColumnDef="active">
              <th mat-header-cell *matHeaderCellDef>Active</th>
              <td mat-cell *matCellDef="let row">{{ row.active }}</td>
            </ng-container>
            <ng-container matColumnDef="cancelled">
              <th mat-header-cell *matHeaderCellDef>Cancelled</th>
              <td mat-cell *matCellDef="let row">{{ row.cancelled }}</td>
            </ng-container>
            <ng-container matColumnDef="revenue">
              <th mat-header-cell *matHeaderCellDef>Revenue</th>
              <td mat-cell *matCellDef="let row">₹{{ row.revenue | number:'1.0-0' }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="['month','active','cancelled','revenue']"></tr>
            <tr mat-row *matRowDef="let row; columns: ['month','active','cancelled','revenue']"></tr>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .report-container { display: flex; flex-direction: column; gap: 20px; }

    .summary-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
    }

    .summary-card {
      background: var(--dms-card);
      border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius);
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: var(--dms-card-shadow);
    }

    .summary-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      mat-icon { font-size: 24px; width: 24px; height: 24px; }

      &.sold { background: #dbeafe; color: #2563eb; }
      &.delivered { background: #dcfce7; color: #16a34a; }
      &.revenue { background: #fef3c7; color: #d97706; }
    }

    .summary-info { display: flex; flex-direction: column; }
    .summary-label { font-size: 12px; color: var(--dms-text-secondary); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }
    .summary-value { font-size: 24px; font-weight: 700; color: var(--dms-text-primary); letter-spacing: -0.02em; }

    .chart-card {
      background: var(--dms-card);
      border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius);
      box-shadow: var(--dms-card-shadow);
      overflow: hidden;
    }

    .chart-header {
      padding: 20px 24px 0;
      h3 {
        font-size: 16px; font-weight: 600; color: var(--dms-text-primary); margin: 0;
        display: flex; align-items: center; gap: 8px;
        mat-icon { font-size: 20px; width: 20px; height: 20px; color: var(--dms-accent); }
      }
      ::ng-deep .mat-mdc-form-field-subscript-wrapper {
        display: none;
      }
    }

    .chart-body { padding: 16px 24px 24px; height: 320px; }

    .table-container {
      padding: 0 24px 24px;
      table { width: 100%; }
      th { font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 0.03em; }
      td { font-size: 14px; }
    }

    .empty-state {
      padding: 60px 24px;
      text-align: center;
      color: var(--dms-text-tertiary);
      mat-icon { font-size: 48px; width: 48px; height: 48px; margin-bottom: 12px; }
      p { font-size: 14px; margin: 0; }
    }
    
    @media (max-width: 768px) {
      .summary-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class SalesReportComponent implements OnInit {
  currentYear = new Date().getFullYear();
  availableYears: number[] = [];
  
  activeBookings = 0;
  cancelledBookings = 0;
  revenueCollected = 0;
  monthlyData: any[] = [];

  salesChartData: ChartData<'line'> = { labels: [], datasets: [] };

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: { usePointStyle: true, pointStyle: 'circle', font: { size: 12 } },
      },
      tooltip: { backgroundColor: 'rgba(0,0,0,0.8)', padding: 12, cornerRadius: 8 },
    },
    scales: {
      x: { grid: { display: false }, ticks: { font: { size: 12 } } },
      y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.06)' }, ticks: { font: { size: 12 }, stepSize: 1 } },
    },
    elements: {
      line: { tension: 0.4 },
      point: { radius: 4, hoverRadius: 6 },
    },
  };

  constructor(private reportsService: ReportsService) {
    const startYear = this.currentYear - 5;
    for (let i = this.currentYear; i >= startYear; i--) {
      this.availableYears.push(i);
    }
  }

  ngOnInit(): void {
    this.loadData();
  }

  onYearChange(event: any): void {
    this.currentYear = event.value;
    this.loadData();
  }

  loadData(): void {
    this.reportsService.getSalesData(this.currentYear).subscribe({
      next: (res) => {
        const vehicles = res.data?.content || [];
        this.processData(vehicles);
      },
      error: () => {
        this.processData([]);
      }
    });
  }

  processData(bookings: any[]): void {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const activeByMonth = new Array(12).fill(0);
    const cancelledByMonth = new Array(12).fill(0);
    const revenueByMonth = new Array(12).fill(0);

    bookings.forEach((b: any) => {
      if (!b.bookingDate) return;
      const date = new Date(b.bookingDate);
      if (date.getFullYear() === this.currentYear) {
        const month = date.getMonth();
        if (b.status === 'ACTIVE') {
          activeByMonth[month]++;
          revenueByMonth[month] += b.amountPaid || 0;
        } else if (b.status === 'CANCELLED') {
          cancelledByMonth[month]++;
        }
      }
    });

    this.activeBookings = activeByMonth.reduce((a, b) => a + b, 0);
    this.cancelledBookings = cancelledByMonth.reduce((a, b) => a + b, 0);
    this.revenueCollected = revenueByMonth.reduce((a, b) => a + b, 0);

    this.monthlyData = months.map((m, i) => ({
      month: m,
      active: activeByMonth[i],
      cancelled: cancelledByMonth[i],
      revenue: revenueByMonth[i],
    }));

    if (this.activeBookings === 0 && this.cancelledBookings === 0) {
      this.salesChartData = { labels: [], datasets: [] }; // trigger empty state
      this.monthlyData = []; // hide table
      return;
    }

    this.salesChartData = {
      labels: months,
      datasets: [
        {
          data: activeByMonth,
          label: 'Active Bookings',
          borderColor: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          fill: true,
        },
        {
          data: cancelledByMonth,
          label: 'Cancelled',
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          fill: true,
        },
      ],
    };
  }
}
