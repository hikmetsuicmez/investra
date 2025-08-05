import { Alert, AlertDescription } from "@/components/ui/alert";
import {
	AlertDialog,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { validateEmail } from "@/lib/validate-email";
import { UserPlus } from "lucide-react";
import { useEffect, useState } from "react";

interface AddEmployeeDialogProps {
	open: boolean;
	onOpenChange: (open: boolean) => void;
}

interface Employee {
	name: string;
	surname: string;
	nationalId: string; // Tc kimlik
	registrationNumber: string; // Sicil no
	phone: string; // Tel no
	email: string; // E-posta
	role: "ADMIN" | "TRADER" | "VIEWER";
}

export default function AddEmployeeDialog({ open, onOpenChange }: AddEmployeeDialogProps) {
	const [userCreateSuccess, setUserCreateSuccess] = useState(false);
	const [userCreateFail, setUserCreateFail] = useState(false);
	const [failMessage, setFailMessage] = useState("");
	const [isSendingForm, setIsSendingForm] = useState(false);
	const [isFormValid, setIsFormValid] = useState(false);

	const [employee, setEmployee] = useState<Employee>({
		name: "",
		surname: "",
		nationalId: "",
		registrationNumber: "",
		phone: "",
		email: "",
		role: "VIEWER",
	});

	useEffect(() => {
		const validateForm = () => {
			const isValidEmail = validateEmail(employee.email);
			const isValidNationalId = /^\d{11}$/.test(employee.nationalId);
			const isValidPhone = employee.phone.length >= 10;

			return (
				employee.name.trim() !== "" &&
				employee.surname.trim() !== "" &&
				employee.registrationNumber.trim() !== "" &&
				isValidEmail &&
				isValidPhone &&
				isValidNationalId
			);
		};

		setIsFormValid(validateForm());
	}, [employee]);

	async function handleSubmit(e: React.FormEvent) {
		e.preventDefault();
		setIsSendingForm(true);

		try {
			const response = await fetch("/api/users/create-user", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ employee }),
			});

			const data = await response.json();

			if (response.ok) {
				setUserCreateFail(false);
				setUserCreateSuccess(true);

				setTimeout(() => {
					setIsSendingForm(false);
					onOpenChange(false);
				}, 1000);
			} else {
				setUserCreateSuccess(false);
				setUserCreateFail(true);
				setFailMessage(data.message);
				setIsSendingForm(false);
			}
		} catch (error) {
			console.error("Network or server error:", error);
		}
	}

	return (
		<AlertDialog open={open} onOpenChange={onOpenChange}>
			<AlertDialogContent className="max-w-lg rounded-lg shadow-lg p-6 bg-white">
				<AlertDialogHeader>
					<div className="flex justify-center mb-2">
						<div className="bg-blue-600 p-3 rounded-full">
							<UserPlus size={32} color="white" />
						</div>
					</div>
					<AlertDialogTitle className="text-center text-xl font-semibold mb-4">Personel Kayıt Formu</AlertDialogTitle>
				</AlertDialogHeader>

				<form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
					<div className="flex flex-col gap-2">
						<Label htmlFor="name">Ad *</Label>
						<Input
							id="name"
							value={employee.name}
							onChange={(e) => setEmployee((prev) => ({ ...prev, name: e.target.value }))}
							required
						/>
					</div>

					<div className="flex flex-col gap-2">
						<Label htmlFor="surname">Soyad *</Label>
						<Input
							id="surname"
							value={employee.surname}
							onChange={(e) => setEmployee((prev) => ({ ...prev, surname: e.target.value }))}
							required
						/>
					</div>

					<div className="flex flex-col gap-2 col-span-2">
						<Label htmlFor="email">E-posta *</Label>
						<Input
							id="email"
							type="email"
							value={employee.email}
							onChange={(e) => setEmployee((prev) => ({ ...prev, email: e.target.value }))}
							required
						/>
					</div>

					<div className="flex flex-col gap-2">
						<Label htmlFor="nationalId">T.C. Kimlik Numarası *</Label>
						<Input
							id="nationalId"
							value={employee.nationalId}
							onChange={(e) => setEmployee((prev) => ({ ...prev, nationalId: e.target.value }))}
						/>
					</div>

					<div className="flex flex-col gap-2">
						<Label htmlFor="registrationNumber">Sicil Numarası *</Label>
						<Input
							id="registrationNumber"
							value={employee.registrationNumber}
							onChange={(e) => setEmployee((prev) => ({ ...prev, registrationNumber: e.target.value }))}
						/>
					</div>

					<div className="flex flex-col gap-2">
						<Label htmlFor="phone">Telefon Numarası *</Label>
						<Input
							id="phone"
							value={employee.phone}
							onChange={(e) => setEmployee((prev) => ({ ...prev, phone: e.target.value }))}
							required
						/>
					</div>

					<div className="flex flex-col gap-2">
						<Label htmlFor="role">Vatandaşlık Türü *</Label>
						<Select
							onValueChange={(value) =>
								setEmployee((prev) => ({ ...prev, role: value as "ADMIN" | "TRADER" | "VIEWER" }))
							}
							value={employee.role}
						>
							<SelectTrigger id="role" className="w-full">
								<SelectValue placeholder="Rol" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value="ADMIN">Admin</SelectItem>
								<SelectItem value="TRADER">Trader</SelectItem>
								<SelectItem value="VIEWER">Viewer</SelectItem>
							</SelectContent>
						</Select>
					</div>

					{userCreateSuccess ? (
						<UserCreateSuccessDialog userCreateSuccess={userCreateSuccess} />
					) : (
						<UserCreateFailDialog userCreateFail={userCreateFail} failMessage={failMessage} />
					)}

					{/* Buttons */}
					<div className="col-span-2 flex justify-end gap-4 mt-4">
						<AlertDialogCancel type="button" className="px-6 py-2 border rounded-md hover:bg-gray-100">
							İptal
						</AlertDialogCancel>
						<Button
							type="submit"
							className="bg-blue-600 hover:bg-blue-700 px-6 py-2 text-white rounded-md"
							disabled={isSendingForm || !isFormValid}
						>
							Kaydet
						</Button>
					</div>
				</form>
			</AlertDialogContent>
		</AlertDialog>
	);
}

function UserCreateSuccessDialog({ userCreateSuccess }: { userCreateSuccess: boolean }) {
	return (
		<Alert
			className={`border-green-600/70 border-0 border-l-4 bg-green-600/5 transition-all duration-300 col-span-2 ${
				userCreateSuccess ? "opacity-100 max-h-32" : "opacity-0 max-h-0 h-0 p-0 -z-50"
			}`}
		>
			<AlertDescription className="text-green-600">Personel başarıyla eklendi.</AlertDescription>
		</Alert>
	);
}

function UserCreateFailDialog({ userCreateFail, failMessage }: { userCreateFail: boolean; failMessage: string }) {
	return (
		<Alert
			variant="destructive"
			className={`border-red-600/70 border-0 border-l-4 bg-red-600/5 transition-all duration-300 col-span-2 ${
				userCreateFail ? "opacity-100 max-h-32" : "opacity-0 max-h-0 h-0 p-0 -z-50"
			}`}
		>
			<AlertDescription className="text-red-600">
				{failMessage || "Personel eklenirken bir hata oluştu."}
			</AlertDescription>
		</Alert>
	);
}
