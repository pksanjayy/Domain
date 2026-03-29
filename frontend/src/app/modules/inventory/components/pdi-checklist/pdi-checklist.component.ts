import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InventoryService } from '../../services/inventory.service';
import {
  VehicleDetailDto,
  PdiChecklistDto,
  PdiChecklistItemDto,
  PdiItemResult,
} from '../../models/inventory.model';

interface PdiGroup {
  category: string;
  items: PdiChecklistItemDto[];
}

@Component({
  selector: 'app-pdi-checklist',
  templateUrl: './pdi-checklist.component.html',
  styleUrls: ['./pdi-checklist.component.scss'],
})
export class PdiChecklistComponent implements OnInit {
  vehicle: VehicleDetailDto | null = null;
  checklist: PdiChecklistDto | null = null;
  groupedItems: PdiGroup[] = [];
  isLoading = true;
  isSubmitting = false;

  // Photo preview map: itemId → data URL
  photoPreviews: Record<number, string> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private inventoryService: InventoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const vehicleId = Number(this.route.snapshot.paramMap.get('vehicleId'));
    if (vehicleId) {
      this.loadData(vehicleId);
    }
  }

  private loadData(vehicleId: number): void {
    this.isLoading = true;

    // Load vehicle
    this.inventoryService.getVehicle(vehicleId).subscribe({
      next: (res) => {
        this.vehicle = res.data;
      },
    });

    // Load checklist
    this.inventoryService.getPdiChecklist(vehicleId).subscribe({
      next: (res) => {
        this.checklist = res.data;
        this.buildGroups();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load PDI checklist', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
      },
    });
  }

  private buildGroups(): void {
    if (!this.checklist) return;
    const categoryMap = new Map<string, PdiChecklistItemDto[]>();

    this.checklist.items.forEach((item) => {
      const cat = item.category || 'General';
      if (!categoryMap.has(cat)) {
        categoryMap.set(cat, []);
      }
      categoryMap.get(cat)!.push(item);
    });

    this.groupedItems = Array.from(categoryMap.entries()).map(([category, items]) => ({
      category,
      items: items.sort((a, b) => a.sortOrder - b.sortOrder),
    }));
  }

  get completedCount(): number {
    if (!this.checklist) return 0;
    return this.checklist.items.filter((item) => item.result !== null).length;
  }

  get totalCount(): number {
    return this.checklist?.items.length || 0;
  }

  get progressPercent(): number {
    if (this.totalCount === 0) return 0;
    return Math.round((this.completedCount / this.totalCount) * 100);
  }

  get allCompleted(): boolean {
    return this.completedCount === this.totalCount && this.totalCount > 0;
  }

  onResultChange(item: PdiChecklistItemDto, result: PdiItemResult): void {
    item.result = result;
    if (this.checklist) {
      this.inventoryService
        .updatePdiItem(this.checklist.id, item.id, {
          result,
          remarks: item.remarks,
          photoUrl: item.photoUrl,
        })
        .subscribe();
    }
  }

  onRemarksChange(item: PdiChecklistItemDto): void {
    if (this.checklist && item.result) {
      this.inventoryService
        .updatePdiItem(this.checklist.id, item.id, {
          result: item.result,
          remarks: item.remarks,
          photoUrl: item.photoUrl,
        })
        .subscribe();
    }
  }

  onPhotoSelect(event: Event, item: PdiChecklistItemDto): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.photoPreviews[item.id] = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  completePdi(): void {
    if (!this.checklist || !this.allCompleted) return;

    this.isSubmitting = true;
    this.inventoryService.completePdi(this.checklist.id).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open('PDI completed successfully!', 'Close', {
          duration: 3000,
          panelClass: 'success-snackbar',
        });
        this.router.navigate(['/inventory/vehicles']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.snackBar.open(err.error?.message || 'Failed to complete PDI', 'Close', {
          duration: 3000,
          panelClass: 'error-snackbar',
        });
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/inventory/vehicles']);
  }
}
