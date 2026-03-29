import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { NgChartsModule } from 'ng2-charts';

import { ReportsDashboardComponent } from './components/reports-dashboard/reports-dashboard.component';
import { SalesReportComponent } from './components/sales-report/sales-report.component';
import { InventoryReportComponent } from './components/inventory-report/inventory-report.component';
import { AuditLogReportComponent } from './components/audit-log-report/audit-log-report.component';

const routes: Routes = [
  { path: '', component: ReportsDashboardComponent },
  { path: 'sales', component: ReportsDashboardComponent },
  { path: 'inventory', component: ReportsDashboardComponent },
  { path: 'audit-logs', component: ReportsDashboardComponent }
];

@NgModule({
  declarations: [
    ReportsDashboardComponent,
    SalesReportComponent,
    InventoryReportComponent,
    AuditLogReportComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    NgChartsModule,
    RouterModule.forChild(routes)
  ]
})
export class ReportsModule { }
