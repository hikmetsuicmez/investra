"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Account } from "./Account";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

const InfoRow = ({ label, value }: { label: string; value: React.ReactNode }) => (
	<div className="flex flex-col sm:flex-row justify-between border-b py-2">
		<dt className="font-semibold text-gray-600">{label}</dt>
		<dd className="text-gray-800 text-right">{value}</dd>
	</div>
);

export default function AccountInformation() {
	const { clientId, accountId } = useParams();

	const [account, setAccount] = useState<Account | null>(null);
	const [isLoading, setIsLoading] = useState(true);
	const [error, setError] = useState("");

	useEffect(() => {
		async function fetchAccount() {
			if (!clientId || !accountId) return;

			setIsLoading(true);
			setError("");

			try {
				const res = await fetch(`/api/accounts/${clientId}/account/${accountId}`);
				const data = await res.json();

				if (res.ok && data.data) {
					setAccount(data.data);
				} else {
					setError(data.message || "Hesap bilgileri alınamadı.");
				}
			} catch (err) {
				console.error("Fetch error:", err);
				setError("Sunucuya bağlanırken bir hata oluştu.");
			} finally {
				setIsLoading(false);
			}
		}

		fetchAccount();
	}, [clientId, accountId]);

	return (
		<div className="p-4 md:p-6 space-y-6 bg-gray-50 min-h-screen relative">
			<div className="flex justify-between items-center">
				<h1 className="text-3xl font-bold text-gray-800">Hesap Detayları</h1>
			</div>

			{isLoading && <p className="text-center">Yükleniyor...</p>}
			{error && <p className="text-red-500 text-center p-4 bg-red-100 rounded-md">{error}</p>}

			{account && (
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
					<Card className="lg:col-span-2">
						<CardHeader>
							<CardTitle>{account.nickname}</CardTitle>
						</CardHeader>
						<CardContent>
							<dl className="space-y-2">
								<InfoRow label="Hesap Numarası" value={account.accountNumber} />
								<InfoRow label="IBAN" value={account.iban} />
								<InfoRow
									label="Hesap Türü"
									value={
										<Badge variant={account.accountType === "INVESTMENT" ? "default" : "secondary"}>
											{account.accountType === "INVESTMENT" ? "Yatırım" : "Vadesiz"}
										</Badge>
									}
								/>
								<InfoRow
									label="Ana Takas Hesabı"
									value={
										<span className={`font-bold ${account.isPrimarySettlement ? "text-green-600" : "text-red-600"}`}>
											{account.isPrimarySettlement ? "Evet" : "Hayır"}
										</span>
									}
								/>
								<InfoRow
									label="Bakiye"
									value={
										<span className="font-bold text-lg text-blue-600">
											{account?.balance != null
												? account.balance.toLocaleString("tr-TR", {
														style: "currency",
														currency: account.currency,
												  })
												: "-"}
										</span>
									}
								/>
								<InfoRow
									label="Kullanılabilir Bakiye"
									value={
										<span className="font-bold text-lg text-green-700">
											{account.availableBalance != null
												? account.availableBalance.toLocaleString("tr-TR", {
														style: "currency",
														currency: account.currency,
												  })
												: "-"}
										</span>
									}
								/>
							</dl>
						</CardContent>
					</Card>
					<Card>
						<CardHeader>
							<CardTitle>Kurum Bilgileri</CardTitle>
						</CardHeader>
						<CardContent>
							<dl className="space-y-2">
								<InfoRow label="Aracı Kurum" value={account.brokerName} />
								<InfoRow label="Kurum Hesap No" value={account.accountNumberAtBroker} />
								<InfoRow label="Saklamacı Kurum" value={account.custodianName} />
								<InfoRow label="Oluşturulma Tarihi" value={new Date(account.createdAt).toLocaleDateString("tr-TR")} />
							</dl>
						</CardContent>
					</Card>
				</div>
			)}
		</div>
	);
}
