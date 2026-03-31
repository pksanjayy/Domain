import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseApiService } from '../../../core/services/base-api.service';
import { ApiResponse, PageResponse, FilterRequest } from '../../../core/models';
import {
  UserListDto,
  CreateUserRequest,
  UpdateUserRequest,
  RoleDetailDto,
  PermissionEntry,
  MenuTreeNode,
  AuditLogDto,
  BranchDto,
} from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService extends BaseApiService {
  private readonly userUrl = '/api/admin/users';
  private readonly roleUrl = '/api/admin/roles';
  private readonly menuUrl = '/api/admin/menus';
  private readonly auditUrl = '/api/admin/audit-logs';
  private readonly branchUrl = '/api/admin/branches';

  constructor(http: HttpClient) {
    super(http);
  }

  // ─── Users ───
  getUsers(params: {
    search?: string,
    roleId?: number,
    branchId?: number,
    isActive?: boolean,
    page?: number,
    size?: number
  } = {}): Observable<ApiResponse<PageResponse<UserListDto>>> {
    let httpParams: any = { 
      page: params.page || 0, 
      size: params.size || 20 
    };
    if (params.search) httpParams.search = params.search;
    if (params.roleId) httpParams.roleId = params.roleId;
    if (params.branchId) httpParams.branchId = params.branchId;
    if (params.isActive !== undefined) httpParams.isActive = params.isActive;
    
    return this.get<PageResponse<UserListDto>>(this.userUrl, httpParams);
  }

  getUser(id: number): Observable<ApiResponse<UserListDto>> {
    return this.get<UserListDto>(`${this.userUrl}/${id}`);
  }

  createUser(request: CreateUserRequest): Observable<ApiResponse<UserListDto>> {
    return this.post<UserListDto>(this.userUrl, request);
  }

  updateUser(id: number, request: UpdateUserRequest): Observable<ApiResponse<UserListDto>> {
    return this.put<UserListDto>(`${this.userUrl}/${id}`, request);
  }

  lockUnlockUser(id: number, lock: boolean): Observable<ApiResponse<UserListDto>> {
    return this.patch<UserListDto>(`${this.userUrl}/${id}/lock`, { lock });
  }

  resetPassword(id: number): Observable<ApiResponse<{ temporaryPassword: string }>> {
    return this.patch<{ temporaryPassword: string }>(`${this.userUrl}/${id}/reset-password`, {});
  }

  // ─── Roles ───
  getRoles(): Observable<ApiResponse<RoleDetailDto[]>> {
    return this.get<RoleDetailDto[]>(this.roleUrl);
  }

  updateRolePermissions(roleId: number, permissions: PermissionEntry[]): Observable<ApiResponse<RoleDetailDto>> {
    return this.put<RoleDetailDto>(`${this.roleUrl}/${roleId}/permissions`, permissions);
  }

  updateRoleMenus(roleId: number, menuIds: number[]): Observable<ApiResponse<RoleDetailDto>> {
    return this.put<RoleDetailDto>(`${this.roleUrl}/${roleId}/menus`, menuIds);
  }

  // ─── Menus ───
  getAllMenus(): Observable<ApiResponse<MenuTreeNode[]>> {
    return this.get<MenuTreeNode[]>(this.menuUrl);
  }

  createMenu(request: any): Observable<ApiResponse<MenuTreeNode>> {
    return this.post<MenuTreeNode>(this.menuUrl, request);
  }

  updateMenu(id: number, request: any): Observable<ApiResponse<MenuTreeNode>> {
    return this.put<MenuTreeNode>(`${this.menuUrl}/${id}`, request);
  }

  deleteMenu(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.menuUrl}/${id}`);
  }

  // ─── Audit Logs ───
  getAuditLogs(params: {
    from?: string;
    to?: string;
    module?: string;
    action?: string;
    userId?: number;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<PageResponse<AuditLogDto>>> {
    return this.get<PageResponse<AuditLogDto>>(this.auditUrl, params);
  }

  exportAuditLogs(params: {
    from?: string;
    to?: string;
    module?: string;
    action?: string;
    userId?: number;
  }): Observable<Blob> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val !== null && val !== undefined) {
        httpParams = httpParams.set(key, String(val));
      }
    });
    return this.http.get(`${this.auditUrl}/export`, {
      params: httpParams,
      responseType: 'blob',
    });
  }

  // ─── Branches ───
  getBranches(params: {
    search?: string,
    isActive?: boolean,
    page?: number,
    size?: number
  } = {}): Observable<ApiResponse<PageResponse<BranchDto>>> {
    let httpParams: any = {
      page: params.page || 0,
      size: params.size || 20
    };
    if (params.search) httpParams.search = params.search;
    if (params.isActive !== undefined) httpParams.isActive = params.isActive;
    
    return this.get<PageResponse<BranchDto>>(this.branchUrl, httpParams);
  }

  getAllBranches(): Observable<ApiResponse<BranchDto[]>> {
    return this.get<BranchDto[]>(`${this.branchUrl}/dropdown`);
  }

  createBranch(request: any): Observable<ApiResponse<BranchDto>> {
    return this.post<BranchDto>(this.branchUrl, request);
  }

  updateBranch(id: number, request: any): Observable<ApiResponse<BranchDto>> {
    return this.put<BranchDto>(`${this.branchUrl}/${id}`, request);
  }

  deleteBranch(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`${this.branchUrl}/${id}`);
  }
}
