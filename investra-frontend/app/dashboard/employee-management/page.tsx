"use client";

import AddEmployeeDialog from "@/components/dashboard/employee-management/AddEmployeeDialog";
import SimulationDateDisplay from "@/components/dashboard/simulation-day/SimulationDayDisplay";
import EmployeeTable from "@/components/dashboard/employee-management/EmployeeTable";
import { User } from "@/types/employees";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Plus } from "lucide-react";
import { useEffect, useState } from "react";

export default function EmployeeManagement() {
	const [openDialog, setOpenDialog] = useState(false);
	const [users, setUsers] = useState<User[]>([]);

	async function getAllUsers() {
		try {
			const response = await fetch("/api/users/get-all");
			if (!response.ok) {
				throw new Error("Failed to fetch users");
			}
			const data = await response.json();

			setUsers(data.data);
		} catch (error) {
			console.error("Error fetching users:", error);
		}
	}

	useEffect(() => {
		getAllUsers();
	}, []);

	return (
		<div className="flex flex-col h-screen bg-gray-100 p-6">
			<SimulationDateDisplay />
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Personel Yönetimi</h1>
				<Button className="flex items-center gap-2 bg-blue-600" onClick={() => setOpenDialog(true)}>
					<Plus size={20} />
					<span>Yeni Personel Ekle</span>
				</Button>
			</div>

			<Card className="flex-grow flex flex-col overflow-hidden">
				<CardContent className="flex-grow overflow-auto">
					<EmployeeTable employees={users} />
				</CardContent>
			</Card>

			<AddEmployeeDialog open={openDialog} onOpenChange={setOpenDialog} />
		</div>
	);
}
