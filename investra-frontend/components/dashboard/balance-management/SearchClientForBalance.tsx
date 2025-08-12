"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useState } from "react";
import { Client, SearchType } from "@/types/customers";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

import SearchAccountByClientId from "./SearchAccountsForBalance";

export default function SearchClientForBalance() {
	const [searchTerm, setSearchTerm] = useState("");
	const [client, setClient] = useState<Client | null>(null);
	const [isLoading, setIsLoading] = useState(false);
	const [notFound, setNotFound] = useState(false);
	const [searchType, setSearchType] = useState<SearchType>("TCKN");
	const [selectedClientId, setSelectedClientId] = useState<number | null>(null);

	async function handleSearch() {
		if (!searchTerm.trim()) return;

		setIsLoading(true);
		setNotFound(false);

		try {
			const token = localStorage.getItem("token");
			const res = await fetch("/api/clients/get-client-by-search-term", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					Authorization: `Bearer ${token}`,
				},
				body: JSON.stringify({
					searchTerm: searchTerm.trim(),
					searchType,
					isActive: true,
				}),
			});

			const data = await res.json();
			if (res.ok && data.client) {
				setClient(data.client);
				setNotFound(false);
				setSelectedClientId(null); // Önceki seçim temizlenebilir
			} else {
				setClient(null);
				setNotFound(true);
				setSelectedClientId(null);
			}
		} catch (err) {
			console.error("Arama hatası:", err);
			setClient(null);
			setNotFound(true);
			setSelectedClientId(null);
		} finally {
			setIsLoading(false);
		}
	}

	function handleSelectCustomer() {
		if (client?.id) {
			setSelectedClientId(client.id);
		}
	}

	return (
		<div className="flex flex-col h-screen bg-gray-100 p-12 space-y-6">
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0 bg-white rounded-xl shadow">
				<h1 className="text-2xl font-semibold">Bakiye İşlemleri</h1>
			</div>
			<div className="flex justify-between items-center p-4 mb-4 rounded-xl ">
				<h6>İşlem yapmak istediğiniz müşteriyi bilgileri girerek arayınız</h6>
			</div>

			<div className="bg-white rounded-xl shadow p-6 space-y-4">
				<p className="text-xs text-muted-foreground">Müşteri İsim• Müşteri Numarası</p>

				<div className="flex items-center gap-4">
					<Input
						placeholder="Arama yapın..."
						value={searchTerm}
						onChange={(e) => setSearchTerm(e.target.value)}
						className="flex-grow"
					/>

					<Select value={searchType} onValueChange={(value) => setSearchType(value as SearchType)}>
						<SelectTrigger className="w-48">
							<SelectValue placeholder="Arama Tipi" />
						</SelectTrigger>
						<SelectContent>
							<SelectItem value="ISIM">Ad - Soyad</SelectItem>
							<SelectItem value="MUSTERI_NUMARASI">Müşteri Numarası</SelectItem>
						</SelectContent>
					</Select>

					<Button onClick={handleSearch} disabled={isLoading}>
						Ara
					</Button>
				</div>
			</div>

			{client && (
				<div
					className="bg-white rounded-xl shadow overflow-auto border cursor-pointer"
					onClick={handleSelectCustomer}
				>
					<div className="grid grid-cols-8 font-semibold bg-gray-100 p-4 text-sm md:text-base">
						<div>Ad Soyad</div>
						<div>Müşteri No</div>
						<div>TCKN</div>
						<div>Vergi No</div>
						<div>Telefon</div>
						<div>E-posta</div>
						<div className="pl-10">Durum</div>
						<div></div>
					</div>

					<div className="grid grid-cols-8 items-center p-4 hover:bg-gray-100 transition">
						<div>{client.fullName}</div>
						<div>{client.id}</div>
						<div>{client.nationalityNumber}</div>
						<div>{client.taxId || "-"}</div>
						<div>{client.phone || "-"}</div>
						<div>{client.email || "-"}</div>
						<div className={client.isActive ? "text-green-600 pl-10" : "text-red-600 pl-10"}>
							{client.isActive ? "Aktif" : "Pasif"}
						</div>
					</div>
				</div>
			)}

			{selectedClientId && (
				<div className="mt-8">
					<SearchAccountByClientId clientId={selectedClientId} />
				</div>
			)}

			{notFound && <p className="text-sm text-red-500 bg-white p-4 rounded-xl shadow">Müşteri bulunamadı.</p>}
		</div>
	);
}
