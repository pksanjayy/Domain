import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceBookingFormComponent } from './service-booking-form.component';

describe('ServiceBookingFormComponent', () => {
  let component: ServiceBookingFormComponent;
  let fixture: ComponentFixture<ServiceBookingFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceBookingFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ServiceBookingFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
