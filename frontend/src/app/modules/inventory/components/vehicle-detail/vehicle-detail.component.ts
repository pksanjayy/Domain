import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InventoryService } from '../../services/inventory.service';
import { VehicleDetailDto } from '../../models/inventory.model';

@Component({
  selector: 'app-vehicle-detail',
  templateUrl: './vehicle-detail.component.html',
  styleUrls: ['./vehicle-detail.component.scss'],
})
export class VehicleDetailComponent implements OnInit {
  vehicle: VehicleDetailDto | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadVehicle(id);
    }
  }

  private loadVehicle(id: number): void {
    this.isLoading = true;
    this.inventoryService.getVehicle(id).subscribe({
      next: (response) => {
        this.vehicle = response.data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load vehicle details', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
        this.router.navigate(['/inventory/vehicles']);
      },
    });
  }

  getStatusClass(status: string): string {
    const classMap: Record<string, string> = {
      IN_TRANSIT: 'status-transit',
      IN_STOCK: 'status-stock',
      PDI_PENDING: 'status-pdi-pending',
      PDI_PASSED: 'status-pdi-passed',
      PDI_FAILED: 'status-pdi-failed',
      AVAILABLE: 'status-available',
      RESERVED: 'status-reserved',
      SOLD: 'status-sold',
      DELIVERED: 'status-delivered',
    };
    return classMap[status] || '';
  }

  getAgeClass(ageDays: number): string {
    if (ageDays <= 30) return 'age-green';
    if (ageDays <= 60) return 'age-amber';
    if (ageDays <= 90) return 'age-orange';
    return 'age-red';
  }

  editVehicle(): void {
    if (this.vehicle) {
      this.router.navigate(['/inventory/vehicles', this.vehicle.id, 'edit']);
    }
  }

  deleteVehicle(): void {
    if (this.vehicle && confirm(`Are you sure you want to delete vehicle ${this.vehicle.vin}?`)) {
      this.inventoryService.deleteVehicle(this.vehicle.id).subscribe({
        next: () => {
          this.snackBar.open('Vehicle deleted successfully', 'Close', { duration: 3000, panelClass: 'success-snackbar' });
          this.router.navigate(['/inventory/vehicles']);
        },
        error: () => {
          this.snackBar.open('Failed to delete vehicle', 'Close', { duration: 3000, panelClass: 'error-snackbar' });
        }
      });
    }
  }

  goToGrn(): void {
    this.router.navigate(['/inventory/grn/new']);
  }

  goToPdi(): void {
    if (this.vehicle) {
      this.router.navigate(['/inventory/pdi', this.vehicle.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/inventory/vehicles']);
  }
}
