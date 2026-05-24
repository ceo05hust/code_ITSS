import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GenerateQrPayload {
  amount: number;
}

export interface SimulatePaymentPayload {
  externalTransactionId: string;
  amount: number;
}

export interface SwitchMethodPayload {
  externalTransactionId: string;
  method: string;
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
    qrCode: { qrCode: string; qrLink: string; content?: string; bankCode: string; bankName: string; bankAccount: string };
    externalTransactionId: string;
  }>> {
    return this.http.post<any>(`${this.apiUrl}/generate-qr`, payload);
  }

  simulatePayment(payload: SimulatePaymentPayload): Observable<ApiResponse<any>> {
    return this.http.post<any>(`${this.apiUrl}/simulate-payment`, payload);
  }

  getTransaction(externalTransactionId: string): Observable<ApiResponse<{ status: string; transactionId?: number; externalTransactionId: string; amount?: number }>> {
    return this.http.get<any>(`${this.apiUrl}/transaction/${externalTransactionId}`);
  }

  switchMethod(payload: SwitchMethodPayload): Observable<ApiResponse<any>> {
    return this.http.post<any>(`${this.apiUrl}/switch-method`, payload);
  }
}
