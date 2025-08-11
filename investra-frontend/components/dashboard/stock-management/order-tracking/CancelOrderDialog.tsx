"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogTitle, DialogFooter, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { XCircle } from "lucide-react";
import { TradeOrder } from "@/types/stocks";
import { toast } from "sonner";

type CancelOrderDialogProps = {
	order: TradeOrder;
	onCancelSuccess?: () => void;
};

export default function CancelOrderDialog({ order, onCancelSuccess }: CancelOrderDialogProps) {
	const [loading, setLoading] = useState(false);

	async function handleCancel() {
		if (!order?.id) return;
		try {
			setLoading(true);
			const response = await fetch(`/api/trade-orders/${order.id}/cancel`, {
				method: "POST",
			});
			const result = await response.json();

			if (!response.ok) {
				toast.error(result.message || "Emri iptal ederken bir hata oluştu.");
			} else {
				toast.success("Emir başarıyla iptal edildi.");
				if (onCancelSuccess) onCancelSuccess();
			}
		} catch (error) {
			console.error(error);
			toast.error("Sunucu hatası: İptal işlemi gerçekleştirilemedi.");
		} finally {
			setLoading(false);
		}
	}

	return (
		<Dialog>
			<DialogTrigger asChild>
				<Button size="sm" variant="destructive" disabled={loading}>
					<XCircle className="mr-1" size={16} />
					İptal Et
				</Button>
			</DialogTrigger>

			<DialogContent>
				<DialogTitle>Emir İptali</DialogTitle>
				<div className="space-y-4 mt-4 text-sm">
					<p className="text-gray-600">Aşağıdaki emri iptal etmek istediğinize emin misiniz?</p>
					<div className="grid grid-cols-2 gap-2 border p-3 rounded-md bg-gray-50 text-sm">
						<span className="text-gray-500">Emir No:</span>
						<span className="font-semibold">{order.id}</span>

						<span className="text-gray-500">Müşteri:</span>
						<span className="font-semibold">{order.clientFullName}</span>

						<span className="text-gray-500">Hisse:</span>
						<span className="font-semibold">{order.stockCode}</span>

						<span className="text-gray-500">Adet:</span>
						<span className="font-semibold">{order.quantity.toLocaleString("tr-TR")}</span>

						<span className="text-gray-500">Fiyat:</span>
						<span className="font-semibold">
							{order.price.toLocaleString("tr-TR", { style: "currency", currency: "TRY" })}
						</span>
					</div>
				</div>

				<DialogFooter>
					<Button variant="secondary">Vazgeç</Button>
					<Button variant="destructive" onClick={handleCancel} disabled={loading}>
						{loading ? "İptal ediliyor..." : "Evet, İptal Et"}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
