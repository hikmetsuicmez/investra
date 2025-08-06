"use client";

import AddCustomerDialog from "@/components/dashboard/customer-management/AddCustomerDialog";
import CustomerTable from "@/components/dashboard/customer-management/CustomerTable";
import AddEmployeeDialog from "@/components/dashboard/employee-management/AddEmployeeDialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Plus } from "lucide-react";
import { useState } from "react";

export default function CustomerManagement() {
	const [openDialog, setOpenDialog] = useState(false);

	return (
		<div className="flex-col h-screen bg-gray-100 p-6">
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Müşteri Yönetimi</h1>
				<Button className="flex items-center gap-2 bg-blue-600" onClick={() => setOpenDialog(true)}>
					<Plus size={20} />
					<span>Yeni Müşteri Ekle</span>
				</Button>
			</div>

			<Card className="flex-grow flex flex-col overflow-hidden">
				<CardContent className="flex-grow overflow-auto">
					<CustomerTable customers={[]} />
				</CardContent>
			</Card>

			<AddCustomerDialog open={openDialog} onOpenChange={setOpenDialog} />
		</div>
	);
}
