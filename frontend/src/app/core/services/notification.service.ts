import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Store } from '@ngrx/store';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { selectCurrentUser } from '../auth/store/auth.selectors';
import { NotificationDto } from '../models';
import { ApiResponse, CursorPageResponse } from '../models';

import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private stompClient: any = null;
  private readonly wsUrl = '/ws';
  private readonly apiUrl = '/api/notifications';

  private unreadCountSubject = new BehaviorSubject<number>(0);
  private notificationsSubject = new BehaviorSubject<NotificationDto[]>([]);
  private destroy$ = new Subject<void>();

  unreadCount$ = this.unreadCountSubject.asObservable();
  notifications$ = this.notificationsSubject.asObservable();

  constructor(private http: HttpClient, private store: Store) {}

  connect(): void {
    this.store
      .select(selectCurrentUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        if (user) {
          this.initWebSocket(user.id);
          this.loadUnreadCount();
        } else {
          this.disconnect();
        }
      });
  }

  private initWebSocket(userId: number): void {
    if (this.stompClient?.connected) {
      return;
    }

    try {
      const socket = new SockJS(this.wsUrl);
      this.stompClient = Stomp.over(socket);
      this.stompClient.debug = () => {}; // suppress debug logs

      this.stompClient.connect({}, () => {
        this.stompClient.subscribe(
          `/topic/notifications/${userId}`,
          (message: any) => {
            const notification: NotificationDto = JSON.parse(message.body);
            const current = this.notificationsSubject.value;
            this.notificationsSubject.next([notification, ...current].slice(0, 10));
            this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
          }
        );
      });
    } catch (e) {
      console.error('WebSocket connection failed', e);
    }
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.stompClient = null;
    }
  }

  loadUnreadCount(): void {
    this.http
      .get<ApiResponse<number>>(`${this.apiUrl}/count`)
      .subscribe((res) => {
        this.unreadCountSubject.next(res.data);
      });
  }

  loadNotifications(cursor?: string): Observable<ApiResponse<CursorPageResponse<NotificationDto>>> {
    const params: any = { size: 10 };
    if (cursor) {
      params.cursor = cursor;
    }
    return this.http.get<ApiResponse<CursorPageResponse<NotificationDto>>>(this.apiUrl, { params });
  }

  markAsRead(id: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/read-all`, {});
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnect();
  }
}
