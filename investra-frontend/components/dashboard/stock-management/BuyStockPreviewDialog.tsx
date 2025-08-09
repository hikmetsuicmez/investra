"use client";

import { Dialog, DialogContent, DialogFooter, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { EyeIcon } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { BuyStockPreviewDialogProps, BuyOrderResults } from "@/types/stocks";

export default function BuyStockPreviewDialog({
	selectedStock,
	quantity,
	totalCost,
	selectedAccount,
	executionType,
}: BuyStockPreviewDialogProps) {
	const [previewFailed, setPreviewFailed] = useState(false);
	const [previewResults, setPreviewResults] = useState<BuyOrderResults>({
		accountNumber: "",
		operation: "",
		stockName: "",
		stockSymbol: "",
		price: 0,
		quantity: 0,
		tradeDate: "",
		valueDate: "",
		totalAmount: 0,
		stockGroup: "",
		commission: 0,
		bsmv: 0,
		totalTaxAndCommission: 0,
		netAmount: 0,
		executionType: "",
	});

	async function handlePreview() {
		try {
			const order = {
				clientId: selectedAccount.clientId,
				accountId: selectedAccount.id,
				stockId: selectedStock.id,
				executionType: executionType,
				quantity: quantity,
				price: selectedStock.currentPrice,
			};

			const res = await fetch("/api/stocks/buy/preview", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ order }),
			});

			const result = await res.json();

			toast(result.message);

			if (res.ok) {
				console.log(result);
				setPreviewResults(result.data);
			} else {
				setPreviewFailed(true);
			}
		} catch (error) {
			console.error("Ön izleme sırasında hata oluştu: ", error);
		}
	}

	async function handleExecute() {
		try {
			const order = {
				clientId: selectedAccount.clientId,
				accountId: selectedAccount.id,
				stockId: selectedStock.id,
				executionType: executionType,
				quantity: quantity,
				price: selectedStock.currentPrice,
				previewId: previewResults.previewId,
			};

			const res = await fetch("/api/stocks/buy/execute", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ order }),
			});

			if (res.ok) {
				const result = await res.json();
				if (result.statusCode == 200) {
					toast("Hisse alım işleminiz başarıyla gerçekleşti.");
				} else {
					toast("Hisse alım sırasında bir hata oluştu.");
				}
			}
		} catch (error) {
			console.error("Hisse alım sırasında hata oluştu: ", error);
		}
	}

	return (
		<Dialog>
			<DialogTrigger asChild>
				<div className="flex justify-end">
					<Button
						className="bg-blue-600"
						disabled={quantity === 0 || totalCost > selectedAccount.availableBalance}
						onClick={handlePreview}
					>
						<EyeIcon />
						Devam
					</Button>
				</div>
			</DialogTrigger>
			<DialogContent>
				<DialogTitle>Hisse Alım Ön İzleme</DialogTitle>
				{!previewFailed ? (
					<>
						<div className="grid grid-cols-2 gap-4 text-sm">
							<p className="text-gray-500">Hisse Hesap No:</p>
							<p className="font-semibold">{previewResults.accountNumber}</p>

							<p className="text-gray-500">Hisse Adı + Kodu:</p>
							<p className="font-semibold">{previewResults.stockName + " " + previewResults.stockSymbol}</p>

							<p className="text-gray-500">İşlem Türü:</p>
							<p className="font-semibold">{previewResults.operation}</p>

							<p className="text-gray-500">Fiyat:</p>
							<p className="font-semibold">{previewResults.price} TL</p>

							<p className="text-gray-500">Adet:</p>
							<p className="font-semibold">{previewResults.quantity}</p>

							<p className="text-gray-500">Tutar:</p>
							<p className="font-semibold">{previewResults.totalAmount} TL</p>

							<p className="text-gray-500">Komisyon + BSMV:</p>
							<p className="font-semibold">{previewResults.totalTaxAndCommission} TL</p>

							<p className="text-gray-500">Toplam Maliyet:</p>
							<p className="text-red-500 font-semibold">{previewResults.netAmount} TL</p>

							<p className="text-gray-500">İşlem Tarihi:</p>
							<p className="font-semibold">{previewResults.tradeDate}</p>

							<p className="text-gray-500">Süre/Valör:</p>
							<p className="font-semibold">{previewResults.valueDate}</p>

							<p className="text-gray-500">Hisse Grubu:</p>
							<p className="font-semibold">{previewResults.stockGroup}</p>
						</div>
						<DialogFooter>
							<Button className="bg-green-600" onClick={handleExecute}>
								Emri Onayla
							</Button>
						</DialogFooter>
					</>
				) : (
					<p className="py-4">Ön izleme sırasında bir hata oluştu...</p>
				)}
			</DialogContent>
		</Dialog>
	);
}
