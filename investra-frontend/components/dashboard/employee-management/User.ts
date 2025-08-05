export type User = {
	id: number;
	username: string | null;
	firstName: string;
	lastName: string;
	nationalityNumber: string | null;
	employeeNumber: string | null;
	phoneNumber: string | null;
	email: string;
	role: "ADMIN" | "TRADER" | "VIEWER";
};