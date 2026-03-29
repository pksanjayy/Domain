import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { RoleGuard } from './core/auth/role.guard';
import { LayoutComponent } from './layout/components/layout/layout.component';

const routes: Routes = [
  {
    path: 'login',
    loadChildren: () =>
      import('./modules/auth/auth.module').then((m) => m.AuthModule),
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./modules/dashboard/dashboard.module').then(
            (m) => m.DashboardModule
          ),
      },
      {
        path: 'inventory',
        loadChildren: () =>
          import('./modules/inventory/inventory.module').then(
            (m) => m.InventoryModule
          ),
        canActivate: [RoleGuard],
        data: {
          roles: [
            'SUPER_ADMIN',
            'MASTER_USER',
            'SALES_CRM_EXEC',
            'WORKSHOP_EXEC',
            'MANAGER_VIEWER',
          ],
        },
      },
      {
        path: 'sales',
        loadChildren: () =>
          import('./modules/sales/sales.module').then((m) => m.SalesModule),
        canActivate: [RoleGuard],
        data: {
          roles: [
            'SUPER_ADMIN',
            'MASTER_USER',
            'SALES_CRM_EXEC',
            'MANAGER_VIEWER',
          ],
        },
      },
      {
        path: 'service',
        loadChildren: () =>
          import('./modules/service/service.module').then((m) => m.ServiceModule),
        canActivate: [RoleGuard],
        data: {
          roles: [
            'SUPER_ADMIN',
            'MASTER_USER',
            'WORKSHOP_EXEC',
            'MANAGER_VIEWER',
          ],
        },
      },
      {
        path: 'reports',
        loadChildren: () =>
          import('./modules/reports/reports.module').then((m) => m.ReportsModule),
        canActivate: [AuthGuard],
      },
      {
        path: 'admin',
        loadChildren: () =>
          import('./modules/admin/admin.module').then((m) => m.AdminModule),
        canActivate: [RoleGuard],
        data: { roles: ['SUPER_ADMIN'] },
      },
      {
        path: 'testdrive',
        loadChildren: () =>
          import('./modules/testdrive/testdrive.module').then((m) => m.TestDriveModule),
        canActivate: [RoleGuard],
        data: {
          roles: [
            'SUPER_ADMIN',
            'MASTER_USER',
            'SALES_CRM_EXEC',
            'WORKSHOP_EXEC',
            'MANAGER_VIEWER',
          ],
        },
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
