import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { TestDriveRoutingModule } from './testdrive-routing.module';
import { SharedModule } from '../../shared/shared.module';

import { FleetListComponent } from './components/fleet-list/fleet-list.component';
import { FleetFormComponent } from './components/fleet-form/fleet-form.component';
import { BookingListComponent } from './components/booking-list/booking-list.component';
import { BookingFormComponent } from './components/booking-form/booking-form.component';

@NgModule({
  declarations: [
    FleetListComponent,
    FleetFormComponent,
    BookingListComponent,
    BookingFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TestDriveRoutingModule,
    SharedModule
  ]
})
export class TestDriveModule { }
