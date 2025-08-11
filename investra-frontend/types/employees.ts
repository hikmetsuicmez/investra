// User type for employee data
export type User = {
	id: number;
	username: string | null;
	firstName: string;
	lastName: string;
	nationalityNumber: string | null;
	employeeNumber: string | null;
	phoneNumber: string | null;
	email: string;
	role: Role;
};

export type Role = "ADMIN" | "TRADER" | "VIEWER"

// Employee interface for creating new employees
export interface Employee {
	name: string;
	surname: string;
	nationalId: string; // Tc kimlik
	phone: string; // Tel no
	email: string; // E-posta
	role: Role;
}

// Props for AddEmployeeDialog component
export interface AddEmployeeDialogProps {
	open: boolean;
	onOpenChange: (open: boolean) => void;
}

// Props for EditUserDialog component
export interface EditUserDialogProps {
	user: User;
}

// Props for DeleteUserDialog component
export interface DeleteUserDialogProps {
	user: User;
	onDeleted?: () => void;
}

// Props for EmployeeTable component
export interface EmployeeTableProps {
	employees: User[];
}

// User role type
export type UserRole = "ADMIN" | "TRADER" | "VIEWER";
