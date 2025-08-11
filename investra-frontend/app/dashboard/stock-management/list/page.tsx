"use client";

import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useEffect, useState } from "react";
import { Stock } from "@/types/stocks";

export default function StockList() {
	const [stocks, setStocks] = useState<Stock[]>([]);

	async function getStocks() {
		try {
			const response = await fetch("/api/stocks/buy/available");
			if (!response.ok) {
				throw new Error("Failed to fetch users");
			}
			const data = await response.json();

			setStocks(data.data);
		} catch (error) {
			console.error("Error fetching users:", error);
		}
	}

	useEffect(() => {
		getStocks();
	}, []);

	return (
		<div className="flex flex-col h-screen bg-gray-100 p-6">
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Hisse Senetleri</h1>
			</div>

			<Card className="flex-grow flex flex-col overflow-hidden">
				<CardContent className="flex-grow overflow-auto">
					<Table className="text-lg">
						<TableHeader>
							<TableRow>
								<TableHead>Hisse Kodu</TableHead>
								<TableHead>Şirket Adı</TableHead>
								<TableHead>Hisse Türü</TableHead>
								<TableHead>Anlık Fiyat</TableHead>
							</TableRow>
						</TableHeader>
						<TableBody>
							{stocks.map((stock) => (
								<TableRow key={stock.id}>
									<TableCell>{stock.symbol}</TableCell>
									<TableCell>{stock.name}</TableCell>
									<TableCell>{stock.stockGroup}</TableCell>
									<TableCell>{stock.currentPrice}</TableCell>
								</TableRow>
							))}
						</TableBody>
					</Table>
				</CardContent>
			</Card>
		</div>
	);
}
