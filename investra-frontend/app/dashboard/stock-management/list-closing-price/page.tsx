"use client";

import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useEffect, useState } from "react";
import { Stock } from "@/types/stocks";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Loader2Icon } from "lucide-react";

export default function StockClosingPrice() {
	const [stocks, setStocks] = useState<Stock[]>([]);
	const [searchQuery, setSearchQuery] = useState("");
	const [loading, setLoading] = useState(false);

	async function getStocks() {
		try {
			setLoading(true);
			const response = await fetch("/api/stocks/get-closing-prices");
			if (!response.ok) {
				throw new Error("Failed to fetch closing prices");
			}
			const data = await response.json();

			setStocks(data.data);
		} catch (error) {
			console.error("Error fetching stocks:", error);
		} finally {
			setLoading(false);
		}
	}

	function formatToTurkishLira(amount: number) {
		return amount.toLocaleString("tr-TR", { style: "currency", currency: "TRY" });
	}

	useEffect(() => {
		getStocks();
	}, []);

	// Filter stocks based on search query (case insensitive)
	const filteredStocks = stocks.filter(
		(stock) =>
			(stock.stockCode ?? "").toLowerCase().includes(searchQuery.toLowerCase()) ||
			(stock.companyName ?? "").toLowerCase().includes(searchQuery.toLowerCase())
	);

	return (
		<div className="flex flex-col h-screen bg-gray-100 p-6">
			<div className="flex justify-between items-center p-4 mb-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Hisse Kapanış Fiyatları</h1>
			</div>

			<Card className="flex-grow flex flex-col overflow-hidden">
				<CardContent className="flex-grow overflow-auto space-y-4">
					<div className="flex flex-col gap-4">
						<div>
							<h1 className="text-lg font-semibold">Hisse Senetleri Listesi</h1>
						</div>
						<Input
							type="text"
							placeholder="Hisse Kodu veya Şirket Adı ara..."
							className="border rounded px-3 py-1 text-lg"
							value={searchQuery}
							onChange={(e) => setSearchQuery(e.target.value)}
						/>
					</div>

					{loading ? (
						<div className="flex justify-center items-center h-64">
							<Loader2Icon className="animate-spin text-gray-600" size={48} />
						</div>
					) : (
						<Table className="text-lg">
							<TableHeader>
								<TableRow>
									<TableHead>Hisse Kodu</TableHead>
									<TableHead>Şirket Adı</TableHead>
									<TableHead>Sektör</TableHead>
									<TableHead>Kapanış Fiyat</TableHead>
									<TableHead>Değişim</TableHead>
									<TableHead>%</TableHead>
									<TableHead>En Yüksek</TableHead>
									<TableHead>En Düşük</TableHead>
								</TableRow>
							</TableHeader>
							<TableBody>
								{filteredStocks.slice(0, 20).map((stock) => {
									const closingPrice = stock.closePrice ?? 0;
									const percentageChange = stock.changePercentage ?? 0;

									const difference = closingPrice * (1 - 1 / (1 + percentageChange / 100));
									return (
										<TableRow key={stock.stockCode}>
											<TableCell className="font-semibold">{stock.stockCode}</TableCell>
											<TableCell className="text-sm">{stock.companyName}</TableCell>
											<TableCell>
												<Badge variant={"outline"}>{stock.sector}</Badge>
											</TableCell>
											<TableCell className="font-semibold">{formatToTurkishLira(closingPrice)}</TableCell>
											<TableCell
												className={
													difference >= 0
														? "text-green-600 font-semibold"
														: difference < 0
														? "text-red-600 font-semibold"
														: ""
												}
											>
												{difference > 0 ? "+" : ""}
												{formatToTurkishLira(Number(difference.toFixed(2)))}
											</TableCell>
											<TableCell>
												<Badge
													variant={"outline"}
													className={
														difference >= 0 ? "bg-green-600 text-white" : difference < 0 ? "bg-red-600 text-white" : ""
													}
												>
													{percentageChange > 0 ? "+" : ""}
													{percentageChange.toFixed(2)}%
												</Badge>
											</TableCell>
											<TableCell className="text-sm">{formatToTurkishLira(stock.highPrice ?? 0)}</TableCell>
											<TableCell className="text-sm">{formatToTurkishLira(stock.lowPrice ?? 0)}</TableCell>
										</TableRow>
									);
								})}
							</TableBody>
						</Table>
					)}
				</CardContent>
			</Card>
		</div>
	);
}
