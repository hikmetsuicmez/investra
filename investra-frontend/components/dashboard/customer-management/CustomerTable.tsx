"use client";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { CorporateCustomerInfo, IndividualCustomerInfo } from "./AddCustomerDialog";

function mapToCustomerDisplay(customer: IndividualCustomerInfo | CorporateCustomerInfo): {
	name: string;
	type: string;
	phone: string;
	email: string;
	status: string;
} {
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

export default function CustomerTable({
	customers,
}: {
	customers: (IndividualCustomerInfo | CorporateCustomerInfo)[];
}) {
	const displayCustomers = customers.map(mapToCustomerDisplay);

	return (
		<Table className="text-lg">
			<TableHeader>
				<TableRow>
					<TableHead>Müşteri adı</TableHead>
					<TableHead>Müşteri tipi</TableHead>
					<TableHead>Telefon Numarası</TableHead>
					<TableHead>E-posta</TableHead>
					<TableHead>Durum</TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				{displayCustomers.map((customer, index) => (
					<TableRow key={index}>
						<TableCell>{customer.name}</TableCell>
						<TableCell>{customer.type}</TableCell>
						<TableCell>{customer.phone}</TableCell>
						<TableCell>{customer.email}</TableCell>
						<TableCell>{customer.status}</TableCell>
					</TableRow>
				))}
			</TableBody>
		</Table>
	);
}
