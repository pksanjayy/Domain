import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FleetListComponent } from './components/fleet-list/fleet-list.component';
import { FleetFormComponent } from './components/fleet-form/fleet-form.component';
import { BookingListComponent } from './components/booking-list/booking-list.component';
import { BookingFormComponent } from './components/booking-form/booking-form.component';

const routes: Routes = [
  { path: '', redirectTo: 'fleet', pathMatch: 'full' },
  { path: 'fleet', component: FleetListComponent },
  { path: 'fleet/new', component: FleetFormComponent },
  { path: 'fleet/:id/edit', component: FleetFormComponent },
  { path: 'bookings', component: BookingListComponent },
  { path: 'bookings/new', component: BookingFormComponent },
  { path: 'bookings/:id/edit', component: BookingFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TestDriveRoutingModule { }
