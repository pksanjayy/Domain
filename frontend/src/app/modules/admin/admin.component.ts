import { Component } from '@angular/core';

@Component({
  selector: 'app-admin',
  template: `
    <div class="module-placeholder">
      <mat-icon class="placeholder-icon">admin_panel_settings</mat-icon>
      <h2>Admin Panel</h2>
      <p>User management, roles, menus, and system settings.</p>
      <mat-chip-listbox>
        <mat-chip>Users</mat-chip>
        <mat-chip>Roles</mat-chip>
        <mat-chip>Menus</mat-chip>
        <mat-chip>Codes</mat-chip>
      </mat-chip-listbox>
    </div>
  `,
  styles: [`
    .module-placeholder {
      text-align: center; padding: 64px 24px;
      mat-icon.placeholder-icon { font-size: 64px; width: 64px; height: 64px; color: #e65100; }
      h2 { font-size: 24px; color: #1a237e; margin: 16px 0 8px; }
      p { color: #666; margin-bottom: 24px; }
    }
  `],
})
export class AdminComponent {}
