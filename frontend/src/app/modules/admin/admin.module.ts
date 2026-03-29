import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { UserManagementComponent } from './components/user-management/user-management.component';
import { RolePermissionComponent } from './components/role-permission/role-permission.component';
import { MenuManagementComponent } from './components/menu-management/menu-management.component';
import { AuditLogComponent } from './components/audit-log/audit-log.component';
import { BranchManagementComponent } from './components/branch-management/branch-management.component';

const routes: Routes = [
  { path: '', redirectTo: 'users', pathMatch: 'full' },
  { path: 'users', component: UserManagementComponent },
  { path: 'roles', component: RolePermissionComponent },
  { path: 'menus', component: MenuManagementComponent },
  { path: 'audit-log', component: AuditLogComponent },
  { path: 'branches', component: BranchManagementComponent },
];

@NgModule({
  declarations: [
    UserManagementComponent,
    RolePermissionComponent,
    MenuManagementComponent,
    AuditLogComponent,
    BranchManagementComponent,
  ],
  imports: [SharedModule, RouterModule.forChild(routes)],
})
export class AdminModule {}
