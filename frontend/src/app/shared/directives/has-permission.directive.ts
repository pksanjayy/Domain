import { Directive, Input, TemplateRef, ViewContainerRef, OnInit, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { selectUserPermissions } from '../../core/auth/store/auth.selectors';
import { Permission } from '../../core/models';

@Directive({ selector: '[hasPermission]' })
export class HasPermissionDirective implements OnInit, OnDestroy {
  @Input('hasPermission') requiredPermission: { module: string; action: string } = { module: '', action: '' };

  private destroy$ = new Subject<void>();
  private isVisible = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private store: Store
  ) {}

  ngOnInit(): void {
    this.store
      .select(selectUserPermissions)
      .pipe(takeUntil(this.destroy$))
      .subscribe((permissions: Permission[]) => {
        const hasPermission = permissions.some(
          (p) => {
            if (p.moduleName !== this.requiredPermission.module) return false;
            const actionMap: Record<string, boolean> = {
              CREATE: p.canCreate,
              READ: p.canRead,
              UPDATE: p.canUpdate,
              DELETE: p.canDelete,
            };
            return actionMap[this.requiredPermission.action] ?? false;
          }
        );

        if (hasPermission) {
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
