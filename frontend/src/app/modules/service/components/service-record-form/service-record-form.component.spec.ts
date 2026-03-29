import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceRecordFormComponent } from './service-record-form.component';

describe('ServiceRecordFormComponent', () => {
  let component: ServiceRecordFormComponent;
  let fixture: ComponentFixture<ServiceRecordFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceRecordFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ServiceRecordFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
