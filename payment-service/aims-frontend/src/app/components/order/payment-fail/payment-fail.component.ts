import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-payment-fail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-fail.component.html',
  styleUrls: ['./payment-fail.component.scss']
})
export class PaymentFailComponent {
  reason = 'Transaction declined by your bank or payment provider.';

  constructor(private router: Router) {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as any;
    if (state?.['reason']) {
      this.reason = state['reason'];
    }
  }

  retry()        { this.router.navigate(['/payment']); }
  changeMethod() { this.router.navigate(['/invoice']); }
}
