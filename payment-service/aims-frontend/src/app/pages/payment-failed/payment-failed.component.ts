import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-payment-failed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--color-bg); padding: 24px;">
      <div class="card" style="max-width: 560px; width: 100%; text-align: center; padding: 56px 48px; box-shadow: var(--shadow-modal);">

        <!-- Error Icon -->
        <div class="error-icon-wrap">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>

        <!-- Title -->
        <h1 style="font-size: 42px; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 12px;">Payment Failed</h1>
        <p style="font-size: 17px; margin-bottom: 40px; color: var(--color-text-muted); line-height: 1.5;">
          We're sorry, but your transaction could not be processed.
        </p>

        <!-- Reason Box -->
        <div style="background: var(--color-bg-muted); border-radius: var(--radius); padding: 20px 24px; text-align: left; margin-bottom: 40px;">
          <div class="label-caps" style="margin-bottom: 8px;">Reason</div>
          <p style="color: var(--color-text); font-size: 14px; line-height: 1.6;">{{ reason }}</p>
        </div>

        <!-- Actions -->
        <div style="display: flex; gap: 12px;">
          <button class="btn btn-primary" (click)="retry()" style="flex: 1;">Retry Payment</button>
          <button class="btn btn-secondary" (click)="changeMethod()" style="flex: 1;">Change Payment Method</button>
        </div>
      </div>
    </div>
  `
})
export class PaymentFailedComponent {
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
