"use client";

import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useEffect, useState } from "react";
import { Stock } from "@/types/stocks";
import SimulationDateDisplay from "@/components/dashboard/simulation-day/SimulationDayDisplay";

export default function StockClosingPrice() {
	const [stocks, setStocks] = useState<Stock[]>([]);

	async function getStocks() {
		try {
			const response = await fetch("/api/stocks/get-closing-prices");
			if (!response.ok) {
				throw new Error("Failed to fetch closing prices");
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
			<SimulationDateDisplay />
			
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Hisse Kapanış Fiyatları</h1>
			</div>

			<Card className="flex-grow flex flex-col overflow-hidden">
				<CardContent className="flex-grow overflow-auto">
					<Table className="text-lg">
						<TableHeader>
							<TableRow>
								<TableHead>Hisse Kodu</TableHead>
								<TableHead>Şirket Adı</TableHead>
								<TableHead>Hisse Türü</TableHead>
								<TableHead>Kapanış Fiyat</TableHead>
							</TableRow>
						</TableHeader>
						<TableBody>
							{stocks.map((stock) => (
								<TableRow key={stock.stockCode}>  
									<TableCell>{stock.stockCode}</TableCell>   
									<TableCell>{stock.companyName}</TableCell>  
									<TableCell>{stock.sector}</TableCell>        
									<TableCell>{stock.closePrice}</TableCell>
								</TableRow>
							))}
						</TableBody>
					</Table>
				</CardContent>
			</Card>
		</div>
	);
}