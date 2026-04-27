import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-qr-scan',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-container animate-fade-in text-center">
      <h1>Thanh toán đơn hàng</h1>
      <p class="subtitle">Quét mã QR qua ứng dụng ngân hàng của bạn</p>
      
      <div class="qr-box" *ngIf="qrLink; else loading">
        <img [src]="qrLink" alt="VietQR Code" class="qr-image" />
        <div class="qr-details">
          <p>Số tiền: <strong class="highlight">250,000 đ</strong></p>
          <p>Nội dung: <strong>{{ qrContent }}</strong></p>
        </div>
      </div>

      <ng-template #loading>
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Đang khởi tạo mã QR...</p>
        </div>
      </ng-template>

      <div class="actions">
        <button class="btn-primary" [disabled]="!qrLink || isConfirming" (click)="confirmPayment()">
          {{ isConfirming ? 'Đang kiểm tra...' : 'Tôi đã chuyển tiền xong' }}
        </button>
        <button class="btn-secondary" (click)="goBack()">Hủy thanh toán</button>
      </div>

      <div *ngIf="errorMessage" class="error-msg animate-fade-in">
        {{ errorMessage }}
      </div>
    </div>
  `,
  styles: [`
    .text-center { text-align: center; }
    .qr-box {
      background: white;
      padding: 24px;
      border-radius: 16px;
      margin: 0 auto 32px;
      display: inline-block;
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }
    .qr-image {
      width: 250px;
      height: 250px;
      object-fit: contain;
      margin-bottom: 16px;
    }
    .qr-details p {
      margin: 8px 0;
      font-size: 15px;
    }
    .highlight { color: #4f46e5; font-size: 18px; }
    
    .loading-state {
      padding: 60px 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
    }
    .spinner {
      width: 40px;
      height: 40px;
      border: 4px solid rgba(79, 70, 229, 0.2);
      border-top-color: #4f46e5;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .btn-secondary {
      background: transparent;
      border: 1px solid var(--text-muted);
      color: var(--text-muted);
      padding: 12px 24px;
      border-radius: 12px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s;
    }
    .btn-secondary:hover {
      background: rgba(0,0,0,0.05);
      color: var(--text-main);
      border-color: var(--text-main);
    }
    .error-msg {
      margin-top: 16px;
      color: #ef4444;
      background: #fef2f2;
      padding: 12px;
      border-radius: 8px;
      font-size: 14px;
      border: 1px solid #fecaca;
    }
  `]
})
export class QrScanComponent implements OnInit {
  qrLink: string = '';
  qrContent: string = '';
  isConfirming = false;
  errorMessage = '';

  constructor(
    private router: Router,
    private paymentService: PaymentService
  ) {}

  ngOnInit() {
    this.generateQR();
  }

  generateQR() {
    this.paymentService.generateQr({
      invoiceId: 'INV-001',
      shippingFee: 30000,
      totalProductPriceExVAT: 200000,
      totalProductPriceIncVAT: 220000,
      totalAmount: 250000
    }).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.qrLink = res.data.qrLink;
          this.qrContent = res.data.content || 'AIMS INV-001';
        }
      },
      error: (err) => {
        this.errorMessage = 'Lỗi khởi tạo mã QR. Vui lòng thử lại sau.';
        console.error(err);
      }
    });
  }

  confirmPayment() {
    this.isConfirming = true;
    this.errorMessage = '';
    
    this.paymentService.confirmPayment({ invoiceId: 'INV-001' }).subscribe({
      next: (res) => {
        this.isConfirming = false;
        if (res.success || res.message === 'Invoice already paid') {
          this.router.navigate(['/success']);
        } else {
          this.errorMessage = res.message || 'Chưa nhận được thanh toán. Hãy thử lại.';
        }
      },
      error: (err) => {
        this.isConfirming = false;
        this.errorMessage = 'Thanh toán chưa hoàn tất hoặc VietQR từ chối. Vui lòng kiểm tra lại.';
        console.error(err);
      }
    });
  }

  goBack() {
    this.router.navigate(['/']);
  }
}
