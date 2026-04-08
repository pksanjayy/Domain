import { Component, Input, OnInit, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { CustomerService, CustomerDto } from '../../../core/services/customer.service';

@Component({
  selector: 'app-customer-selector',
  templateUrl: './customer-selector.component.html',
  styleUrls: ['./customer-selector.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CustomerSelectorComponent),
      multi: true
    }
  ]
})
export class CustomerSelectorComponent implements OnInit, ControlValueAccessor {
  @Input() label = 'Customer';
  @Input() placeholder = 'Search for a customer';
  @Input() required = false;

  customers: CustomerDto[] = [];
  filteredCustomers$: Observable<CustomerDto[]> = of([]);
  searchText = '';
  selectedCustomer: CustomerDto | null = null;

  private onChange: (value: any) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private customerService: CustomerService) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.customerService.getAllCustomers().subscribe({
      next: (customers) => {
        this.customers = customers;
        this.filteredCustomers$ = of(this.customers);
      },
      error: (err) => console.error('Failed to load customers', err)
    });
  }

  onSearchChange(searchValue: string): void {
    this.searchText = searchValue.toLowerCase();
    this.filteredCustomers$ = of(
      this.customers.filter(customer =>
        customer.name.toLowerCase().includes(this.searchText) ||
        customer.mobile.includes(this.searchText) ||
        (customer.email && customer.email.toLowerCase().includes(this.searchText))
      )
    );
  }

  onCustomerSelected(customer: CustomerDto): void {
    this.selectedCustomer = customer;
    this.onChange(customer.id);
    this.onTouched();
  }

  displayCustomer(customer: CustomerDto | null): string {
    if (!customer) return '';
    return `${customer.name} (${customer.mobile})`;
  }

  // ControlValueAccessor implementation
  writeValue(customerId: number | null): void {
    if (customerId) {
      // If customers are already loaded, find the customer
      if (this.customers.length > 0) {
        this.selectedCustomer = this.customers.find(c => c.id === customerId) || null;
      } else {
        // If customers not loaded yet, load them and then find the customer
        this.customerService.getAllCustomers().subscribe({
          next: (customers) => {
            this.customers = customers;
            this.selectedCustomer = this.customers.find(c => c.id === customerId) || null;
            this.filteredCustomers$ = of(this.customers);
          },
          error: (err) => console.error('Failed to load customers', err)
        });
      }
    } else {
      this.selectedCustomer = null;
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    // Handle disabled state if needed
  }
}
