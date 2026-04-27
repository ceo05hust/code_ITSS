import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-container animate-fade-in">
      <h1>Xác nhận đơn hàng</h1>
      <p class="subtitle">Vui lòng kiểm tra lại thông tin trước khi thanh toán</p>
      
      <div class="order-summary">
        <div class="summary-row">
          <span>Mã đơn hàng</span>
          <strong>INV-001</strong>
        </div>
        <div class="summary-row">
          <span>Giá sản phẩm (trước VAT)</span>
          <span>200,000 đ</span>
        </div>
        <div class="summary-row">
          <span>Giá sản phẩm (sau VAT)</span>
          <span>220,000 đ</span>
        </div>
        <div class="summary-row">
          <span>Phí vận chuyển</span>
          <span>30,000 đ</span>
        </div>
        <div class="divider"></div>
        <div class="summary-row total">
          <span>Tổng thanh toán</span>
          <span class="highlight">250,000 đ</span>
        </div>
      </div>

      <button class="btn-primary" (click)="proceedToPayment()">
        Thanh toán bằng VietQR
      </button>
    </div>
  `,
  styles: [`
    .order-summary {
      background: rgba(255, 255, 255, 0.4);
      border-radius: 16px;
      padding: 24px;
      margin-bottom: 32px;
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 15px;
    }
    .divider {
      height: 1px;
      background: rgba(0, 0, 0, 0.1);
      margin: 16px 0;
    }
    .total {
      font-weight: 700;
      font-size: 18px;
      margin-bottom: 0;
    }
    .highlight {
      color: var(--primary-color);
      font-size: 20px;
    }
  `]
})
export class CheckoutComponent {
  constructor(private router: Router) {}

  proceedToPayment() {
    this.router.navigate(['/qr-scan']);
  }
}
