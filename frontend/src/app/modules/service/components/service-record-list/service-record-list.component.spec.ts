import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceRecordListComponent } from './service-record-list.component';

describe('ServiceRecordListComponent', () => {
  let component: ServiceRecordListComponent;
  let fixture: ComponentFixture<ServiceRecordListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceRecordListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ServiceRecordListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
