import { Component, OnDestroy, OnInit } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit, OnDestroy {
  sidenavOpened = true;
  sidenavMode: 'side' | 'over' = 'side';
  private breakpointSubscription!: Subscription;

  constructor(private breakpointObserver: BreakpointObserver) {}

  ngOnInit() {
    this.breakpointSubscription = this.breakpointObserver
      .observe(['(max-width: 960px)'])
      .subscribe((result) => {
        if (result.matches) {
          this.sidenavMode = 'over';
          this.sidenavOpened = false;
        } else {
          this.sidenavMode = 'side';
          this.sidenavOpened = true;
        }
      });
  }

  ngOnDestroy() {
    if (this.breakpointSubscription) {
      this.breakpointSubscription.unsubscribe();
    }
  }

  toggleSidenav(): void {
    this.sidenavOpened = !this.sidenavOpened;
  }
  
  closeSidenavOnMobile(): void {
    if (this.sidenavMode === 'over') {
      this.sidenavOpened = false;
    }
  }
}
