import { ModuleWithProviders } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AppComponent } from './app.component';
import { MonitorCommercialCurrentComponent } from './monitor/commercial/current/commercial-current.component';
import { MonitorProCurrentComponent } from './monitor/pro/current/pro-current.component';
import { MonitorGrafanaComponent } from './monitor/grafana/grafana.component';
import { MonitorUniversalCurrentComponent } from './monitor/universal/current/universal-current.component';

const appRoutes: Routes = [
  /*{ path: 'monitor/current', component: MonitorCommercialCurrentComponent },*/
  { path: 'monitor/current', component: MonitorUniversalCurrentComponent },
  { path: 'monitor/history', component: MonitorGrafanaComponent },
  /*{ path: 'setting/openems', component: OpenemsSettingComponent },*/
  { path: '', redirectTo: 'monitor/current', pathMatch: 'full' }
];

export const appRoutingProviders: any[] = [

];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);