import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { EyeIcon } from "lucide-react";
import { TradeOrderStatusBadge } from "./Badges";
import { TradeOrder } from "@/types/stocks";

export default function TradeOrderDetails({ selectedOrder }: { selectedOrder: TradeOrder }) {
	return (
		<Dialog>
			<DialogTrigger asChild>
				<Button size="sm" variant="outline">
					<EyeIcon />
					Detay
				</Button>
			</DialogTrigger>
			<DialogContent>
				<DialogTitle>Emir Detayları</DialogTitle>
				{selectedOrder && (
					<div className="grid grid-cols-2 gap-4 text-sm mt-4">
						<p className="text-gray-500">Emir No:</p>
						<p className="font-semibold">{selectedOrder.id}</p>

						<p className="text-gray-500">Müşteri:</p>
						<p className="font-semibold">{selectedOrder.clientFullName}</p>

						<p className="text-gray-500">Hisse:</p>
						<p className="font-semibold">{selectedOrder.stockCode}</p>

						<p className="text-gray-500">Tür:</p>
						<p className="font-semibold">{selectedOrder.orderType}</p>

						<p className="text-gray-500">Adet:</p>
						<p className="font-semibold">{selectedOrder.quantity}</p>

						<p className="text-gray-500">Fiyat:</p>
						<p className="font-semibold">
							{selectedOrder.price.toLocaleString("tr-TR", { style: "currency", currency: "TRY" })}
						</p>

						<p className="text-gray-500">Tutar:</p>
						<p className="font-semibold">
							{(selectedOrder.quantity * selectedOrder.price).toLocaleString("tr-TR", {
								style: "currency",
								currency: "TRY",
							})}
						</p>

						<p className="text-gray-500">Durum:</p>
						<TradeOrderStatusBadge orderStatus={selectedOrder.status} />

						<p className="text-gray-500">Tarih:</p>
						<p className="font-semibold">{new Date(selectedOrder.submittedAt).toLocaleString("tr-TR")}</p>
					</div>
				)}
			</DialogContent>
		</Dialog>
	);
}
