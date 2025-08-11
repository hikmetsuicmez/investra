"use client";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { CorporateCustomerInfo, IndividualCustomerInfo, CustomerDisplayInfo, CustomerTableProps } from "@/types/customers";
import DeleteCustomerDialog from "./DeleteCustomerDialog";
import { useRouter } from "next/navigation";

function mapToCustomerDisplay(customer: IndividualCustomerInfo | CorporateCustomerInfo): CustomerDisplayInfo {
	if (customer.clientType == "INDIVIDUAL") {
		// It's an individual
		return {
			name: customer.fullName,
			type: "Bireysel",
			phone: customer.phone,
			email: customer.email,
			status: "Aktif", // or get from some other logic
		};
	} else {
		// It's a corporate
		return {
			name: customer.companyName,
			type: "Kurumsal",
			phone: customer.phone,
			email: customer.email,
			status: "Aktif", // or get from some other logic
		};
	}
}

export default function CustomerTable({ customers }: CustomerTableProps) {
	const router = useRouter();
	const handleRowClick = (customerId: number) => {
		router.push(`/dashboard/customer-management/portfolio/${customerId}`);
	};
	return (
		<Table className="text-lg">
			<TableHeader>
				<TableRow>
					<TableHead>Müşteri adı</TableHead>
					<TableHead>Müşteri tipi</TableHead>
					<TableHead>Telefon Numarası</TableHead>
					<TableHead>E-posta</TableHead>
					<TableHead className="text-center">Durum</TableHead>
					<TableHead></TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				{customers.map((customer, index) => (
					<TableRow key={customer.id} onClick={() => handleRowClick(customer.id)}>
						<TableCell>{mapToCustomerDisplay(customer).name}</TableCell>
						<TableCell>{mapToCustomerDisplay(customer).type}</TableCell>
						<TableCell>{customer.phone}</TableCell>
						<TableCell>{customer.email}</TableCell>
						<TableCell className="text-center">{customer.isActive ? "Aktif" : "Pasif"}</TableCell>
						<TableCell>
							<DeleteCustomerDialog customer={customer} disabled={!customer.isActive} />
						</TableCell>
					</TableRow>
				))}
			</TableBody>
		</Table>
	);
}
