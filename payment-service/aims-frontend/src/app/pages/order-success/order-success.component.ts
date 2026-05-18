import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface OrderItem {
  name: string;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-order-success',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page" style="padding-top: 24px;">
      <div class="container">

        <!-- Go Back -->
        <button (click)="goBack()"
                style="display:inline-flex; align-items:center; gap:6px; background:none; border:none; cursor:pointer; font-size:14px; color:var(--color-text-muted); margin-bottom:24px; font-family:var(--font); padding:0;">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
          Go Back
        </button>

        <!-- Title -->
        <h1 style="font-size: 36px; font-weight: 700; margin-bottom: 32px;">Order Confirmed.</h1>

        <!-- Email Banner -->
        <div class="info-banner" style="margin-bottom: 32px;">
          <div class="info-banner-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
          </div>
          <div>
            <p style="font-weight: 600; color: var(--color-text); margin-bottom: 2px;">Invoice sent to email</p>
            <p style="font-size: 13px;">A digital copy of your order and receipt was sent to customer&#64;example.com</p>
          </div>
        </div>

        <div style="display: flex; gap: 32px; align-items: flex-start;" class="two-col">

          <!-- Left: Transaction + Items + Summary -->
          <div style="flex: 1; min-width: 0;">
            <!-- Transaction Header -->
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
              <div>
                <div class="label-caps" style="margin-bottom: 4px;">Transaction ID</div>
                <p style="font-size: 20px; font-weight: 700; color: var(--color-text); font-family: monospace;">
                  #{{ transactionId }}
                </p>
              </div>
              <div style="text-align: right;">
                <div class="label-caps" style="margin-bottom: 4px;">Status</div>
                <div style="display: flex; align-items: center; gap: 6px; justify-content: flex-end;">
                  <span style="width:8px; height:8px; border-radius:50%; background: #F59E0B; display:inline-block;"></span>
                  <span style="font-weight: 600; font-size: 14px;">Pending Processing</span>
                </div>
              </div>
            </div>
            <div class="divider"></div>

            <!-- Items -->
            <div style="margin: 20px 0;">
              <p style="font-weight: 600; margin-bottom: 16px;">Order Content</p>
              <div *ngFor="let item of items" style="display:flex; align-items:center; gap:16px; margin-bottom:16px;">
                <div style="width:56px; height:70px; background:var(--color-bg-muted); border:1px solid var(--color-border); border-radius:6px; display:flex; align-items:center; justify-content:center; flex-shrink:0;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--color-text-light)" stroke-width="1.5">
                    <rect x="3" y="2" width="13" height="20" rx="1"/><path d="M7 6h5M7 10h5"/>
                  </svg>
                </div>
                <div style="flex:1;">
                  <p style="font-weight:600; color:var(--color-text); margin-bottom:2px;">{{ item.name }}</p>
                  <p style="font-size:13px;">Quantity: {{ item.quantity | number:'2.0-0' }}</p>
                </div>
                <div style="font-weight:600; white-space:nowrap;">{{ formatCurrency(item.price) }} VND</div>
              </div>
            </div>

            <div class="divider"></div>

            <!-- Totals -->
            <div style="margin-top: 16px;">
              <div class="summary-row">
                <span>Subtotal</span>
                <span class="value">{{ formatCurrency(subtotal) }} VND</span>
              </div>
              <div class="summary-row">
                <span>Shipping Fee</span>
                <span class="value">{{ formatCurrency(shippingFee) }} VND</span>
              </div>
              <div class="summary-row">
                <span>Tax (10%)</span>
                <span class="value">{{ formatCurrency(tax) }} VND</span>
              </div>
              <div class="divider"></div>
              <div class="summary-row" style="font-weight: 800;">
                <span style="font-size: 16px;">Total Amount</span>
                <span class="value" style="font-size: 20px;">{{ formatCurrency(totalAmount) }} VND</span>
              </div>
            </div>
          </div>

          <!-- Right: Customer Info -->
          <div style="width: 280px; flex-shrink: 0;">
            <div style="background: var(--color-bg-muted); border-radius: var(--radius-lg); padding: 20px;">
              <p style="font-weight: 700; font-size: 15px; margin-bottom: 2px;">Khanh Yen</p>
              <p style="font-size: 13px; margin-bottom: 20px;">+84123456789</p>

              <div class="label-caps" style="margin-bottom: 6px;">Shipping Address</div>
              <p style="font-size: 13px; margin-bottom: 4px;">23 Phan Chu Trinh</p>
              <p style="font-size: 13px; margin-bottom: 4px;">Cua Nam</p>
              <p style="font-size: 13px; margin-bottom: 20px;">Hanoi</p>

              <div class="label-caps" style="margin-bottom: 6px;">Payment Method</div>
              <div style="display:flex; align-items:center; gap:8px;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
                </svg>
                <span style="font-size: 13px; font-weight: 500;">Pay Via VietQR</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class OrderSuccessComponent {
  transactionId = 'TXN-8829-0012-AIMS';
  totalAmount = 507000;
  shippingFee = 12000;
  tax = 45000;
  get subtotal() { return 450000; }

  items: OrderItem[] = [
    { name: 'Quantum Series Book', quantity: 1, price: 150000 },
    { name: 'Arc Desk Book',       quantity: 2, price: 300000 },
  ];

  constructor(private router: Router) {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as any;
    if (state?.['transactionId']) this.transactionId = state['transactionId'];
    if (state?.['totalAmount'])   this.totalAmount   = state['totalAmount'];
  }

  formatCurrency(n: number): string {
    return n.toLocaleString('vi-VN').replace(/,/g, '.');
  }

  goBack() { this.router.navigate(['/invoice']); }
}
