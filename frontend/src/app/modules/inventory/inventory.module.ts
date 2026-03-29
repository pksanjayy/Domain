import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { NgChartsModule } from 'ng2-charts';
import { RoleGuard } from '../../core/auth/role.guard';

// Components
import { InventoryDashboardComponent } from './components/inventory-dashboard/inventory-dashboard.component';
import { VehicleListComponent } from './components/vehicle-list/vehicle-list.component';
import { VehicleDetailComponent } from './components/vehicle-detail/vehicle-detail.component';
import { VehicleFormComponent } from './components/vehicle-form/vehicle-form.component';
import { GrnListComponent } from './components/grn-list/grn-list.component';
import { GrnFormComponent } from './components/grn-form/grn-form.component';
import { PdiChecklistComponent } from './components/pdi-checklist/pdi-checklist.component';
import { StockTransferComponent } from './components/stock-transfer/stock-transfer.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: InventoryDashboardComponent },
  { path: 'vehicles', component: VehicleListComponent },
  {
    path: 'vehicles/new',
    component: VehicleFormComponent,
    canActivate: [RoleGuard],
    data: { roles: ['WORKSHOP_EXEC', 'SUPER_ADMIN'] },
  },
  { path: 'vehicles/:id', component: VehicleDetailComponent },
  { path: 'vehicles/:id/edit', component: VehicleFormComponent },
  { path: 'grn', component: GrnListComponent },
  { path: 'grn/new', component: GrnFormComponent },
  { path: 'grn/:id/edit', component: GrnFormComponent },
  { path: 'pdi/:vehicleId', component: PdiChecklistComponent },
  { path: 'transfers', component: StockTransferComponent },
];

@NgModule({
  declarations: [
    InventoryDashboardComponent,
    VehicleListComponent,
    VehicleDetailComponent,
    VehicleFormComponent,
    GrnListComponent,
    GrnFormComponent,
    PdiChecklistComponent,
    StockTransferComponent,
  ],
  imports: [SharedModule, NgChartsModule, RouterModule.forChild(routes)],
})
export class InventoryModule {}
