"use client";

import AddCustomerDialog from "@/components/dashboard/customer-management/AddCustomerDialog";
import CustomerTable from "@/components/dashboard/customer-management/CustomerTable";
import { CorporateCustomerInfo, IndividualCustomerInfo } from "@/types/customers";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Plus } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";

export default function CustomerManagement() {
	const [openDialog, setOpenDialog] = useState(false);
	const [customers, setCustomers] = useState<(IndividualCustomerInfo | CorporateCustomerInfo)[]>([]);

	async function fetchCustomers() {
		try {
			const [activeRes, passiveRes] = await Promise.all([
				fetch("/api/clients/active-clients"),
				fetch("/api/clients/passive-clients"),
			]);

			if (!activeRes.ok || !passiveRes.ok) {
				toast.error("Müşteri listelerini alırken bir sıkıntı oldu.");
			}

			const activeData = await activeRes.json();
			const passiveData = await passiveRes.json();

			// Combine active and passive customers
			const combinedCustomers = [...(activeData.data || []), ...(passiveData.data || [])];

			setCustomers(combinedCustomers);
			toast.success("Müşteri listeleri başarıyla alındı.");
		} catch (error) {
			console.error("Error fetching customers:", error);
		}
	}

	useEffect(() => {
		fetchCustomers();
	}, []);

	return (
		<div className="flex flex-col h-screen bg-gray-100 p-6 overflow-hidden">
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Müşteri Yönetimi</h1>
				<Button className="flex items-center gap-2 bg-blue-600" onClick={() => setOpenDialog(true)}>
					<Plus size={20} />
					<span>Yeni Müşteri Ekle</span>
				</Button>
			</div>

			<Card className="flex-grow flex flex-col overflow-hidden">
				<CardContent className="flex-grow overflow-auto">
					<CustomerTable customers={customers} />
				</CardContent>
			</Card>

			<AddCustomerDialog open={openDialog} onOpenChange={setOpenDialog} />
		</div>
	);
}
