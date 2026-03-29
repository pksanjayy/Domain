import { Component, OnInit, OnDestroy } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ChartConfiguration, ChartData } from 'chart.js';
import { InventoryService } from '../../services/inventory.service';
import { DashboardSummaryDto } from '../../models/inventory.model';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-inventory-dashboard',
  templateUrl: './inventory-dashboard.component.html',
  styleUrls: ['./inventory-dashboard.component.scss'],
})
export class InventoryDashboardComponent implements OnInit, OnDestroy {
  summary: DashboardSummaryDto | null = null;
  isLoading = true;
  gridCols = 4;

  private destroy$ = new Subject<void>();

  // ─── KPI Cards ───
  kpiCards: { title: string; value: number; icon: string; color: string }[] = [];

  // ─── Bar Chart (Ageing Buckets) ───
  barChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      title: { display: true, text: 'Stock Ageing Distribution', font: { size: 14 } },
    },
    scales: {
      y: { beginAtZero: true, ticks: { stepSize: 1 } },
    },
  };

  // ─── Doughnut Chart (Branch Distribution) ───
  doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      title: { display: true, text: 'Branch Stock Distribution', font: { size: 14 } },
    },
  };

  // ─── Heatmap ───
  heatmapModels: string[] = [];
  heatmapBuckets: string[] = ['0-30 days', '31-60 days', '61-90 days', '90+ days'];
  heatmapData: number[][] = [];

  constructor(
    private inventoryService: InventoryService,
    private notificationService: NotificationService,
    private breakpointObserver: BreakpointObserver
  ) {}

  ngOnInit(): void {
    this.setupResponsiveGrid();
    this.loadDashboard();
    this.subscribeToLiveUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupResponsiveGrid(): void {
    this.breakpointObserver
      .observe([Breakpoints.XSmall, Breakpoints.Small, Breakpoints.Medium])
      .pipe(takeUntil(this.destroy$))
      .subscribe((result) => {
        if (result.breakpoints[Breakpoints.XSmall]) {
          this.gridCols = 1;
        } else if (result.breakpoints[Breakpoints.Small]) {
          this.gridCols = 2;
        } else {
          this.gridCols = 4;
        }
      });
  }

  private loadDashboard(): void {
    this.isLoading = true;
    this.inventoryService.getDashboardSummary().subscribe({
      next: (response) => {
        this.summary = response.data;
        this.buildKpiCards();
        this.buildBarChart();
        this.buildDoughnutChart();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  private buildKpiCards(): void {
    if (!this.summary) return;
    this.kpiCards = [
      { title: 'Total Stock', value: this.summary.totalStock, icon: 'inventory_2', color: '#1976d2' },
      { title: 'Available', value: this.summary.available, icon: 'check_circle', color: '#2e7d32' },
      { title: 'On Hold', value: this.summary.onHold, icon: 'pause_circle', color: '#ed6c02' },
      { title: 'Booked', value: this.summary.booked, icon: 'bookmark', color: '#9c27b0' },
    ];
  }

  private buildBarChart(): void {
    if (!this.summary) return;
    this.barChartData = {
      labels: this.summary.ageingBuckets.map((b) => b.range),
      datasets: [
        {
          data: this.summary.ageingBuckets.map((b) => b.count),
          backgroundColor: ['#4caf50', '#ff9800', '#f57c00', '#d32f2f'],
          borderRadius: 4,
        },
      ],
    };
  }

  private buildDoughnutChart(): void {
    if (!this.summary) return;
    const colors = ['#1976d2', '#2e7d32', '#ed6c02', '#9c27b0', '#00bcd4', '#ff5722', '#607d8b'];
    this.doughnutChartData = {
      labels: this.summary.branchDistribution.map((b) => b.branchName),
      datasets: [
        {
          data: this.summary.branchDistribution.map((b) => b.count),
          backgroundColor: colors.slice(0, this.summary.branchDistribution.length),
        },
      ],
    };
  }

  private subscribeToLiveUpdates(): void {
    this.notificationService.notifications$
      .pipe(takeUntil(this.destroy$))
      .subscribe((notifications) => {
        const hasInventoryUpdate = notifications.some((n) => n.module === 'INVENTORY');
        if (hasInventoryUpdate && this.summary) {
          this.loadDashboard();
        }
      });
  }
}
