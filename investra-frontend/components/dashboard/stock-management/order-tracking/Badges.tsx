import { Badge } from "@/components/ui/badge";
import { Ban, CheckCircle, Clock, XCircle } from "lucide-react";

export function TradeOrderTypeBadge({ orderType }: { orderType: string }) {
	return (
		<Badge
			className={`select-none ${orderType == "SELL" ? "bg-orange-100 text-orange-800" : "bg-blue-100 text-blue-800"}`}
		>
			{orderType == "SELL" ? "Satış" : "Alış"}
		</Badge>
	);
}

export function TradeOrderStatusBadge({ orderStatus }: { orderStatus: string }) {
	const statusMap: Record<string, { label: string; color: string; icon: React.ElementType }> = {
		PENDING: {
			label: "Beklemede",
			color: "bg-yellow-100 text-yellow-800",
			icon: Clock,
		},
		EXECUTED: {
			label: "Gerçekleşti",
			color: "bg-green-100 text-green-800",
			icon: CheckCircle,
		},
		CANCELLED: {
			label: "İptal Edildi",
			color: "bg-rose-100 text-rose-800",
			icon: XCircle,
		},
		REJECTED: {
			label: "Reddedildi",
			color: "bg-red-100 text-red-800",
			icon: Ban,
		},
	};

	const status = statusMap[orderStatus] || {
		label: orderStatus,
		color: "bg-gray-100 text-gray-800",
		icon: Clock,
	};

	const Icon = status.icon;

	return (
		<Badge className={`flex items-center gap-1 select-none ${status.color}`}>
			<Icon className="w-4 h-4" />
			{status.label}
		</Badge>
	);
}
