export interface ServiceBooking {
    id?: number;
    branchId: number;
    branchName?: string;
    customerId: number;
    customerName?: string;
    customerEmail?: string;
    customerMobile?: string;
    bookingId?: string;
    bookingDate: string;
    preferredServiceDate?: string;
    serviceType: 'FREE' | 'PAID' | 'REPAIR' | 'WARRANTY';
    complaints?: string;
    status: 'CONFIRMED' | 'CANCELLED' | 'RESCHEDULED' | 'COMPLETED' | 'NO_SHOW';
}
