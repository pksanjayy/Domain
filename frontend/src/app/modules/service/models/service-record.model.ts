export interface ServiceRecord {
    id?: number;
    branchId: number;
    branchName?: string;
    serviceBookingId: number;
    serviceBookingRef?: string;
    serviceDate: string;
    odometer?: number;
    workPerformed?: string;
    partsUsed?: string;
    noOfTechnicians?: number;
    technicianHours?: number;
    notes?: string;
    status: 'IN_PROGRESS' | 'COMPLETED' | 'WAITING_FOR_PARTS';
    paymentStatus: 'PAID' | 'UNPAID' | 'PARTIAL';
}
