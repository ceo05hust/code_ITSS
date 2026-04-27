import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-success',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-container animate-fade-in text-center">
      <div class="success-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
      </div>
      
      <h1>Thanh toán thành công!</h1>
      <p class="subtitle">Đơn hàng INV-001 của bạn đã được thanh toán.</p>
      
      <div class="receipt">
        <div class="receipt-row">
          <span>Thời gian</span>
          <strong>{{ currentTime | date:'short' }}</strong>
        </div>
        <div class="receipt-row">
          <span>Phương thức</span>
          <strong>VietQR</strong>
        </div>
        <div class="receipt-row total">
          <span>Đã thanh toán</span>
          <span class="highlight">250,000 đ</span>
        </div>
      </div>

      <button class="btn-primary mt-4" (click)="goHome()">
        Quay lại màn hình chính
      </button>
    </div>
  `,
  styles: [`
    .text-center { text-align: center; }
    .success-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 24px;
      background: #10b981;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 8px 24px rgba(16, 185, 129, 0.3);
      animation: popIn 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
    .success-icon svg {
      width: 40px;
      height: 40px;
    }
    @keyframes popIn {
      0% { transform: scale(0); opacity: 0; }
      100% { transform: scale(1); opacity: 1; }
    }
    
    .receipt {
      background: rgba(255, 255, 255, 0.5);
      border-radius: 16px;
      padding: 24px;
      margin-top: 24px;
      text-align: left;
    }
    .receipt-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 15px;
    }
    .receipt-row.total {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px dashed rgba(0,0,0,0.1);
      font-weight: bold;
    }
    .highlight { color: #10b981; font-size: 18px; }
    .mt-4 { margin-top: 32px; }
  `]
})
export class SuccessComponent {
  currentTime = new Date();

  constructor(private router: Router) {}

  goHome() {
    this.router.navigate(['/']);
  }
}
