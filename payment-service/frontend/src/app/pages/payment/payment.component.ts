import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Navbar -->
    <nav class="navbar">
      <div class="container">
        <span class="navbar-logo">AIMS</span>
        <div class="navbar-cart">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 01-8 0"/>
          </svg>
          <span class="cart-badge">3</span>
        </div>
      </div>
    </nav>

    <div class="page">
      <div class="container">
        <div style="display: flex; gap: 40px; align-items: flex-start;" class="two-col">

          <!-- Left: Payment Info -->
          <div style="flex: 1; min-width: 0;">
            <h1 style="font-size: 32px; font-weight: 700; margin-bottom: 12px;">Complete your order</h1>
            <p style="margin-bottom: 32px;">Please use VietQR to scan the code for instant payment processing.</p>

            <!-- Amount Card -->
            <div class="card" style="margin-bottom: 24px;">
              <div style="text-align: right; margin-bottom: 4px;">
                <span class="amount-unit">VND</span>
              </div>
              <div style="display: flex; align-items: baseline; justify-content: space-between;">
                <span style="font-size: 14px; color: var(--color-text-muted);">Total Amount</span>
                <span class="amount-large">{{ formatCurrency(totalAmount) }}</span>
              </div>

              <div class="divider"></div>

              <div class="summary-row">
                <span style="color: var(--color-text-muted);">Order Reference</span>
                <span style="font-weight: 700; letter-spacing: 0.05em;">{{ externalTransactionId || 'Loading...' }}</span>
              </div>
              <div class="summary-row">
                <span style="color: var(--color-text-muted);">Transfer Content</span>
                <span style="display:flex; align-items:center; gap: 8px; font-weight: 700;">
                  {{ transferContent || 'Loading...' }}
                  <button class="copy-btn" (click)="copyContent()" [title]="copied ? 'Copied!' : 'Copy'">
                    {{ copied ? '✓' : '⎘' }}
                  </button>
                </span>
              </div>
            </div>

            <!-- Back button -->
            <div style="margin-bottom: 32px;">
              <button class="btn btn-secondary" (click)="goBack()">BACK TO REVIEW</button>
            </div>

            <!-- Other Methods -->
            <div>
              <div class="label-caps" style="margin-bottom: 12px;">Other Methods</div>
              <div class="method-row" (click)="switchToPayPal()">
                <div class="method-row-left">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
                  </svg>
                  Pay Order by PayPal
                </div>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
            </div>
          </div>

          <!-- Right: QR Panel -->
          <div style="width: 360px; flex-shrink: 0;">
            <div class="card" style="text-align: center;">
              <!-- Status Badge -->
              <div style="margin-bottom: 20px;">
                <span class="badge badge-dark">
                  <span class="badge-dot"></span>
                  Waiting for payment
                </span>
              </div>

              <!-- QR Code -->
              <div style="display: flex; align-items: center; justify-content: center; min-height: 280px;">
                <div *ngIf="isLoadingQR" style="text-align: center; padding: 40px 0;">
                  <div class="spinner" style="margin-bottom: 16px;"></div>
                  <p>Generating QR code...</p>
                </div>

                <div *ngIf="!isLoadingQR && qrImageUrl"
                     style="padding: 16px; border: 1px solid var(--color-border); border-radius: var(--radius-lg); background: white; display:inline-block;">
                  <img [src]="qrImageUrl" alt="VietQR Payment Code" style="width: 220px; height: 220px; display:block;"/>
                </div>

                <div *ngIf="!isLoadingQR && !qrImageUrl"
                     style="padding: 32px; color: var(--color-text-muted); text-align: center;">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin: 0 auto 12px;">
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  <p>Could not load QR code</p>
                </div>
              </div>

              <!-- Instruction -->
              <div style="margin: 20px 0;">
                <p style="font-size: 15px; font-weight: 600; color: var(--color-text); margin-bottom: 4px;">Open your banking app</p>
                <p style="font-size: 13px;">Scan the code above to pay automatically</p>
              </div>

              <!-- Action Buttons -->
              <div style="display: flex; gap: 10px;">
                <button class="btn btn-primary"
                        [disabled]="isSimulating || isLoadingQR || !externalTransactionId"
                        (click)="simulatePayment()"
                        style="flex: 1;">
                  {{ isSimulating ? 'Simulating...' : ' I have already paid ' }}
                </button>
                <button class="btn btn-secondary" (click)="cancelPayment()" style="flex: 0 0 auto; width: auto; padding: 14px 20px;">
                  Cancel
                </button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  `
})
export class PaymentComponent implements OnInit, OnDestroy {
  totalAmount = 0;
  shippingFee = 0;
  totalProductPriceExVAT = 0;
  totalProductPriceIncVAT = 0;

  externalTransactionId = '';
  qrImageUrl = '';    // URL ảnh tạo từ API ngoài
  transferContent = '';

  isLoadingQR = true;
  isSimulating = false;
  copied = false;
  pollingInterval: any;

  constructor(
    private router: Router, 
    private paymentService: PaymentService,
    private cdr: ChangeDetectorRef
  ) {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as any;
    if (state) {
      this.totalAmount             = state['totalAmount']             ?? 507000;
      this.shippingFee             = state['shippingFee']             ?? 12000;
      this.totalProductPriceExVAT  = state['totalProductPriceExVAT']  ?? 450000;
      this.totalProductPriceIncVAT = state['totalProductPriceIncVAT'] ?? 495000;
    } else {
      this.totalAmount = 507000; this.shippingFee = 12000;
      this.totalProductPriceExVAT = 450000; this.totalProductPriceIncVAT = 495000;
    }
  }

  ngOnInit() {
    this.loadQR();
  }

  loadQR() {
    this.isLoadingQR = true;
    this.cdr.detectChanges();

    this.paymentService.generateQr({
      shippingFee: this.shippingFee,
      totalProductPriceExclVAT: this.totalProductPriceExVAT,
      totalProductPriceInclVAT: this.totalProductPriceIncVAT,
      totalAmount: this.totalAmount,
    }).subscribe({
      next: (res) => {
        this.isLoadingQR = false;
        if (res.success && res.data) {
          const qr = res.data.qrCode;
          
          this.externalTransactionId = res.data.externalTransactionId ?? '';
          this.transferContent = qr?.content ?? ('AIMS ' + this.externalTransactionId);

          // Tạo URL ảnh QR từ chuỗi EMVCo bằng qrserver api
          if (qr?.qrCode) {
            this.qrImageUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=' + encodeURIComponent(qr.qrCode);
          }

          // Bắt đầu polling
          this.startPolling();
        }
        this.cdr.detectChanges(); // Ép Angular cập nhật giao diện
      },
      error: (err) => {
        console.error('Error generating QR:', err);
        this.isLoadingQR = false;
        this.cdr.detectChanges();
      }
    });
  }

  startPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
    this.pollingInterval = setInterval(() => {
      if (!this.externalTransactionId) return;
      this.paymentService.getTransaction(this.externalTransactionId).subscribe({
        next: (res) => {
          if (res.success && res.data?.status === 'SUCCESS') {
            clearInterval(this.pollingInterval);
            this.router.navigate(['/success'], { state: { transactionId: res.data.transactionId, totalAmount: this.totalAmount } });
          }
        },
        error: (err) => {
          console.error('Polling error:', err);
        }
      });
    }, 3000); // Check every 3 seconds
  }

  ngOnDestroy() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  simulatePayment() {
    if (!this.externalTransactionId) return;
    this.isSimulating = true;
    this.cdr.detectChanges();

    this.paymentService.simulatePayment({ externalTransactionId: this.externalTransactionId, amount: this.totalAmount }).subscribe({
      next: (res) => {
        // Just wait for polling to pick up the success state
        console.log('Simulation triggered successfully', res);
      },
      error: (err) => {
        console.error('Error simulating payment', err);
        this.isSimulating = false;
        this.cdr.detectChanges();
      }
    });
  }

  copyContent() {
    if (!this.transferContent) return;
    navigator.clipboard.writeText(this.transferContent).then(() => {
      this.copied = true;
      this.cdr.detectChanges();
      setTimeout(() => {
        this.copied = false;
        this.cdr.detectChanges();
      }, 2000);
    });
  }

  switchToPayPal() {
    if (!this.externalTransactionId) return;
    this.paymentService.switchMethod({ externalTransactionId: this.externalTransactionId, method: 'PayPal' }).subscribe({
      next: () => {
        // TODO: Redirect to the PayPal component/route that the other teammate is working on.
        // For now, we simulate the redirection.
        alert('Chuyển hướng sang trang PayPal...');
        this.router.navigate(['/paypal-payment']); // Change this to actual route when ready
      },
      error: (err) => {
        console.error('Failed to switch method:', err);
      }
    });
  }

  goBack()       { this.router.navigate(['/invoice']); }
  cancelPayment(){ this.router.navigate(['/invoice']); }

  formatCurrency(n: number): string {
    return n.toLocaleString('vi-VN').replace(/,/g, '.');
  }
}
