import { Directive, Input, TemplateRef, ViewContainerRef, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { selectUserRole } from '../../core/auth/store/auth.selectors';

@Directive({ selector: '[hasRole]' })
export class HasRoleDirective implements OnInit, OnDestroy {
  @Input('hasRole') role: string = '';

  private destroy$ = new Subject<void>();
  private isVisible = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private store: Store
  ) {}

  ngOnInit(): void {
    this.store
      .select(selectUserRole)
      .pipe(takeUntil(this.destroy$))
      .subscribe((userRole) => {
        const roles = this.role.split(',').map((r) => r.trim());
        if (userRole && roles.includes(userRole)) {
          if (!this.isVisible) {
            this.viewContainer.createEmbeddedView(this.templateRef);
            this.isVisible = true;
          }
        } else {
          this.viewContainer.clear();
          this.isVisible = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
