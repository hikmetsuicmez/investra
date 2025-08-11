"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { Loader2, XCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";


interface ApiPosition {
	stockCode: string;
	stockName: string;
	quantity: number;
	costPrice: number;
	currentPrice: number;
	positionValue: number;
	unrealizedProfitLoss: number;
	changePercentage: number;
}

interface ApiValuation {
	clientId: number;
	clientName: string | null;
	totalPortfolioValue: number;
	unrealizedProfitLoss: number; 
	dailyChangePercentage: number; 
	valuationDate: string;
	positions: ApiPosition[];
}

interface PortfolioItem {
	hisseKodu: string;
	hisseAdi: string;
	adet: number;
	birimFiyat: number;
	maliyet: number; 
	kapanisTutari: number; 
	karZararOrani: number; 
}

interface PortfolioData {
	musteriAdiSoyadi: string;
	musteriNo: string;
	portfoyGuncelDegeri: number;
	toplamMaliyet: number; 
	tumHesapBakiyesi: number;
	hisseler: PortfolioItem[];
}

const formatCurrency = (value: number) => {
	return value.toLocaleString("tr-TR", {
		style: "currency",
		currency: "TRY",
	});
};

export default function CustomerPortfolio() {
	const [portfolio, setPortfolio] = useState<PortfolioData | null>(null);
	const [isLoading, setIsLoading] = useState(true);
	const [error, setError] = useState<string | null>(null);

	const params = useParams();
	const clientId = params.clientId;

	useEffect(() => {
		const fetchPortfolio = async () => {
			if (!clientId) {
				setIsLoading(false);
				setError("Müşteri ID'si bulunamadı.");
				return;
			}

			setIsLoading(true);
			setError(null);
			try {
				const res = await fetch(`/api/portfolios/get-single-client-valuation/${clientId}`);
				const data = await res.json();

				if (!res.ok) {
					throw new Error(data.message || "Portföy bilgileri alınamadı.");
				}

				const valuation: ApiValuation = data.valuation;

            const transformedData: PortfolioData = {
                musteriAdiSoyadi: valuation.clientName || "Bilinmiyor",
                musteriNo: valuation.clientId.toString(),
                portfoyGuncelDegeri: valuation.totalPortfolioValue,
                toplamMaliyet: valuation.totalPortfolioValue, 
                tumHesapBakiyesi: 0, 
                hisseler: valuation.positions.map((pos) => ({
                    hisseKodu: pos.stockCode,
                    hisseAdi: pos.stockName,
                    adet: pos.quantity,
                    birimFiyat: pos.currentPrice,
                    maliyet: pos.quantity * pos.costPrice,
                    kapanisTutari: pos.positionValue,
                    karZararOrani: pos.changePercentage,
                })),
            };


				setPortfolio(transformedData);
			} catch (err: unknown) {
				const errorMessage = err instanceof Error ? err.message : "Bilinmeyen bir hata oluştu.";
				setError(errorMessage);
			} finally {
				setIsLoading(false);
			}
		};

		fetchPortfolio();
	}, [clientId]); 


	if (isLoading) {
		return (
			<div className="flex items-center justify-center h-screen bg-gray-50">
				<div className="text-center">
					<Loader2 className="h-12 w-12 animate-spin mx-auto text-gray-400" />
					<p className="mt-4 text-lg text-gray-600">Portföy Yükleniyor...</p>
				</div>
			</div>
		);
	}

	if (error) {
		return (
			<div className="flex items-center justify-center h-screen bg-gray-50">
				<div className="p-6 bg-red-50 text-red-700 rounded-lg shadow-md flex items-center">
					<XCircle className="mr-3 h-6 w-6" />
					<span>Hata: {error}</span>
				</div>
			</div>
		);
	}

	if (!portfolio) {
		return (
			<div className="flex items-center justify-center h-screen bg-gray-50">
				<p className="text-xl text-gray-500">Gösterilecek portföy verisi bulunamadı.</p>
			</div>
		);
	}

	return (
		<div className="p-4 md:p-8 bg-gray-50 min-h-screen">
			<div className="max-w-7xl mx-auto space-y-6">
				<Card className="shadow-sm">
					<CardContent className="p-6">
						<div className="grid grid-cols-1 md:grid-cols-2 gap-6">
							<div>
								<div className="flex justify-between py-2 border-b">
									<span className="text-sm text-gray-600">Müşteri Adı Soyadı</span>
									<span className="font-medium">{portfolio.musteriAdiSoyadi}</span>
								</div>
								<div className="flex justify-between py-2 border-b">
									<span className="text-sm text-gray-600">Müşteri Id:</span>
									<span className="font-medium">{portfolio.musteriNo}</span>
								</div>
							</div>
							<div className="md:border-l md:pl-6">
								<div className="flex justify-between py-2 border-b">
									<span className="text-sm text-gray-600">Portföy Güncel Değeri</span>
									<span className="font-semibold text-lg">{formatCurrency(portfolio.portfoyGuncelDegeri)}</span>
								</div>
							</div>
						</div>
					</CardContent>
				</Card>

				<Card className="shadow-sm">
					<CardHeader>
						<CardTitle>Müşteri Portföyü</CardTitle>
					</CardHeader>
					<CardContent>
						<div className="border rounded-lg overflow-hidden">
							<Table>
								<TableHeader className="bg-gray-100">
									<TableRow>
										<TableHead className="text-left">Hisse Adı ve Kodu</TableHead>
										<TableHead className="text-right">Adet</TableHead>
										<TableHead className="text-right">Güncel Fiyat</TableHead>
										<TableHead className="text-right">Toplam Maliyet</TableHead>
										<TableHead className="text-right">Pozisyon Değeri</TableHead>
										<TableHead className="text-right">K/Z Oranı</TableHead>
									</TableRow>
								</TableHeader>
								<TableBody>
									{portfolio.hisseler.map((hisse) => (
										<TableRow key={hisse.hisseKodu}>
											<TableCell className="text-left font-medium">
												<div>{hisse.hisseAdi}</div>
												<div className="text-xs text-gray-500">{hisse.hisseKodu}</div>
											</TableCell>
											<TableCell className="text-right">{hisse.adet.toLocaleString("tr-TR")}</TableCell>
											<TableCell className="text-right">{formatCurrency(hisse.birimFiyat)}</TableCell>
											<TableCell className="text-right">{formatCurrency(hisse.maliyet)}</TableCell>
											<TableCell className="text-right">{formatCurrency(hisse.kapanisTutari)}</TableCell>
											<TableCell
												className={`text-right font-semibold ${
													hisse.karZararOrani >= 0 ? "text-green-600" : "text-red-600"
												}`}
											>
												{hisse.karZararOrani.toFixed(2)}%
											</TableCell>
										</TableRow>
									))}
								</TableBody>
							</Table>
						</div>
					</CardContent>
				</Card>
			</div>
		</div>
	);
}