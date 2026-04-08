import { Component, Input, OnInit, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormControl } from '@angular/forms';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { VehicleModelService, VehicleModelDto } from '../../../core/services/vehicle-model.service';

@Component({
  selector: 'app-vehicle-model-selector',
  templateUrl: './vehicle-model-selector.component.html',
  styleUrls: ['./vehicle-model-selector.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => VehicleModelSelectorComponent),
      multi: true
    }
  ]
})
export class VehicleModelSelectorComponent implements OnInit, ControlValueAccessor {
  @Input() label = 'Vehicle Model';
  @Input() placeholder = 'Select or search model';
  @Input() required = false;
  @Input() appearance: 'fill' | 'outline' = 'outline';
  @Input() includeInactive = false;

  searchControl = new FormControl('');
  vehicleModels: VehicleModelDto[] = [];
  filteredModels$!: Observable<VehicleModelDto[]>;
  
  selectedValue: any = null;
  disabled = false;

  private onChange: (value: any) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private vehicleModelService: VehicleModelService) {}

  ngOnInit(): void {
    this.loadModels();
  }

  loadModels(): void {
    const source$ = this.includeInactive 
      ? this.vehicleModelService.getAllModels()
      : this.vehicleModelService.getActiveModels();

    source$.subscribe(models => {
      this.vehicleModels = models;
      this.setupFilter();
    });
  }

  setupFilter(): void {
    this.filteredModels$ = this.searchControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value || ''))
    );
  }

  private _filter(value: string): VehicleModelDto[] {
    const filterValue = value.toLowerCase();
    return this.vehicleModels.filter(model =>
      model.displayName.toLowerCase().includes(filterValue) ||
      model.brand.toLowerCase().includes(filterValue) ||
      model.model.toLowerCase().includes(filterValue)
    );
  }

  onSelectionChange(model: VehicleModelDto): void {
    this.selectedValue = {
      brand: model.brand,
      model: model.model
    };
    this.onChange(this.selectedValue);
    this.onTouched();
  }

  displayFn(value: any): string {
    if (!value) return '';
    if (typeof value === 'string') return value;
    return value.brand && value.model ? `${value.brand} ${value.model}` : '';
  }

  // ControlValueAccessor implementation
  writeValue(value: any): void {
    this.selectedValue = value;
    if (value && value.brand && value.model) {
      this.searchControl.setValue(`${value.brand} ${value.model}`, { emitEvent: false });
    } else {
      this.searchControl.setValue('', { emitEvent: false });
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (isDisabled) {
      this.searchControl.disable();
    } else {
      this.searchControl.enable();
    }
  }
}
