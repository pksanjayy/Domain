import { Component, Output, EventEmitter } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { selectCurrentUser } from '../../../core/auth/store/auth.selectors';
import { logout } from '../../../core/auth/store/auth.actions';
import { User } from '../../../core/models';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-topbar',
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss'],
})
export class TopbarComponent {
  @Output() toggleSidenav = new EventEmitter<void>();

  user$: Observable<User | null>;
  loading$: Observable<boolean>;

  constructor(private store: Store, private loadingService: LoadingService) {
    this.user$ = this.store.select(selectCurrentUser);
    this.loading$ = this.loadingService.loading$;
  }

  onToggleSidenav(): void {
    this.toggleSidenav.emit();
  }

  onLogout(): void {
    this.store.dispatch(logout());
  }
}
