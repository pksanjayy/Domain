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
  private readonly wsUrl = 'http://localhost:8080/ws';
  private readonly apiUrl = '/api/notifications';
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 3;
  private reconnectTimeout: any = null;

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
          // Delay WebSocket connection to ensure backend is ready
          setTimeout(() => {
            this.initWebSocket(user.id);
            this.loadUnreadCount();
          }, 1000);
        } else {
          this.disconnect();
        }
      });
  }

  private initWebSocket(userId: number): void {
    if (this.stompClient?.connected) {
      return;
    }

    // Check if we've exceeded max reconnect attempts
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.info('Max WebSocket reconnect attempts reached. Notifications will not be real-time.');
      return;
    }

    try {
      const socket = new SockJS(this.wsUrl);
      this.stompClient = Stomp.over(socket);
      this.stompClient.debug = () => {}; // suppress debug logs

      this.stompClient.connect(
        {},
        () => {
          // Connection successful - reset reconnect attempts
          this.reconnectAttempts = 0;
          console.info('WebSocket connected successfully');
          
          try {
            this.stompClient.subscribe(
              `/topic/notifications/${userId}`,
              (message: any) => {
                try {
                  const notification: NotificationDto = JSON.parse(message.body);
                  const current = this.notificationsSubject.value;
                  this.notificationsSubject.next([notification, ...current].slice(0, 10));
                  this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
                } catch (e) {
                  console.warn('Error processing notification message', e);
                }
              }
            );
          } catch (e) {
            console.warn('Error subscribing to notifications', e);
          }
        },
        (error: any) => {
          // Connection failed
          this.reconnectAttempts++;
          console.warn(`WebSocket connection failed (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`, error);
          this.stompClient = null;
          
          // Retry connection if under max attempts
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectTimeout = setTimeout(() => {
              console.info('Retrying WebSocket connection...');
              this.initWebSocket(userId);
            }, 3000 * this.reconnectAttempts); // Exponential backoff
          }
        }
      );
      
      // Handle socket errors
      socket.onerror = (error: any) => {
        console.warn('WebSocket error occurred', error);
      };
      
      socket.onclose = () => {
        console.info('WebSocket connection closed');
        this.stompClient = null;
      };
    } catch (e) {
      console.warn('WebSocket initialization failed', e);
      this.stompClient = null;
      this.reconnectAttempts++;
    }
  }

  disconnect(): void {
    // Clear any pending reconnect timeout
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    
    if (this.stompClient) {
      try {
        if (this.stompClient.connected) {
          this.stompClient.disconnect();
        }
      } catch (e) {
        console.warn('Error disconnecting WebSocket', e);
      } finally {
        this.stompClient = null;
      }
    }
    
    // Reset reconnect attempts
    this.reconnectAttempts = 0;
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
