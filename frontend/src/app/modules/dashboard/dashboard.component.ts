import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { selectCurrentUser } from '../../core/auth/store/auth.selectors';
import { User } from '../../core/models';
import { InventoryService } from '../inventory/services/inventory.service';
import { NotificationService } from '../../core/services/notification.service';
import { DashboardSummaryDto } from '../inventory/models/inventory.model';
import { ChartConfiguration, ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="dashboard-container">
      <!-- Header -->
      <div class="dashboard-header">
        <div>
          <h1>Dashboard</h1>
          <p *ngIf="user$ | async as user">Welcome back, <strong>{{ user.username }}</strong></p>
        </div>
        <div class="header-date">
          <mat-icon>calendar_today</mat-icon>
          <span>{{ today | date:'EEEE, MMMM d, y' }}</span>
        </div>
      </div>

      <!-- KPI Cards -->
      <div class="kpi-grid">
        <div class="kpi-card animate-fade-up stagger-1" (click)="onKpiClick('total')">
          <div class="kpi-icon-wrapper">
            <mat-icon>inventory_2</mat-icon>
          </div>
          <div class="kpi-body">
            <span class="kpi-label">Total Stock</span>
            <span class="kpi-value tabular">{{ dashboardData?.totalStock ?? '—' }}</span>
          </div>
          <span class="kpi-subtitle">Vehicles in inventory</span>
        </div>

        <div class="kpi-card animate-fade-up stagger-2" (click)="onKpiClick('available')">
          <div class="kpi-icon-wrapper success">
            <mat-icon>check_circle</mat-icon>
          </div>
          <div class="kpi-body">
            <span class="kpi-label">Available</span>
            <span class="kpi-value tabular success-text">{{ dashboardData?.available ?? '—' }}</span>
          </div>
          <span class="kpi-subtitle">Ready for sale</span>
        </div>

        <div class="kpi-card animate-fade-up stagger-3" (click)="onKpiClick('hold')">
          <div class="kpi-icon-wrapper warning">
            <mat-icon>pause_circle</mat-icon>
          </div>
          <div class="kpi-body">
            <span class="kpi-label">On Hold</span>
            <span class="kpi-value tabular warning-text">{{ dashboardData?.onHold ?? '—' }}</span>
          </div>
          <span class="kpi-subtitle">Reserved / booked</span>
        </div>

        <div class="kpi-card animate-fade-up stagger-4">
          <div class="kpi-icon-wrapper info">
            <mat-icon>notifications_active</mat-icon>
          </div>
          <div class="kpi-body">
            <span class="kpi-label">Notifications</span>
            <span class="kpi-value tabular">{{ unreadCount ?? '—' }}</span>
          </div>
          <span class="kpi-subtitle">Unread alerts</span>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="charts-grid" *ngIf="dashboardData">
        <!-- Stock Ageing Bar Chart -->
        <div class="chart-card animate-fade-up stagger-2">
          <div class="chart-header">
            <h3><mat-icon>timeline</mat-icon> Stock Ageing Analysis</h3>
            <span class="chart-subtitle">Days since arrival</span>
          </div>
          <div class="chart-body">
            <canvas baseChart
              [data]="ageingChartData"
              [options]="barChartOptions"
              [type]="'bar'">
            </canvas>
          </div>
        </div>

        <!-- Status Distribution Doughnut -->
        <div class="chart-card animate-fade-up stagger-3">
          <div class="chart-header">
            <h3><mat-icon>donut_large</mat-icon> Status Distribution</h3>
            <span class="chart-subtitle">Current inventory breakdown</span>
          </div>
          <div class="chart-body chart-body-doughnut">
            <canvas baseChart
              [data]="statusChartData"
              [options]="doughnutChartOptions"
              [type]="'doughnut'">
            </canvas>
          </div>
        </div>

        <!-- Branch Distribution Bar Chart -->
        <div class="chart-card animate-fade-up stagger-4">
          <div class="chart-header">
            <h3><mat-icon>store</mat-icon> Branch-wise Stock</h3>
            <span class="chart-subtitle">Vehicle count per branch</span>
          </div>
          <div class="chart-body">
            <canvas baseChart
              [data]="branchChartData"
              [options]="horizontalBarOptions"
              [type]="'bar'">
            </canvas>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="chart-card stats-card animate-fade-up stagger-5">
          <div class="chart-header">
            <h3><mat-icon>insights</mat-icon> Quick Stats</h3>
            <span class="chart-subtitle">Key performance indicators</span>
          </div>
          <div class="stats-list">
            <div class="stat-item">
              <div class="stat-icon booked"><mat-icon>bookmark</mat-icon></div>
              <div class="stat-info">
                <span class="stat-label">Booked</span>
                <span class="stat-value">{{ dashboardData.booked }}</span>
              </div>
            </div>
            <div class="stat-item" *ngFor="let bucket of dashboardData.ageingBuckets">
              <div class="stat-icon" [ngClass]="bucket.severity">
                <mat-icon>{{ getAgeingIcon(bucket.severity) }}</mat-icon>
              </div>
              <div class="stat-info">
                <span class="stat-label">{{ bucket.range }} days</span>
                <span class="stat-value">{{ bucket.count }} vehicles</span>
              </div>
              <div class="stat-bar">
                <div class="stat-bar-fill" [ngClass]="bucket.severity"
                     [style.width.%]="getBarWidth(bucket.count)"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div class="loading-state" *ngIf="!dashboardData">
        <mat-spinner diameter="48"></mat-spinner>
        <p>Loading dashboard data...</p>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      max-width: 1440px;
      margin: 0 auto;
    }

    .dashboard-header {
      margin-bottom: 28px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;

      h1 {
        font-size: 26px;
        font-weight: 700;
        color: var(--dms-text-primary);
        letter-spacing: -0.02em;
        margin-bottom: 4px;
      }

      p {
        font-size: 14px;
        color: var(--dms-text-secondary);
        margin: 0;

        strong {
          color: var(--dms-text-primary);
          font-weight: 500;
        }
      }
    }

    .header-date {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--dms-text-secondary);
      font-size: 13px;
      background: var(--dms-card);
      padding: 8px 16px;
      border-radius: var(--dms-card-radius);
      border: 1px solid var(--dms-card-border);

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        color: var(--dms-accent);
      }
    }

    /* KPI Grid */
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin-bottom: 24px;
    }

    .kpi-card {
      background: var(--dms-card);
      border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius);
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      box-shadow: var(--dms-card-shadow);
      transition: box-shadow 0.2s ease, transform 0.15s ease;
      min-height: 140px;
      cursor: pointer;

      &:hover {
        box-shadow: var(--dms-card-hover-shadow);
        transform: translateY(-2px);
      }
    }

    .kpi-icon-wrapper {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: var(--dms-accent-light);
      color: var(--dms-accent);
      display: flex;
      align-items: center;
      justify-content: center;

      mat-icon {
        font-size: 22px;
        width: 22px;
        height: 22px;
      }

      &.success {
        background: #dcfce7;
        color: #16a34a;
      }

      &.warning {
        background: #fef3c7;
        color: #d97706;
      }

      &.info {
        background: #dbeafe;
        color: #2563eb;
      }
    }

    .kpi-body {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .kpi-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--dms-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .kpi-value {
      font-size: 30px;
      font-weight: 700;
      color: var(--dms-text-primary);
      line-height: 1.1;
      letter-spacing: -0.03em;
      font-variant-numeric: tabular-nums;
    }

    .success-text { color: #16a34a; }
    .warning-text { color: #d97706; }

    .kpi-subtitle {
      font-size: 12px;
      color: var(--dms-text-tertiary);
    }

    /* Charts Grid */
    .charts-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }

    .chart-card {
      background: var(--dms-card);
      border: 1px solid var(--dms-card-border);
      border-radius: var(--dms-card-radius);
      box-shadow: var(--dms-card-shadow);
      overflow: hidden;
      transition: box-shadow 0.2s ease;

      &:hover {
        box-shadow: var(--dms-card-hover-shadow);
      }
    }

    .chart-header {
      padding: 20px 24px 0;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: var(--dms-text-primary);
        margin: 0 0 4px;
        display: flex;
        align-items: center;
        gap: 8px;

        mat-icon {
          font-size: 20px;
          width: 20px;
          height: 20px;
          color: var(--dms-accent);
        }
      }
    }

    .chart-subtitle {
      font-size: 12px;
      color: var(--dms-text-tertiary);
    }

    .chart-body {
      padding: 16px 24px 24px;
      height: 280px;
      position: relative;
    }

    .chart-body-doughnut {
      height: 280px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    /* Stats Card */
    .stats-card {
      .stats-list {
        padding: 12px 24px 24px;
        display: flex;
        flex-direction: column;
        gap: 14px;
      }
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 14px;
      border-radius: 10px;
      background: rgba(0,0,0,0.02);
      transition: background 0.2s;

      &:hover {
        background: rgba(0,0,0,0.04);
      }
    }

    .stat-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      &.green { background: #dcfce7; color: #16a34a; }
      &.amber { background: #fef3c7; color: #d97706; }
      &.orange { background: #ffedd5; color: #ea580c; }
      &.red { background: #fee2e2; color: #dc2626; }
      &.booked { background: #dbeafe; color: #2563eb; }
    }

    .stat-info {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-width: 0;
    }

    .stat-label {
      font-size: 13px;
      font-weight: 500;
      color: var(--dms-text-primary);
    }

    .stat-value {
      font-size: 12px;
      color: var(--dms-text-secondary);
    }

    .stat-bar {
      width: 80px;
      height: 6px;
      background: rgba(0,0,0,0.06);
      border-radius: 3px;
      overflow: hidden;
    }

    .stat-bar-fill {
      height: 100%;
      border-radius: 3px;
      transition: width 0.6s ease;

      &.green { background: #16a34a; }
      &.amber { background: #d97706; }
      &.orange { background: #ea580c; }
      &.red { background: #dc2626; }
    }

    /* Loading */
    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 80px 0;
      gap: 16px;

      p {
        color: var(--dms-text-secondary);
        font-size: 14px;
      }
    }

    /* Animations */
    .animate-fade-up {
      animation: fadeUp 0.5s ease both;
    }
    .stagger-1 { animation-delay: 0.05s; }
    .stagger-2 { animation-delay: 0.1s; }
    .stagger-3 { animation-delay: 0.15s; }
    .stagger-4 { animation-delay: 0.2s; }
    .stagger-5 { animation-delay: 0.25s; }

    @keyframes fadeUp {
      from { opacity: 0; transform: translateY(12px); }
      to { opacity: 1; transform: translateY(0); }
    }

    @media (max-width: 1200px) {
      .kpi-grid { grid-template-columns: repeat(2, 1fr); }
      .charts-grid { grid-template-columns: 1fr; }
    }

    @media (max-width: 480px) {
      .kpi-grid { grid-template-columns: 1fr; }
    }
  `],
})
export class DashboardComponent implements OnInit {
  user$: Observable<User | null>;
  dashboardData: DashboardSummaryDto | null = null;
  unreadCount: number | null = null;
  today = new Date();

  // Chart data
  ageingChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  statusChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  branchChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0,0,0,0.8)',
        titleFont: { size: 13 },
        bodyFont: { size: 12 },
        padding: 12,
        cornerRadius: 8,
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { font: { size: 12 } },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(0,0,0,0.06)' },
        ticks: { font: { size: 12 }, stepSize: 1 },
      },
    },
  };

  doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          padding: 16,
          usePointStyle: true,
          pointStyle: 'circle',
          font: { size: 12 },
        },
      },
      tooltip: {
        backgroundColor: 'rgba(0,0,0,0.8)',
        padding: 12,
        cornerRadius: 8,
      },
    },
  };

  horizontalBarOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0,0,0,0.8)',
        padding: 12,
        cornerRadius: 8,
      },
    },
    scales: {
      x: {
        beginAtZero: true,
        grid: { color: 'rgba(0,0,0,0.06)' },
        ticks: { font: { size: 12 }, stepSize: 1 },
      },
      y: {
        grid: { display: false },
        ticks: { font: { size: 12 } },
      },
    },
  };

  constructor(
    private store: Store,
    private inventoryService: InventoryService,
    private notificationService: NotificationService
  ) {
    this.user$ = this.store.select(selectCurrentUser);
  }

  ngOnInit(): void {
    this.inventoryService.getDashboardSummary().subscribe({
      next: (res) => {
        this.dashboardData = res.data;
        this.buildCharts(res.data);
      },
    });

    this.notificationService.unreadCount$.subscribe({
      next: (count) => (this.unreadCount = count),
    });
    this.notificationService.loadUnreadCount();
  }

  buildCharts(data: DashboardSummaryDto): void {
    // Ageing chart
    const severityColors: Record<string, string> = {
      green: '#22c55e',
      amber: '#f59e0b',
      orange: '#f97316',
      red: '#ef4444',
    };

    if (data.ageingBuckets && data.ageingBuckets.length > 0) {
      this.ageingChartData = {
        labels: data.ageingBuckets.map((b) => b.range + ' days'),
        datasets: [
          {
            data: data.ageingBuckets.map((b) => b.count),
            backgroundColor: data.ageingBuckets.map(
              (b) => severityColors[b.severity] || '#6366f1'
            ),
            borderRadius: 8,
            borderSkipped: false,
            barPercentage: 0.6,
          },
        ],
      };
    }

    // Status doughnut
    if (data.statusBreakdown) {
      const statusColors: Record<string, string> = {
        IN_TRANSIT: '#8b5cf6',
        IN_STOCK: '#6366f1',
        AVAILABLE: '#10b981',
        RESERVED: '#3b82f6',
        HOLD: '#f97316',
        SOLD: '#06b6d4',
        DELIVERED: '#14b8a6',
        BOOKED: '#8b5cf6',
        GRN_RECEIVED: '#0ea5e9',
      };

      const entries = Object.entries(data.statusBreakdown).filter(
        ([, count]) => count > 0
      );
      this.statusChartData = {
        labels: entries.map(([status]) =>
          status.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())
        ),
        datasets: [
          {
            data: entries.map(([, count]) => count),
            backgroundColor: entries.map(
              ([status]) => statusColors[status] || '#94a3b8'
            ),
            borderWidth: 0,
            hoverOffset: 6,
          },
        ],
      };
    }

    // Branch chart
    if (data.branchDistribution && data.branchDistribution.length > 0) {
      const branchColors = ['#6366f1', '#8b5cf6', '#a855f7', '#c084fc', '#d8b4fe'];
      this.branchChartData = {
        labels: data.branchDistribution.map((b) => b.branchName),
        datasets: [
          {
            data: data.branchDistribution.map((b) => b.count),
            backgroundColor: data.branchDistribution.map(
              (_, i) => branchColors[i % branchColors.length]
            ),
            borderRadius: 8,
            borderSkipped: false,
            barPercentage: 0.5,
          },
        ],
      };
    }
  }

  onKpiClick(type: string): void {
    // Could navigate to filtered views
  }

  getAgeingIcon(severity: string): string {
    const icons: Record<string, string> = {
      green: 'schedule',
      amber: 'warning',
      orange: 'priority_high',
      red: 'error',
    };
    return icons[severity] || 'info';
  }

  getBarWidth(count: number): number {
    if (!this.dashboardData || this.dashboardData.totalStock === 0) return 0;
    return Math.max(5, (count / this.dashboardData.totalStock) * 100);
  }
}
