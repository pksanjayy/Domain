import { Component, OnInit, OnDestroy, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { selectUserMenus, selectUserRole } from '../../../core/auth/store/auth.selectors';
import { Menu, RoleName } from '../../../core/models';

@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss'],
  animations: [
    trigger('slideDown', [
      transition(':enter', [
        style({ height: 0, opacity: 0, overflow: 'hidden' }),
        animate('200ms ease-out', style({ height: '*', opacity: 1 })),
      ]),
      transition(':leave', [
        style({ height: '*', opacity: 1, overflow: 'hidden' }),
        animate('200ms ease-in', style({ height: 0, opacity: 0 })),
      ]),
    ]),
  ],
})
export class SidenavComponent implements OnInit, OnDestroy {
  @Output() toggleSidenav = new EventEmitter<void>();

  menus$: Observable<Menu[]>;
  userRole$: Observable<RoleName | null>;
  currentRoute = '';
  expandedMenus: Set<number> = new Set();

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.menus$ = this.store.select(selectUserMenus);
    this.userRole$ = this.store.select(selectUserRole);
  }

  ngOnInit(): void {
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((e: any) => {
        this.currentRoute = e.urlAfterRedirects || e.url;
      });
  }

  onToggleSidenav(): void {
    this.toggleSidenav.emit();
  }

  toggleMenu(menuId: number): void {
    if (this.expandedMenus.has(menuId)) {
      this.expandedMenus.delete(menuId);
    } else {
      this.expandedMenus.add(menuId);
    }
    this.cdr.detectChanges();
  }

  isExpanded(menuId: number): boolean {
    return this.expandedMenus.has(menuId);
  }

  isActive(route: string): boolean {
    return this.currentRoute.startsWith(route);
  }

  navigate(route: string): void {
    if (route) {
      this.router.navigate([route]);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
