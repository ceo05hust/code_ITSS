import { Routes } from '@angular/router';
import { PaymentComponent }       from './components/order/payment/payment.component';
import { PaymentFailComponent } from './components/order/payment-fail/payment-fail.component';

export const routes: Routes = [
  { path: '',              component: PaymentComponent, pathMatch: 'full' },
  { path: 'payment',       component: PaymentComponent },
  { path: 'payment-failed', component: PaymentFailComponent },
  { path: '**',            redirectTo: '' },
];
