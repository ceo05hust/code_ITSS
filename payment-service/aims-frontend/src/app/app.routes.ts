import { Routes } from '@angular/router';
import { InvoiceComponent }       from './pages/invoice/invoice.component';
import { PaymentComponent }       from './pages/payment/payment.component';
import { PaymentFailedComponent } from './pages/payment-failed/payment-failed.component';
import { OrderSuccessComponent }  from './pages/order-success/order-success.component';

export const routes: Routes = [
  { path: '',              redirectTo: 'invoice', pathMatch: 'full' },
  { path: 'invoice',       component: InvoiceComponent },
  { path: 'payment',       component: PaymentComponent },
  { path: 'payment-failed', component: PaymentFailedComponent },
  { path: 'success',       component: OrderSuccessComponent },
  { path: '**',            redirectTo: 'invoice' },
];
