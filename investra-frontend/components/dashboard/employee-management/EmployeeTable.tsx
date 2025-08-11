import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { EmployeeTableProps } from "@/types/employees";
import EditUserDialog from "./EditUserDialog";
import DeleteUserDialog from "./DeleteUserDialog";

export default function EmployeeTable({ employees }: EmployeeTableProps) {
	return (
		<Table className="text-lg">
			<TableHeader>
				<TableRow>
					<TableHead>Personel adı</TableHead>
					<TableHead>Telefon Numarası</TableHead>
					<TableHead>E-posta</TableHead>
					<TableHead>Rol</TableHead>
					<TableHead>Durum</TableHead>
					<TableHead className="text-center">İşlemler</TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				{employees.map((employee) => (
					<TableRow key={employee.id}>
						<TableCell>{employee.firstName + " " + employee.lastName}</TableCell>
						<TableCell>{employee.phoneNumber}</TableCell>
						<TableCell>{employee.email}</TableCell>
						<TableCell>{employee.role}</TableCell>
						<TableCell>{employee.isActive ? "Aktif" : "Pasif"}</TableCell>
						<TableCell className="flex justify-center gap-2">
							<EditUserDialog user={employee} />
							<DeleteUserDialog user={employee} />
						</TableCell>
					</TableRow>
				))}
			</TableBody>
		</Table>
	);
}
