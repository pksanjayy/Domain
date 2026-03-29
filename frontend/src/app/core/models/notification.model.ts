export type NotificationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface NotificationDto {
  id: number;
  title: string;
  message: string;
  module: string;
  priority: NotificationPriority;
  isRead: boolean;
  deepLink: string | null;
  createdAt: string;
}
