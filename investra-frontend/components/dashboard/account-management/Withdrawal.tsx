"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { useRouter } from "next/navigation";
import {
	AlertDialog,
	AlertDialogContent,
	AlertDialogHeader,
	AlertDialogTitle,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogAction,
} from "@/components/ui/alert-dialog";
import SimulationDateDisplay from "@/components/dashboard/simulation-day/SimulationDayDisplay";

export default function Withdrawal({ clientId, accountId }: { clientId: string; accountId: string }) {
	const [description, setDescription] = useState("");
	const [amount, setAmount] = useState("");
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState("");
	const router = useRouter();
	const [showSuccess, setShowSuccess] = useState(false);

	const [confirmationData, setConfirmationData] = useState<{
		amount: number;
		accountNumber: string;
		date: string;
		description: string;
	} | null>(null);

	const [accountInfo, setAccountInfo] = useState<{
		accountNumber: string;
		availableBalance: string;
		balance: string;
		currency: string;
	} | null>(null);

	useEffect(() => {
		async function fetchAccountInfo() {
			try {
				const res = await fetch(`/api/accounts/${clientId}/account/${accountId}`);
				if (!res.ok) throw new Error("Hesap bilgisi alınamadı");
				const data = await res.json();

				setAccountInfo({
					accountNumber: data.data.accountNumber,
					availableBalance: data.data.availableBalance,
					balance: data.data.balance,
					currency: data.data.currency,
				});
			} catch (error) {
				console.error(error);
				setAccountInfo(null);
			}
		}
		fetchAccountInfo();
	}, [clientId, accountId]);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setIsLoading(true);
		setError("");

		if (!amount) {
			setError("Lütfen tutar giriniz.");
			setIsLoading(false);
			return;
		}

		const withdrawal = {
			accountId: Number(accountId),
			clientId: Number(clientId),
			description,
			amount: Number(amount),
		};

		try {
			const res = await fetch(`/api/accounts/${clientId}/account/${accountId}/withdrawal`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(withdrawal),
			});
			const data = await res.json();

			if (!res.ok) {
				throw new Error(data?.message || "İşlem başarısız oldu.");
			}
			setConfirmationData({
				amount: Number(amount),
				accountNumber: accountInfo?.accountNumber || "",
				date: new Date().toLocaleDateString("tr-TR"),
				description: description || "—",
			});

			setShowSuccess(true);
			setDescription("");
			setAmount("");
		} catch (err: unknown) {
			if (err instanceof Error) {
				setError(err.message);
			} else {
				setError("Bir hata oluştu.");
			}
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="min-h-screen flex items-center justify-center px-4">
			<SimulationDateDisplay />

			<Card className="w-full max-w-10xl shadow-md border">
				<CardHeader>
					<CardTitle>Bakiye Çıkış İşlemi</CardTitle>
					<CardDescription>Aşağıdaki formu doldurarak bakiye çıkış işlemini gerçekleştirin.</CardDescription>
				</CardHeader>
				{accountInfo && (
					<div className="flex justify-between px-6 py-4 bg-gray-50 border-b border-gray-200 mb-6 rounded">
						<div className="mr-8">
							<strong>Hesap No:</strong> {accountInfo.accountNumber}
						</div>
						<div className="mr-8">
							<strong>Kullanılabilir Bakiye:</strong>
							{parseFloat(accountInfo.availableBalance).toLocaleString("tr-TR", { minimumFractionDigits: 2 })}{" "}
							{accountInfo.currency}
						</div>
						<div className="mr-8">
							<strong>Güncel Bakiye:</strong>
							{parseFloat(accountInfo.balance).toLocaleString("tr-TR", {
								minimumFractionDigits: 2,
							})}{" "}
							{accountInfo.currency}
						</div>
						<div>
							<strong>Para Birimi:</strong> {accountInfo.currency}
						</div>
					</div>
				)}
				<form onSubmit={handleSubmit}>
					<CardContent className="space-y-6">
						<div className="space-y-2">
							<Label htmlFor="amount">
								Tutar <span className="text-destructive">*</span>
							</Label>
							<Input
								id="amount"
								type="text"
								placeholder="Örn: 10000"
								value={amount}
								onChange={(e) => {
									const val = e.target.value.replace(/[.,]/g, "");
									if (/^\d*$/.test(val)) {
										setAmount(val);
									}
								}}
								required
							/>
						</div>

						<div className="space-y-2">
							<Label htmlFor="description">Açıklama (Opsiyonel)</Label>
							<Textarea
								id="description"
								placeholder="thy için yatırılan para"
								value={description}
								onChange={(e) => setDescription(e.target.value)}
							/>
						</div>
						{error && <p className="text-sm text-red-500">{error}</p>}

						<div className="flex justify-between">
							<Button type="button" variant="outline" onClick={() => router.back()}>
								Geri Dön
							</Button>
							<Button type="submit" disabled={isLoading}>
								{isLoading ? "Yükleniyor..." : "✓ Onayla"}
							</Button>
						</div>
					</CardContent>
				</form>
			</Card>
			{showSuccess && confirmationData && (
				<AlertDialog open={showSuccess} onOpenChange={setShowSuccess}>
					<AlertDialogContent className="bg-green-50 text-green-800">
						<AlertDialogHeader>
							<AlertDialogTitle className="text-green-700 text-lg font-bold">✔ İşlem Başarılı</AlertDialogTitle>
							<AlertDialogDescription className="mt-2 text-sm text-green-800 space-y-1">
								<p>
									<strong>Çıkış Yapılan Tutar:</strong> {confirmationData.amount.toLocaleString("tr-TR")} TRY
								</p>
								<p>
									<strong>İşlem Tarihi:</strong> {confirmationData.date}
								</p>
								<p>
									<strong>Hesap:</strong> {confirmationData.accountNumber}
								</p>
								<p>
									<strong>Açıklama:</strong> {confirmationData.description}
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
