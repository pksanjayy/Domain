import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';

import { LayoutComponent } from './components/layout/layout.component';
import { SidenavComponent } from './components/sidenav/sidenav.component';
import { TopbarComponent } from './components/topbar/topbar.component';

@NgModule({
  declarations: [LayoutComponent, SidenavComponent, TopbarComponent],
  imports: [SharedModule, RouterModule],
  exports: [LayoutComponent],
})
export class LayoutModule {}
