import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { SalesComponent } from './sales.component';
import { CustomerListComponent } from './components/customer-list/customer-list.component';
import { CustomerFormComponent } from './components/customer-form/customer-form.component';
import { LeadListComponent } from './components/lead-list/lead-list.component';
import { LeadFormComponent } from './components/lead-form/lead-form.component';
import { BookingListComponent } from './components/booking-list/booking-list.component';
import { BookingFormComponent } from './components/booking-form/booking-form.component';
import { PaymentListComponent } from './components/payment-list/payment-list.component';
import { PaymentFormComponent } from './components/payment-form/payment-form.component';
import { PaymentDetailComponent } from './components/payment-detail/payment-detail.component';
import { LeadDetailComponent } from './components/lead-detail/lead-detail.component';
import { BookingDetailComponent } from './components/booking-detail/booking-detail.component';

const routes: Routes = [
  {
    path: '',
    component: SalesComponent,
    children: [
      { path: '', redirectTo: 'customers', pathMatch: 'full' },
      { path: 'customers', component: CustomerListComponent },
      { path: 'customers/new', component: CustomerFormComponent },
      { path: 'customers/edit/:id', component: CustomerFormComponent },
      { path: 'leads', component: LeadListComponent },
      { path: 'leads/new', component: LeadFormComponent },
      { path: 'leads/edit/:id', component: LeadFormComponent },
      { path: 'leads/:id', component: LeadDetailComponent },
      { path: 'payments', component: PaymentListComponent },
      { path: 'payments/new', component: PaymentFormComponent },
      { path: 'payments/:id', component: PaymentDetailComponent },
      { path: 'payments/:id/edit', component: PaymentFormComponent },
      { path: 'bookings', component: BookingListComponent },
      { path: 'bookings/new', component: BookingFormComponent },
      { path: 'bookings/edit/:id', component: BookingFormComponent },
      { path: 'bookings/:id', component: BookingDetailComponent },
    ],
  },
];

@NgModule({
  declarations: [
    SalesComponent,
    CustomerListComponent,
    CustomerFormComponent,
    LeadListComponent,
    LeadFormComponent,
    LeadDetailComponent,
    BookingListComponent,
    BookingFormComponent,
    BookingDetailComponent,
    PaymentListComponent,
    PaymentFormComponent,
    PaymentDetailComponent,
  ],
  imports: [SharedModule, RouterModule.forChild(routes)],
})
export class SalesModule {}
