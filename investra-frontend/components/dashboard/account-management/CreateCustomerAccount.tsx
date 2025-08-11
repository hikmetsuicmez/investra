"use client";

import { useState, useEffect } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";
import SimulationDateDisplay from "@/components/dashboard/simulation-day/SimulationDayDisplay";
import {
	AlertDialog,
	AlertDialogContent,
	AlertDialogHeader,
	AlertDialogTitle,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogAction,
} from "@/components/ui/alert-dialog";
import { useRouter } from "next/navigation";

interface CreateCustomerAccountProps {
	clientId: string;
}

export default function CreateCustomerAccount({ clientId }: CreateCustomerAccountProps) {
	const [form, setForm] = useState({
		nickname: "",
		accountType: "",
		currency: "",
		brokerName: "",
		brokerCode: "",
		custodianName: "",
		custodianCode: "",
		iban: "",
		accountNumberAtBroker: "",
		initialBalance: 0,
		clientId: "", // İlk başta boş
	});

	useEffect(() => {
		setForm((prev) => ({ ...prev, clientId }));
	}, [clientId]);
	const router = useRouter();
	const [error, setError] = useState("");
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [showSuccess, setShowSuccess] = useState(false);
	const [confirmationData, setConfirmationData] = useState<{
		nickname: string;
		accountType: string;
		currency: string;
	} | null>(null);

	const handleChange = (field: string, value: string) => {
		setForm((prev) => ({ ...prev, [field]: value }));
	};

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setError("");

		const requiredFields = [
			"accountType",
			"currency",
			"brokerName",
			"brokerCode",
			"custodianName",
			"custodianCode",
			"iban",
			"accountNumberAtBroker",
			"clientId",
		];

		for (const field of requiredFields) {
			if (!form[field as keyof typeof form]) {
				setError("Lütfen tüm zorunlu alanları doldurunuz.");
				return;
			}
		}

		setIsSubmitting(true);

		try {
			const res = await fetch(`/api/accounts/${clientId}/create-account`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(form),
			});

			const data = await res.json();

			if (!res.ok) {
				throw new Error(data?.message || "Hesap oluşturulamadı.");
			}

			setConfirmationData({
				nickname: form.nickname || "—",
				accountType: form.accountType,
				currency: form.currency,
			});
			setShowSuccess(true);
		} catch (err: unknown) {
			if (err instanceof Error) {
				setError(err.message);
			} else {
				setError("Bir hata oluştu.");
			}
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<div className="max-w-3xl mx-auto py-8 px-4">
			<SimulationDateDisplay />

			<Card>
				<CardHeader>
					<CardTitle>Portföy Yöneticisi Hesap Açma</CardTitle>
					<CardDescription>Aşağıdaki formu doldurarak yeni bir hesap açabilirsiniz.</CardDescription>
				</CardHeader>
				<CardContent className="space-y-6">
					<form onSubmit={handleSubmit} className="space-y-4">
						<div>
							<Label htmlFor="nickname">Hesap Adı / Rumuz</Label>
							<Input
								id="nickname"
								placeholder="Örn: Ana Hesap"
								value={form.nickname}
								onChange={(e) => handleChange("nickname", e.target.value)}
							/>
						</div>

						<div className="grid grid-cols-2 gap-4">
							<div className="grid grid-cols-2 gap-4">
								<div>
									<Label>
									Hesap Tipi <span className="text-red-500">*</span>
									</Label>
									<Select
									defaultValue="SETTLEMENT"
									onValueChange={(val) => handleChange("accountType", val)}
									>
									<SelectTrigger>
										<SelectValue placeholder="Seçiniz" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="SETTLEMENT">Takas</SelectItem>
										<SelectItem value="DEPOSIT" disabled>Mevduat</SelectItem>
										<SelectItem value="BLOKED" disabled>Bloke</SelectItem>
									</SelectContent>
									</Select>
								</div>
							</div>
							<div>
								<Label>
									Para Birimi <span className="text-red-500">*</span>
								</Label>
								<Select onValueChange={(val) => handleChange("currency", val)}>
									<SelectTrigger>
										<SelectValue placeholder="Seçiniz" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="TRY">TRY</SelectItem>
										<SelectItem value="USD">USD</SelectItem>
										<SelectItem value="EUR">EUR</SelectItem>
									</SelectContent>
								</Select>
							</div>
						</div>

						<div className="grid grid-cols-2 gap-4">
							<div>
								<Label>
									Aracı Kurum Adı <span className="text-red-500">*</span>
								</Label>
								<Input
									placeholder="Örn: Vakıf Yatırım"
									value={form.brokerName}
									onChange={(e) => handleChange("brokerName", e.target.value)}
								/>
							</div>
							<div>
								<Label>
									Aracı Kurum Kodu <span className="text-red-500">*</span>
								</Label>
								<Input
									placeholder="Örn: 123"
									value={form.brokerCode}
									onChange={(e) => handleChange("brokerCode", e.target.value)}
								/>
							</div>
						</div>

						<div className="grid grid-cols-2 gap-4">
							<div>
								<Label>
									Saklama Kurumu Adı <span className="text-red-500">*</span>
								</Label>
								<Input
									placeholder="Örn: Takasbank"
									value={form.custodianName}
									onChange={(e) => handleChange("custodianName", e.target.value)}
								/>
							</div>
							<div>
								<Label>
									Saklama Kurumu Kodu <span className="text-red-500">*</span>
								</Label>
								<Input
									placeholder="Örn: 456"
									value={form.custodianCode}
									onChange={(e) => handleChange("custodianCode", e.target.value)}
								/>
							</div>
						</div>

						<div className="grid grid-cols-2 gap-4">
							<div>
								<Label>
									IBAN No <span className="text-red-500">*</span>
								</Label>
								<Input
									placeholder="TR12 3456 7890 1234 5678 9012 34"
									value={form.iban}
									onChange={(e) => handleChange("iban", e.target.value)}
								/>
							</div>
							<div>
								<Label>
									Hesap No <span className="text-red-500">*</span>
								</Label>
								<Input
									placeholder="123456789"
									value={form.accountNumberAtBroker}
									onChange={(e) => handleChange("accountNumberAtBroker", e.target.value)}
								/>
							</div>
						</div>

						{error && <p className="text-red-500 text-sm">{error}</p>}

						<div className="flex justify-end">
							<Button type="submit" disabled={isSubmitting}>
								{isSubmitting ? "Kaydediliyor..." : "Hesap Aç"}
							</Button>
						</div>
					</form>
				</CardContent>
			</Card>
			{showSuccess && confirmationData && (
				<AlertDialog open={showSuccess} onOpenChange={setShowSuccess}>
					<AlertDialogContent className="bg-green-50 text-green-800">
						<AlertDialogHeader>
							<AlertDialogTitle className="text-green-700 text-lg font-bold">
								✔ Hesap Başarıyla Oluşturuldu
							</AlertDialogTitle>
							<AlertDialogDescription className="mt-2 text-sm text-green-800 space-y-1">
								<p>
									<strong>Hesap Adı:</strong> {confirmationData.nickname}
								</p>
								<p>
									<strong>Hesap Tipi:</strong> {confirmationData.accountType}
								</p>
								<p>
									<strong>Para Birimi:</strong> {confirmationData.currency}
								</p>
							</AlertDialogDescription>
						</AlertDialogHeader>

						<AlertDialogFooter>
							<AlertDialogAction
								onClick={() => {
									setShowSuccess(false);
									router.push("/dashboard");
								}}
							>
								Tamam
							</AlertDialogAction>
						</AlertDialogFooter>
					</AlertDialogContent>
				</AlertDialog>
			)}
		</div>
	);
}
