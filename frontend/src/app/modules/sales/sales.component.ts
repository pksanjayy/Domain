import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sales',
  template: `
    <div class="sales-container">
      <nav mat-tab-nav-bar [tabPanel]="tabPanel" color="primary">
        <a mat-tab-link
           *ngFor="let tab of tabs"
           [routerLink]="tab.path"
           routerLinkActive
           #rla="routerLinkActive"
           [active]="rla.isActive">
          <mat-icon class="tab-icon">{{ tab.icon }}</mat-icon>
          {{ tab.label }}
        </a>
      </nav>
      <mat-tab-nav-panel #tabPanel>
        <router-outlet></router-outlet>
      </mat-tab-nav-panel>
    </div>
  `,
  styles: [`
    .sales-container {
      .tab-icon { margin-right: 8px; font-size: 20px; }
    }
  `],
})
export class SalesComponent {
  tabs = [
    { label: 'Customers', path: 'customers', icon: 'people' },
    { label: 'Leads', path: 'leads', icon: 'trending_up' },
    { label: 'Payments', path: 'payments', icon: 'payments' },
    { label: 'Bookings', path: 'bookings', icon: 'event_available' },
  ];
}
