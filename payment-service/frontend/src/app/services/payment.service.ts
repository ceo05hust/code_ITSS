import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GenerateQrPayload {
  invoiceId: string;
  shippingFee: number;
  totalProductPriceExVAT: number;
  totalProductPriceIncVAT: number;
  totalAmount: number;
}

export interface ConfirmPayload {
  invoiceId: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private apiUrl = '/api/payment'; // Proxied to localhost:8080

  constructor(private http: HttpClient) { }

  generateQr(payload: GenerateQrPayload): Observable<any> {
    return this.http.post(`${this.apiUrl}/generate-qr`, payload);
  }

  confirmPayment(payload: ConfirmPayload): Observable<any> {
    return this.http.post(`${this.apiUrl}/confirm`, payload);
  }
}
