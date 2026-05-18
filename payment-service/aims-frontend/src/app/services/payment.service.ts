import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GenerateQrPayload {
  invoiceId: number;
  shippingFee: number;
  totalProductPriceExVAT: number;
  totalProductPriceIncVAT: number;
  totalAmount: number;
}

export interface ConfirmPayload {
  paymentRef: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly apiUrl = '/api/payment';

  constructor(private http: HttpClient) {}

  generateQr(payload: GenerateQrPayload): Observable<ApiResponse<{
    qrCode: { qrCode: string; qrLink: string; content: string; bankCode: string; bankName: string; bankAccount: string };
    paymentRef: string;
  }>> {
    return this.http.post<any>(`${this.apiUrl}/generate-qr`, payload);
  }

  confirmPayment(payload: ConfirmPayload): Observable<ApiResponse<{ transactionId: string; paymentRef: string; status: string }>> {
    return this.http.post<any>(`${this.apiUrl}/confirm`, payload);
  }

  getTransaction(paymentRef: string): Observable<ApiResponse<any>> {
    return this.http.get<any>(`${this.apiUrl}/transaction/${paymentRef}`);
  }
}
