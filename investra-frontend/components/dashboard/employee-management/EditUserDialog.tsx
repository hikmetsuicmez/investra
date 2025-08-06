"use client";

import {
	AlertDialog,
	AlertDialogTrigger,
	AlertDialogContent,
	AlertDialogHeader,
	AlertDialogTitle,
	AlertDialogFooter,
	AlertDialogCancel,
	AlertDialogAction,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Pencil } from "lucide-react";
import { User } from "./User";
import { useState } from "react";
import { toast } from "sonner";

type Props = {
	user: User;
};

export default function EditUserDialog({ user }: Props) {
	const [firstName, setFirstName] = useState(user.firstName);
	const [lastName, setLastName] = useState(user.lastName);
	const [phoneNumber, setPhoneNumber] = useState(user.phoneNumber || "");
	const [email, setEmail] = useState(user.email);
	const [role, setRole] = useState(user.role);

	const handleSave = async () => {
		const updatedFields: Partial<User> = {};

		if (firstName !== user.firstName) updatedFields.firstName = firstName;
		if (lastName !== user.lastName) updatedFields.lastName = lastName;
		if (phoneNumber !== (user.phoneNumber || "")) updatedFields.phoneNumber = phoneNumber;
		if (email !== user.email) updatedFields.email = email;
		if (role !== user.role) updatedFields.role = role;

		if (Object.keys(updatedFields).length === 0) {
			console.log("No changes detected");
			return;
		}

		try {
			const response = await fetch(`/api/users/update-user/${user.employeeNumber}`, {
				method: "PATCH",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(updatedFields),
			});

			if (!response.ok) {
				toast("Kullanıcı bilgileri değiştirilemedi.");
			} else {
				toast("Kullanıcı bilgileri başarıyla değiştirildi.");
			}
		} catch (error) {
			console.error("API Hatası:", error);
		}
	};

	return (
		<AlertDialog>
			<AlertDialogTrigger asChild>
				<Button variant="ghost" size="icon">
					<Pencil size={18} />
				</Button>
			</AlertDialogTrigger>
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle className="text-center">Personeli Düzenle</AlertDialogTitle>
				</AlertDialogHeader>
				<form>
					<div className="grid grid-cols-2 gap-4">
						<div className="flex flex-col gap-2">
							<Label htmlFor="firstName">Adı</Label>
							<Input id="firstName" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
						</div>
						<div className="flex flex-col gap-2">
							<Label htmlFor="lastName">Soyadı</Label>
							<Input id="lastName" value={lastName} onChange={(e) => setLastName(e.target.value)} />
						</div>
						<div className="flex flex-col gap-2">
							<Label htmlFor="email">E-posta</Label>
							<Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
						</div>
						<div className="flex flex-col gap-2">
							<Label htmlFor="phoneNumber">Telefon</Label>
							<Input id="phoneNumber" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} />
						</div>
						<div className="flex flex-col gap-2">
							<Label htmlFor="role">Rol</Label>
							<Select
								value={role}
								onValueChange={(value) => {
									setRole(value as "ADMIN" | "TRADER" | "VIEWER");
								}}
							>
								<SelectTrigger id="role">
									<SelectValue placeholder="Rol Seçin" />
								</SelectTrigger>
								<SelectContent>
									<SelectItem value="ADMIN">Admin</SelectItem>
									<SelectItem value="TRADER">Trader</SelectItem>
									<SelectItem value="VIEWER">Viewer</SelectItem>
								</SelectContent>
							</Select>
						</div>
					</div>
				</form>
				<AlertDialogFooter>
					<AlertDialogCancel>Vazgeç</AlertDialogCancel>
					<AlertDialogAction onClick={handleSave}>Kaydet</AlertDialogAction>
				</AlertDialogFooter>
			</AlertDialogContent>
		</AlertDialog>
	);
}
