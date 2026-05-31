import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PaymentService } from '../../../services/payment.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.scss']
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
    private route: ActivatedRoute,
    private paymentService: PaymentService,
    private cdr: ChangeDetectorRef
  ) {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as any;
    if (state && state['totalAmount']) {
      this.totalAmount             = state['totalAmount'];
      this.shippingFee             = state['shippingFee']             ?? 12000;
      this.totalProductPriceExVAT  = state['totalProductPriceExVAT']  ?? 450000;
      this.totalProductPriceIncVAT = state['totalProductPriceIncVAT'] ?? 495000;
    } else {
      this.totalAmount = 507000; this.shippingFee = 12000;
      this.totalProductPriceExVAT = 450000; this.totalProductPriceIncVAT = 495000;
    }
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['amount']) {
        this.totalAmount = Number(params['amount']);
      }
      this.loadQR();
    });
  }

  loadQR() {
    this.isLoadingQR = true;
    this.cdr.detectChanges();

    this.paymentService.generateQr({
      amount: this.totalAmount
    }).subscribe({
      next: (res) => {
        this.isLoadingQR = false;
        if (res.success && res.data) {
          const qr = res.data.qrCode;
          
          this.externalTransactionId = res.data.externalTransactionId ?? '';
          this.transferContent = qr?.content ?? ('AIMS ' + this.externalTransactionId);

          if (qr?.qrCode) {
            this.qrImageUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=' + encodeURIComponent(qr.qrCode);
          }

          this.startPolling();
        }
        this.cdr.detectChanges();
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
            alert('Thanh toán thành công! (Tiến trình dừng tại đây theo yêu cầu)');
          }
        },
        error: (err) => {
          console.error('Polling error:', err);
        }
      });
    }, 3000);
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
    this.paymentService.switchMethod({ 
      externalTransactionId: this.externalTransactionId, 
      method: 'PayPal',
      amount: this.totalAmount
    }).subscribe({
      next: (res: any) => {
        if (res.success && res.data?.redirectUrl) {
          window.location.href = res.data.redirectUrl;
        } else {
          alert('Không thể lấy được link thanh toán PayPal từ server.');
        }
      },
      error: (err) => {
        console.error('Failed to switch method:', err);
        alert('Lỗi khi chuyển sang thanh toán PayPal');
      }
    });
  }

  goBack()       { alert('Chưa có trang invoice để quay lại'); }
  cancelPayment(){ alert('Chưa có trang invoice để huỷ'); }

  formatCurrency(n: number): string {
    return n.toLocaleString('vi-VN').replace(/,/g, '.');
  }
}
