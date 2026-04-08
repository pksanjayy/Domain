import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

// Angular Material
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatBadgeModule } from '@angular/material/badge';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSliderModule } from '@angular/material/slider';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTreeModule } from '@angular/material/tree';

// Components
import { DataTableComponent } from './components/data-table/data-table.component';
import { FilterPanelComponent } from './components/filter-panel/filter-panel.component';
import { NotificationBellComponent } from './components/notification-bell/notification-bell.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { BranchSelectorComponent } from './components/branch-selector/branch-selector.component';
import { VehicleModelSelectorComponent } from './components/vehicle-model-selector/vehicle-model-selector.component';
import { CustomerSelectorComponent } from './components/customer-selector/customer-selector.component';

// Directives
import { HasRoleDirective } from './directives/has-role.directive';
import { HasPermissionDirective } from './directives/has-permission.directive';

// Pipes
import { StockStatusPipe } from './pipes/stock-status.pipe';
import { AgeDatePipe } from './pipes/age-date.pipe';
import { MaskPipe } from './pipes/mask.pipe';

const MATERIAL_MODULES = [
  MatTableModule,
  MatPaginatorModule,
  MatSortModule,
  MatFormFieldModule,
  MatInputModule,
  MatSelectModule,
  MatButtonModule,
  MatIconModule,
  MatDialogModule,
  MatSnackBarModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  MatBadgeModule,
  MatExpansionModule,
  MatDatepickerModule,
  MatNativeDateModule,
  MatToolbarModule,
  MatSidenavModule,
  MatListModule,
  MatMenuModule,
  MatCardModule,
  MatTooltipModule,
  MatChipsModule,
  MatDividerModule,
  MatSliderModule,
  MatAutocompleteModule,
  MatGridListModule,
  MatTabsModule,
  MatButtonToggleModule,
  MatRadioModule,
  MatCheckboxModule,
  ScrollingModule,
  MatSlideToggleModule,
  MatTreeModule,
];

const COMPONENTS = [
  DataTableComponent,
  FilterPanelComponent,
  NotificationBellComponent,
  ConfirmDialogComponent,
  LoadingSpinnerComponent,
  BranchSelectorComponent,
  VehicleModelSelectorComponent,
  CustomerSelectorComponent,
];

const DIRECTIVES = [HasRoleDirective, HasPermissionDirective];

const PIPES = [StockStatusPipe, AgeDatePipe, MaskPipe];

@NgModule({
  declarations: [...COMPONENTS, ...DIRECTIVES, ...PIPES],
  imports: [CommonModule, ReactiveFormsModule, FormsModule, ...MATERIAL_MODULES],
  exports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ...MATERIAL_MODULES,
    ...COMPONENTS,
    ...DIRECTIVES,
    ...PIPES,
  ],
})
export class SharedModule {}
