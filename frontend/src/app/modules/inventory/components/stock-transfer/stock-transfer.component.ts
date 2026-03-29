import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Observable, of, Subject } from 'rxjs';
import { debounceTime, switchMap, map, startWith, catchError, takeUntil } from 'rxjs/operators';
import { selectCurrentUser } from '../../../../core/auth/store/auth.selectors';
import { InventoryService } from '../../services/inventory.service';
import { StockTransferDto, VehicleListDto, BranchDto } from '../../models/inventory.model';
import { ColumnDef, FilterField } from '../../../../core/models';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-stock-transfer',
  templateUrl: './stock-transfer.component.html',
  styleUrls: ['./stock-transfer.component.scss'],
})
export class StockTransferComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  transferForm!: FormGroup;
  isSubmitting = false;
  branches: BranchDto[] = [];
  filteredVehicles$!: Observable<VehicleListDto[]>;
  userBranchName = '';

  columns: ColumnDef[] = [
    { field: 'vehicleVin', header: 'Vehicle VIN', sortable: true },
    { field: 'vehicleModel', header: 'Model', sortable: true },
    { field: 'fromBranchName', header: 'From Branch', sortable: true },
    { field: 'toBranchName', header: 'To Branch', sortable: true },
    { field: 'status', header: 'Status', sortable: true },
    { field: 'requestedBy', header: 'Requested By', sortable: true },
    { field: 'requestedAt', header: 'Requested', sortable: true },
  ];

  filterConfig: FilterField[] = [
    {
      field: 'status',
      label: 'Status',
      type: 'select',
      options: [
        { value: 'PENDING', label: 'Pending' },
        { value: 'APPROVED', label: 'Approved' },
        { value: 'REJECTED', label: 'Rejected' },
        { value: 'COMPLETED', label: 'Completed' },
      ],
    },
  ];

  apiUrl = '/api/inventory/transfers/filter';

  constructor(
    private fb: FormBuilder,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private store: Store
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBranches();
    this.setupVehicleAutocomplete();

    this.store.select(selectCurrentUser).pipe(takeUntil(this.destroy$)).subscribe((user) => {
      if (user) {
        this.userBranchName = user.branchName || '';
        this.transferForm.patchValue({ fromBranch: user.branchName });
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    this.transferForm = this.fb.group({
      fromBranch: [{ value: '', disabled: true }],
      toBranchId: [null, [Validators.required]],
      vehicleSearch: ['', [Validators.required]],
      vehicleId: [null, [Validators.required]],
      remarks: ['', [Validators.maxLength(500)]],
    });
  }

  private loadBranches(): void {
    this.inventoryService.getBranches().subscribe({
      next: (res) => {
        this.branches = res.data;
      },
    });
  }

  private setupVehicleAutocomplete(): void {
    this.filteredVehicles$ = this.transferForm.get('vehicleSearch')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      switchMap((value) => {
        if (!value || value.length < 3) return of([]);
        return this.inventoryService.searchVehiclesByVin(value).pipe(
          map((res) => res.data),
          catchError(() => of([]))
        );
      })
    );
  }

  onVehicleSelected(vehicle: VehicleListDto): void {
    this.transferForm.patchValue({ vehicleId: vehicle.id });
  }

  onSubmit(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const request = {
      vehicleId: this.transferForm.get('vehicleId')!.value,
      toBranchId: this.transferForm.get('toBranchId')!.value,
      remarks: this.transferForm.get('remarks')!.value || '',
    };

    this.inventoryService.requestTransfer(request).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open('Transfer requested successfully', 'Close', {
          duration: 3000,
          panelClass: 'success-snackbar',
        });
        this.transferForm.reset();
        this.transferForm.patchValue({ fromBranch: this.userBranchName });
      },
      error: (err) => {
        this.isSubmitting = false;
        this.snackBar.open(err.error?.message || 'Failed to request transfer', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
      },
    });
  }

  approveTransfer(transfer: StockTransferDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Approve Transfer',
        message: `Approve transfer of ${transfer.vehicleVin} from ${transfer.fromBranchName} to ${transfer.toBranchName}?`,
        confirmText: 'Approve',
        color: 'primary',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.inventoryService.approveTransfer(transfer.id).subscribe({
          next: () => {
            this.snackBar.open('Transfer approved', 'Close', {
              duration: 3000,
              panelClass: 'success-snackbar',
            });
          },
        });
      }
    });
  }

  rejectTransfer(transfer: StockTransferDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Reject Transfer',
        message: `Reject transfer of ${transfer.vehicleVin}?`,
        confirmText: 'Reject',
        color: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.inventoryService.rejectTransfer(transfer.id, 'Rejected by manager').subscribe({
          next: () => {
            this.snackBar.open('Transfer rejected', 'Close', {
              duration: 3000,
              panelClass: 'success-snackbar',
            });
          },
        });
      }
    });
  }
}
