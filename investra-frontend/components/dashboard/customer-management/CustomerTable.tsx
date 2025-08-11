"use client";

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
	CorporateCustomerInfo,
	IndividualCustomerInfo,
	CustomerDisplayInfo,
	CustomerTableProps,
} from "@/types/customers";
import DeleteCustomerDialog from "./DeleteCustomerDialog";
import { useRouter } from "next/navigation";
import EditCustomerDialog from "./EditCustomerDialog";
import { Client } from "@/types/customers";
import { ClientTypeBadge, StatusBadge } from "./Badges";

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
	const handleRowClick = (customer: IndividualCustomerInfo | CorporateCustomerInfo) => {
		if (!customer.isActive) return;
		router.push(`/dashboard/customer-management/portfolio/${customer.id}`);
	};
	return (
		<Table className="text-lg">
			<TableHeader>
				<TableRow>
					<TableHead>Müşteri adı</TableHead>
					<TableHead>Müşteri tipi</TableHead>
					<TableHead>Telefon Numarası</TableHead>
					<TableHead>E-posta</TableHead>
					<TableHead>Durum</TableHead>
					<TableHead className="text-center">İşlemler</TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				{customers.map((customer) => (
					<TableRow
						key={customer.id}
						onClick={() => customer.id && handleRowClick(customer)}
						className={customer.isActive ? "cursor-pointer" : ""}
					>
						<TableCell className="font-semibold">{mapToCustomerDisplay(customer).name}</TableCell>
						<TableCell>
							<ClientTypeBadge type={customer.clientType} />
						</TableCell>
						<TableCell className="text-sm text-gray-700">{customer.phone}</TableCell>
						<TableCell className="text-sm text-gray-700">{customer.email}</TableCell>
						<TableCell>
							<StatusBadge status={customer.isActive ? "AKTIF" : "PASIF"} />
						</TableCell>
						<TableCell>
							<div onClick={(e) => e.stopPropagation()} className="flex justify-center gap-2">
								<EditCustomerDialog customer={customer as unknown as Client} />
								<DeleteCustomerDialog customer={customer} disabled={!customer.isActive} />
							</div>
						</TableCell>
					</TableRow>
				))}
			</TableBody>
		</Table>
	);
}
