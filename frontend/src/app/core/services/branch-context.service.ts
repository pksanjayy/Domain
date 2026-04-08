import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { selectCurrentUser } from '../auth/store/auth.selectors';
import { User } from '../models';

/**
 * Central service that manages the active branch context for branch-scoped data access.
 *
 * - SUPER_ADMIN: starts with null (all branches), can pick from dropdown
 * - Other roles: locked to user.branchId, cannot change
 */
@Injectable({ providedIn: 'root' })
export class BranchContextService {
  private activeBranchIdSubject = new BehaviorSubject<number | null>(null);
  private userRole: string | null = null;
  private userBranchId: number | null = null;
  private initialized = false;

  /** Emits the currently active branchId. null means "all branches" (admin view). */
  activeBranchId$: Observable<number | null> = this.activeBranchIdSubject.asObservable();

  constructor(private store: Store) {
    this.store.select(selectCurrentUser).subscribe((user: User | null) => {
      if (user) {
        this.userRole = user.roles?.[0] ?? null;
        this.userBranchId = user.branchId;

        if (this.isAdmin()) {
          // Admin starts with first branch or null (all branches)
          if (!this.initialized) {
            this.activeBranchIdSubject.next(null);
            this.initialized = true;
          }
        } else {
          // Non-admin: locked to their branch
          this.activeBranchIdSubject.next(user.branchId);
          this.initialized = true;
        }
      }
    });
  }

  /** Returns true if the current user is SUPER_ADMIN */
  isAdmin(): boolean {
    return this.userRole === 'SUPER_ADMIN';
  }

  /** Get current active branch ID synchronously */
  getActiveBranchId(): number | null {
    return this.activeBranchIdSubject.value;
  }

  /** Set active branch (only works for admin users) */
  setActiveBranch(branchId: number | null): void {
    if (this.isAdmin()) {
      this.activeBranchIdSubject.next(branchId);
    }
  }

  /** Returns the user's own branch ID (for non-admin users) */
  getUserBranchId(): number | null {
    return this.userBranchId;
  }
}
