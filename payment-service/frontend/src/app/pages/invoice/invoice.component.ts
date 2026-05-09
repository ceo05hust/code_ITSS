import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface OrderItem {
  name: string;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Navbar -->
    <nav class="navbar">
      <div class="container">
        <span class="navbar-logo">AIMS</span>
        <div class="navbar-cart" (click)="null">
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
        <!-- Page Title -->
        <h1 style="margin-bottom: 6px;">REVIEW & CHECKOUT</h1>
        <p style="margin-bottom: 32px; font-size: 13px;">TRANSACTION ID: <strong>#AIMS-{{ transactionId }}</strong></p>

        <div style="display: flex; gap: 32px; align-items: flex-start;" class="two-col">

          <!-- Left Column: Items + Delivery -->
          <div style="flex: 1; min-width: 0;">
            <!-- Order Items -->
            <div class="card" style="margin-bottom: 24px; padding: 0;">
              <div *ngFor="let item of items; let last = last"
                   style="display: flex; align-items: center; gap: 20px; padding: 20px 24px;"
                   [style.border-bottom]="!last ? '1px solid var(--color-border)' : 'none'">
                <div style="width: 64px; height: 80px; background: var(--color-bg-muted); border: 1px solid var(--color-border); border-radius: 6px; display:flex; align-items:center; justify-content:center; flex-shrink:0;">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--color-text-light)" stroke-width="1.5">
                    <rect x="3" y="2" width="13" height="20" rx="1"/><path d="M7 6h5M7 10h5M7 14h3"/>
                    <path d="M17 6l3 3-3 3"/>
                  </svg>
                </div>
                <div style="flex: 1;">
                  <p style="color: var(--color-text); font-weight: 600; font-size: 15px; margin-bottom: 4px;">{{ item.name }}</p>
                </div>
                <div style="text-align:right;">
                  <div class="label-caps" style="margin-bottom: 4px;">Quantity</div>
                  <div style="font-size: 16px; font-weight: 600;">{{ item.quantity | number:'2.0-0' }}</div>
                </div>
                <div style="text-align:right; min-width: 120px;">
                  <div class="label-caps" style="margin-bottom: 4px;">Price</div>
                  <div style="font-size: 17px; font-weight: 700;">{{ formatCurrency(item.price) }} VND</div>
                </div>
              </div>
            </div>

            <!-- Delivery Summary -->
            <div class="card">
              <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 20px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
                  <circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                </svg>
                <h3>DELIVERY SUMMARY</h3>
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                <div>
                  <div class="label-caps" style="margin-bottom: 8px;">Shipping Address</div>
                  <p style="color: var(--color-text); font-weight: 500;">Khanh Yen</p>
                  <p>+84123456789</p>
                  <p>23 Phan Chu Trinh</p>
                  <p>Cua Nam, Hanoi</p>
                </div>
                <div>
                  <div class="label-caps" style="margin-bottom: 8px;">Estimated Arrival</div>
                  <p style="font-size: 28px; font-weight: 700; color: var(--color-text); display: inline;">Oct 24&nbsp;</p>
                  <span style="font-size: 13px; color: var(--color-text-muted);">Express Courier</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Order Summary -->
          <div style="width: 320px; flex-shrink: 0;">
            <div class="card">
              <h3 style="margin-bottom: 20px;">ORDER SUMMARY</h3>

              <div class="summary-row">
                <span>Total (excl. VAT)</span>
                <span class="value">{{ formatCurrency(subtotalExVat) }} VND</span>
              </div>
              <div class="summary-row">
                <span>VAT (10%)</span>
                <span class="value">{{ formatCurrency(vat) }} VND</span>
              </div>
              <div class="summary-row" style="font-weight: 700;">
                <span>TOTAL (INCL. VAT)</span>
                <span class="value" style="font-size: 18px;">{{ formatCurrency(subtotalInclVat) }} VND</span>
              </div>
              <div class="summary-row">
                <span>Shipping Fee</span>
                <span class="value">{{ formatCurrency(shippingFee) }} VND</span>
              </div>

              <div class="divider"></div>

              <div class="summary-row" style="font-weight: 800;">
                <span style="font-size: 15px;">FINAL AMOUNT</span>
                <span class="value" style="font-size: 22px;">{{ formatCurrency(finalAmount) }} VND</span>
              </div>

              <div style="margin-top: 24px; display: flex; flex-direction: column; gap: 12px;">
                <button class="btn btn-primary" (click)="proceedToPayment()">
                  CONFIRM & PAY &nbsp;→
                </button>
                <button class="btn btn-secondary" (click)="goBack()">
                  BACK TO DELIVERY INFO
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class InvoiceComponent {
  transactionId = '928374';

  items: OrderItem[] = [
    { name: 'Quantum Series Book', quantity: 1, price: 150000 },
    { name: 'Arc Desk Book',       quantity: 2, price: 300000 },
  ];

  shippingFee = 12000;

  get subtotalExVat() { return this.items.reduce((s, i) => s + i.price, 0); }
  get vat()           { return Math.round(this.subtotalExVat * 0.1); }
  get subtotalInclVat(){ return this.subtotalExVat + this.vat; }
  get finalAmount()   { return this.subtotalInclVat + this.shippingFee; }

  constructor(private router: Router) {}

  formatCurrency(n: number): string {
    return n.toLocaleString('vi-VN').replace(/,/g, '.');
  }

  proceedToPayment() {
    this.router.navigate(['/payment'], {
      state: {
        totalAmount: this.finalAmount,
        shippingFee: this.shippingFee,
        totalProductPriceExVAT: this.subtotalExVat,
        totalProductPriceIncVAT: this.subtotalInclVat,
      }
    });
  }

  goBack() { window.history.back(); }
}
