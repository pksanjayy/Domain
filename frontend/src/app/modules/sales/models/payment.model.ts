export interface Payment {
    id?: number;
    customerId: number;
    customerName?: string;
    paymentDate: string;
    totalPrice?: number;
    amountPaid: number;
    paymentMethod: 'CASH' | 'CREDIT_CARD' | 'UPI' | 'BANK_TRANSFER';
    transactionId?: string;
    paymentStatus: 'PENDING' | 'PARTIAL' | 'PAID';
}
