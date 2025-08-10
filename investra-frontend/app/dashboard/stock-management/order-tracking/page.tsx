"use client";

import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useEffect, useMemo, useState } from "react";
import { TradeOrder } from "@/types/stocks";
import { SearchIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectGroup,
	SelectItem,
	SelectLabel,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import {
	TradeOrderStatusBadge,
	TradeOrderTypeBadge,
} from "@/components/dashboard/stock-management/order-tracking/Badges";
import TradeOrderDetails from "@/components/dashboard/stock-management/order-tracking/TradeOrderDetails";
import { toast } from "sonner";
import CancelOrderDialog from "@/components/dashboard/stock-management/order-tracking/CancelOrderDialog";

export default function OrderTracking() {
	const [allTradeOrders, setTradeAllOrders] = useState<TradeOrder[]>([]);
	const [searchTerm, setSearchTerm] = useState("");
	const [filterOrderType, setFilterOrderType] = useState("");
	const [filterOrderStatus, setFilterOrderStatus] = useState("");

	async function getStocks() {
		try {
			const response = await fetch("/api/trade-orders/all");
			if (!response.ok) {
				throw new Error("Failed to fetch trade orders");
			}
			const data = await response.json();
			setTradeAllOrders(data.data);
		} catch (error) {
			console.error("Error fetching trade orders:", error);
		}
	}

	function onCancelSuccess() {
		getStocks();
	}

	useEffect(() => {
		getStocks();
	}, []);

	const filteredOrders = useMemo(() => {
		return allTradeOrders.filter((order) => {
			const matchesSearch =
				!searchTerm ||
				order.clientFullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
				String(order.clientId).includes(searchTerm) ||
				order.stockCode?.toLowerCase().includes(searchTerm.toLowerCase()) ||
				String(order.id).includes(searchTerm);

			const matchesOrderType = !filterOrderType || filterOrderType === "all" || order.orderType === filterOrderType;
			const matchesOrderStatus =
				!filterOrderStatus || filterOrderStatus === "all" || order.status === filterOrderStatus;

			return matchesSearch && matchesOrderType && matchesOrderStatus;
		});
	}, [allTradeOrders, searchTerm, filterOrderType, filterOrderStatus]);

	return (
		<div className="flex flex-col h-screen bg-gray-100 p-6 gap-6">
			<div className="flex justify-between items-center p-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Emir Takibi</h1>
			</div>

			{/* Search + Filters */}
			<Card>
				<CardContent className="flex flex-col gap-4">
					<div className="flex items-center gap-2 shrink-0">
						<SearchIcon size={20} />
						<h1 className="text-lg font-semibold">Emir Arama</h1>
					</div>

					<div className="flex flex-wrap gap-4">
						<Input
							placeholder="Ara (Müşteri, Hisse, Emir No...)"
							value={searchTerm}
							onChange={(e) => setSearchTerm(e.target.value)}
							className="flex-1 min-w-[200px]"
						/>

						<Select value={filterOrderType} onValueChange={setFilterOrderType}>
							<SelectTrigger className="min-w-[120px]">
								<SelectValue placeholder="Tüm Türler" />
							</SelectTrigger>
							<SelectContent>
								<SelectGroup>
									<SelectLabel>Tür</SelectLabel>
									<SelectItem value="all">Tüm Türler</SelectItem> {/* Changed here */}
									<SelectItem value="BUY">Alış</SelectItem>
									<SelectItem value="SELL">Satış</SelectItem>
								</SelectGroup>
							</SelectContent>
						</Select>

						<Select value={filterOrderStatus} onValueChange={setFilterOrderStatus}>
							<SelectTrigger className="min-w-[140px]">
								<SelectValue placeholder="Tüm Durumlar" />
							</SelectTrigger>
							<SelectContent>
								<SelectGroup>
									<SelectLabel>Durum</SelectLabel>
									<SelectItem value="all">Tüm Durumlar</SelectItem> {/* Changed here */}
									<SelectItem value="PENDING">Beklemede</SelectItem>
									<SelectItem value="EXECUTED">Gerçekleşti</SelectItem>
									<SelectItem value="CANCELLED">İptal Edildi</SelectItem>
									<SelectItem value="REJECTED">Reddedildi</SelectItem>
								</SelectGroup>
							</SelectContent>
						</Select>
					</div>
				</CardContent>
			</Card>

			<Card className="overflow-hidden">
				<CardContent className="overflow-auto space-y-4">
					<h1 className="text-lg font-semibold">Emirler ({allTradeOrders.length})</h1>

					<Table className="text-lg">
						<TableHeader>
							<TableRow className="text-[16px]">
								<TableHead>Emir No</TableHead>
								<TableHead>Müşteri</TableHead>
								<TableHead>Hisse</TableHead>
								<TableHead>Tür</TableHead>
								<TableHead>Adet</TableHead>
								<TableHead>Fiyat</TableHead>
								<TableHead>Tutar</TableHead>
								<TableHead>Durum</TableHead>
								<TableHead>Tarih/Saat</TableHead>
								<TableHead>İşlemler</TableHead>
							</TableRow>
						</TableHeader>
						<TableBody>
							{filteredOrders.map((tradeOrder) => (
								<TableRow key={tradeOrder.id} className="text-[16px]">
									<TableCell className="font-light">{tradeOrder.id}</TableCell>
									<TableCell>
										<div>
											<p className="font-medium">{tradeOrder.clientFullName || "İsimsiz"}</p>
											<p className="font-light">{tradeOrder.clientId}</p>
										</div>
									</TableCell>
									<TableCell className="font-medium">{tradeOrder.stockCode}</TableCell>
									<TableCell>
										<TradeOrderTypeBadge orderType={tradeOrder.orderType} />
									</TableCell>
									<TableCell className="font-medium">{tradeOrder.quantity.toLocaleString("tr-TR")}</TableCell>
									<TableCell className="font-medium">
										{tradeOrder.price.toLocaleString("tr-TR", { style: "currency", currency: "TRY" })}
									</TableCell>
									<TableCell className="font-medium">
										{(tradeOrder.quantity * tradeOrder.price).toLocaleString("tr-TR", {
											style: "currency",
											currency: "TRY",
										})}
									</TableCell>
									<TableCell>
										<TradeOrderStatusBadge orderStatus={tradeOrder.status} />
									</TableCell>
									<TableCell>
										<div>
											<p className="font-light">{new Date(tradeOrder.submittedAt).toLocaleDateString("tr-TR")}</p>
											<p className="font-light text-gray-700">
												{new Date(tradeOrder.submittedAt).toLocaleTimeString("tr-TR")}
											</p>
										</div>
									</TableCell>
									<TableCell>
										<div className="flex gap-2">
											<TradeOrderDetails selectedOrder={tradeOrder} />

											{tradeOrder.status === "PENDING" && (
												<CancelOrderDialog order={tradeOrder} onCancelSuccess={onCancelSuccess} />
											)}
										</div>
									</TableCell>
								</TableRow>
							))}
						</TableBody>
					</Table>
				</CardContent>
			</Card>
		</div>
	);
}
