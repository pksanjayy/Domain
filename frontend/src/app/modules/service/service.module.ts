import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { ServiceBookingListComponent } from './components/service-booking-list/service-booking-list.component';
import { ServiceBookingFormComponent } from './components/service-booking-form/service-booking-form.component';
import { ServiceBookingDetailComponent } from './components/service-booking-detail/service-booking-detail.component';
import { ServiceRecordListComponent } from './components/service-record-list/service-record-list.component';
import { ServiceRecordFormComponent } from './components/service-record-form/service-record-form.component';
import { ServiceRecordDetailComponent } from './components/service-record-detail/service-record-detail.component';

const routes: Routes = [
  { path: '', redirectTo: 'bookings', pathMatch: 'full' },
  { path: 'bookings', component: ServiceBookingListComponent },
  { path: 'bookings/new', component: ServiceBookingFormComponent },
  { path: 'bookings/:id', component: ServiceBookingDetailComponent },
  { path: 'bookings/:id/edit', component: ServiceBookingFormComponent },
  { path: 'records', component: ServiceRecordListComponent },
  { path: 'records/new', component: ServiceRecordFormComponent },
  { path: 'records/:id', component: ServiceRecordDetailComponent },
  { path: 'records/:id/edit', component: ServiceRecordFormComponent }
];

@NgModule({
  declarations: [
    ServiceBookingListComponent,
    ServiceBookingFormComponent,
    ServiceBookingDetailComponent,
    ServiceRecordListComponent,
    ServiceRecordFormComponent,
    ServiceRecordDetailComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class ServiceModule { }
