import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ServiceApiService } from '../../services/service-api.service';
import { ServiceRecord } from '../../models/service-record.model';

@Component({
  selector: 'app-service-record-detail',
  templateUrl: './service-record-detail.component.html',
  styleUrls: ['./service-record-detail.component.scss']
})
export class ServiceRecordDetailComponent implements OnInit {
  record: ServiceRecord | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private serviceApi: ServiceApiService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadRecord(id);
    }
  }

  private loadRecord(id: number): void {
    this.isLoading = true;
    this.serviceApi.getRecord(id).subscribe({
      next: (data) => {
        this.record = data.data || data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load record details', 'Close', { duration: 3000 });
        this.router.navigate(['/service/records']);
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      IN_PROGRESS: 'status-in-progress',
      COMPLETED: 'status-completed',
      WAITING_FOR_PARTS: 'status-waiting'
    };
    return map[status] || '';
  }

  getPaymentClass(status: string): string {
    const map: Record<string, string> = {
      PAID: 'status-paid',
      UNPAID: 'status-unpaid',
      PARTIAL: 'status-partial'
    };
    return map[status] || '';
  }

  editRecord(): void {
    if (this.record) {
      this.router.navigate(['/service/records', this.record.id, 'edit']);
    }
  }

  deleteRecord(): void {
    if (this.record && confirm('Are you sure you want to delete this record?')) {
      this.serviceApi.deleteRecord(this.record.id!).subscribe({
        next: () => {
          this.snackBar.open('Record deleted successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/service/records']);
        },
        error: () => {
          this.snackBar.open('Error deleting record', 'Close', { duration: 3000 });
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/service/records']);
  }
}
