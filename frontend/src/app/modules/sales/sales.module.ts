import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { SalesComponent } from './sales.component';
import { CustomerListComponent } from './components/customer-list/customer-list.component';
import { LeadListComponent } from './components/lead-list/lead-list.component';
import { BookingListComponent } from './components/booking-list/booking-list.component';
import { PaymentListComponent } from './components/payment-list/payment-list.component';
import { PaymentFormComponent } from './components/payment-form/payment-form.component';
import { PaymentDetailComponent } from './components/payment-detail/payment-detail.component';

const routes: Routes = [
  {
    path: '',
    component: SalesComponent,
    children: [
      { path: '', redirectTo: 'customers', pathMatch: 'full' },
      { path: 'customers', component: CustomerListComponent },
      { path: 'leads', component: LeadListComponent },
      { path: 'payments', component: PaymentListComponent },
      { path: 'payments/new', component: PaymentFormComponent },
      { path: 'payments/:id', component: PaymentDetailComponent },
      { path: 'payments/:id/edit', component: PaymentFormComponent },
      { path: 'bookings', component: BookingListComponent },
    ],
  },
];

@NgModule({
  declarations: [
    SalesComponent,
    CustomerListComponent,
    LeadListComponent,
    BookingListComponent,
    PaymentListComponent,
    PaymentFormComponent,
    PaymentDetailComponent,
  ],
  imports: [SharedModule, RouterModule.forChild(routes)],
})
export class SalesModule {}
