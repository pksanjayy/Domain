import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceBookingListComponent } from './service-booking-list.component';

describe('ServiceBookingListComponent', () => {
  let component: ServiceBookingListComponent;
  let fixture: ComponentFixture<ServiceBookingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceBookingListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ServiceBookingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
