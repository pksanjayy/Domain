import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-reports-dashboard',
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Reports Dashboard</h1>
        <p>View sales, inventory, and system activity reports</p>
      </div>

      <div class="mat-elevation-z1">
        <mat-tab-group animationDuration="0ms" [selectedIndex]="selectedIndex" (selectedIndexChange)="onTabChange($event)">
          <!-- Sales Report Tab -->
          <mat-tab>
            <ng-template mat-tab-label>
              <mat-icon class="tab-icon">show_chart</mat-icon>
              Sales Report
            </ng-template>
            <div class="tab-content" *ngIf="selectedIndex === 0">
              <app-sales-report></app-sales-report>
            </div>
          </mat-tab>

          <!-- Inventory Report Tab -->
          <mat-tab>
            <ng-template mat-tab-label>
              <mat-icon class="tab-icon">inventory_2</mat-icon>
              Inventory Report
            </ng-template>
            <div class="tab-content" *ngIf="selectedIndex === 1">
              <app-inventory-report></app-inventory-report>
            </div>
          </mat-tab>

          <!-- Audit Log Report Tab -->
          <mat-tab>
            <ng-template mat-tab-label>
              <mat-icon class="tab-icon">history</mat-icon>
              Audit Logs
            </ng-template>
            <div class="tab-content" *ngIf="selectedIndex === 2">
              <app-audit-log-report></app-audit-log-report>
            </div>
          </mat-tab>
        </mat-tab-group>
      </div>
    </div>
  `,
  styles: [`
    .page-container {
      max-width: 1440px;
      margin: 0 auto;
      padding: 0;
    }

    .page-header {
      margin-bottom: 24px;

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
      }
    }

    .tab-icon {
      margin-right: 8px;
    }

    .tab-content {
      padding: 24px 0;
    }

    /* Style material tabs for DMS */
    ::ng-deep .mat-mdc-tab-group {
      background: transparent;
    }

    ::ng-deep .mat-mdc-tab-header {
      background: var(--dms-card);
      border-radius: var(--dms-card-radius);
      box-shadow: var(--dms-card-shadow);
      border: 1px solid var(--dms-card-border);
      padding: 0 16px;
    }

    ::ng-deep .mdc-tab__text-label {
      font-weight: 500;
      font-size: 14px;
      font-family: var(--dms-font);
    }

    ::ng-deep .mat-mdc-tab.mdc-tab--active .mdc-tab__text-label {
      color: var(--dms-accent) !important;
    }

    ::ng-deep .mdc-tab-indicator__content--underline {
      border-color: var(--dms-accent) !important;
      border-top-width: 3px !important;
    }
  `]
})
export class ReportsDashboardComponent implements OnInit, OnDestroy {
  selectedIndex = 0;
  private destroy$ = new Subject<void>();
  
  private routeMap: Record<string, number> = {
    '/reports/sales': 0,
    '/reports/inventory': 1,
    '/reports/audit-logs': 2,
    '/reports': 0
  };

  private tabMap = ['/reports/sales', '/reports/inventory', '/reports/audit-logs'];

  constructor(private router: Router) {}

  ngOnInit() {
    this.updateIndexFromRoute(this.router.url);
    
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: any) => {
      this.updateIndexFromRoute(event.urlAfterRedirects || event.url);
    });
  }
  
  private updateIndexFromRoute(url: string) {
    const path = url.split('?')[0];
    if (this.routeMap[path] !== undefined) {
      this.selectedIndex = this.routeMap[path];
    }
  }

  onTabChange(index: number) {
    this.selectedIndex = index;
    // Update the URL to match the tab clicked
    this.router.navigate([this.tabMap[index]]);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
