import { Routes } from '@angular/router';
import { CheckoutComponent } from './components/checkout/checkout.component';
import { QrScanComponent } from './components/qr-scan/qr-scan.component';
import { SuccessComponent } from './components/success/success.component';

export const routes: Routes = [
  { path: '', component: CheckoutComponent },
  { path: 'qr-scan', component: QrScanComponent },
  { path: 'success', component: SuccessComponent },
  { path: '**', redirectTo: '' }
];
