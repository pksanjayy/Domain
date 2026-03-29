import { Component, OnInit } from '@angular/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AdminService } from '../../services/admin.service';
import { MenuTreeNode } from '../../models/admin.model';

interface FlatMenuNode {
  id: number;
  name: string;
  icon: string;
  path: string;
  displayOrder: number;
  parentId: number | null;
  level: number;
  expandable: boolean;
}

@Component({
  selector: 'app-menu-management',
  templateUrl: './menu-management.component.html',
  styleUrls: ['./menu-management.component.scss'],
})
export class MenuManagementComponent implements OnInit {
  menus: MenuTreeNode[] = [];
  isDrawerOpen = false;
  isEditMode = false;
  editingMenu: FlatMenuNode | null = null;
  menuForm!: FormGroup;
  isSubmitting = false;
  parentOptions: { id: number | null; name: string }[] = [];

  treeControl: FlatTreeControl<FlatMenuNode>;
  treeFlattener: MatTreeFlattener<MenuTreeNode, FlatMenuNode>;
  dataSource: MatTreeFlatDataSource<MenuTreeNode, FlatMenuNode>;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) {
    this.treeFlattener = new MatTreeFlattener(
      (node: MenuTreeNode, level: number): FlatMenuNode => ({
        id: node.id,
        name: node.name,
        icon: node.icon,
        path: node.path,
        displayOrder: node.displayOrder,
        parentId: node.parentId,
        level: level,
        expandable: node.children && node.children.length > 0,
      }),
      (node) => node.level,
      (node) => node.expandable,
      (node) => node.children,
    );
    this.treeControl = new FlatTreeControl<FlatMenuNode>(
      (node) => node.level,
      (node) => node.expandable,
    );
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  }

  ngOnInit(): void {
    this.initForm();
    this.loadMenus();
  }

  initForm(): void {
    this.menuForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      icon: ['', Validators.required],
      path: ['', Validators.required],
      displayOrder: [0, [Validators.required, Validators.min(0)]],
      parentId: [null],
    });
  }

  loadMenus(): void {
    this.adminService.getAllMenus().subscribe({
      next: (res) => {
        this.menus = res.data;
        this.dataSource.data = this.menus;
        this.treeControl.expandAll();
        this.buildParentOptions();
      },
    });
  }

  buildParentOptions(): void {
    this.parentOptions = [{ id: null, name: '— Root Level —' }];
    const flatten = (nodes: MenuTreeNode[], prefix = ''): void => {
      nodes.forEach((n) => {
        this.parentOptions.push({ id: n.id, name: prefix + n.name });
        if (n.children?.length) {
          flatten(n.children, prefix + '  └ ');
        }
      });
    };
    flatten(this.menus);
  }

  hasChild = (_: number, node: FlatMenuNode) => node.expandable;

  openCreateDrawer(): void {
    this.isEditMode = false;
    this.editingMenu = null;
    this.menuForm.reset({ displayOrder: 0, parentId: null });
    this.isDrawerOpen = true;
  }

  openEditDrawer(node: FlatMenuNode): void {
    this.isEditMode = true;
    this.editingMenu = node;
    this.menuForm.patchValue({
      name: node.name,
      icon: node.icon,
      path: node.path,
      displayOrder: node.displayOrder,
      parentId: node.parentId,
    });
    this.isDrawerOpen = true;
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
  }

  onSubmitMenu(): void {
    if (this.menuForm.invalid) {
      this.menuForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const payload = this.menuForm.value;

    if (this.isEditMode && this.editingMenu) {
      this.adminService.updateMenu(this.editingMenu.id, payload).subscribe({
        next: () => {
          this.snackBar.open('Menu updated', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadMenus();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    } else {
      this.adminService.createMenu(payload).subscribe({
        next: () => {
          this.snackBar.open('Menu created', 'Close', { duration: 3000 });
          this.closeDrawer();
          this.loadMenus();
          this.isSubmitting = false;
        },
        error: () => (this.isSubmitting = false),
      });
    }
  }

  deleteMenu(node: FlatMenuNode): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Menu',
        message: `Delete menu "${node.name}"? This cannot be undone.`,
        confirmText: 'Delete',
        confirmColor: 'warn',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.adminService.deleteMenu(node.id).subscribe({
          next: () => {
            this.snackBar.open('Menu deleted', 'Close', { duration: 3000 });
            this.loadMenus();
          },
        });
      }
    });
  }
}
