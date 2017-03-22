import { Component, Input } from '@angular/core';

import { Device } from '../../../shared/shared';

@Component({
  selector: 'energytable',
  templateUrl: './energytable.component.html'
})
export class DeviceOverviewEnergytableComponent {

  @Input()
  private device: Device;
}
