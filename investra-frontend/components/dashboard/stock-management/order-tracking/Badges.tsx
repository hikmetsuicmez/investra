import { Badge } from "@/components/ui/badge";
import { SettlementStatus } from "@/types/stocks";
import { Ban, CheckCircle, Clock, MinusCircleIcon, XCircle } from "lucide-react";

export function TradeOrderTypeBadge({ orderType }: { orderType: string }) {
	return (
		<Badge
			className={`select-none ${orderType == "SELL" ? "bg-orange-100 text-orange-800" : "bg-blue-100 text-blue-800"}`}
		>
			{orderType == "SELL" ? "Satış" : "Alış"}
		</Badge>
	);
}

export function TradeExecutionTypeBadge({ executionType }: { executionType: string }) {
	return (
		<Badge
			className={`select-none ${
				executionType === "MARKET" ? "bg-green-100 text-green-800" : "bg-purple-100 text-purple-800"
			}`}
		>
			{executionType === "MARKET" ? "Piyasa" : "Limit"}
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

export function TradeOrderSettlementStatusBadge({ settlementStatus }: { settlementStatus: SettlementStatus }) {
	const statusMap: Record<
		Exclude<SettlementStatus, null>,
		{ label: string; color: string; icon: React.ElementType }
	> = {
		PENDING: {
			label: "T+0",
			color: "bg-yellow-100 text-yellow-800",
			icon: Clock,
		},
		T1: {
			label: "T+1",
			color: "bg-blue-100 text-blue-800",
			icon: Clock,
		},
		T2: {
			label: "T+2",
			color: "bg-indigo-100 text-indigo-800",
			icon: Clock,
		},
		COMPLETED: {
			label: "Tamamlandı",
			color: "bg-green-100 text-green-800",
			icon: CheckCircle,
		},
		CANCELLED: {
			label: "İptal Edildi",
			color: "bg-rose-100 text-rose-800",
			icon: XCircle,
		},
	};

	const defaultStatus = {
		label: String(settlementStatus),
		color: "bg-gray-100 text-gray-800",
		icon: Clock,
	};

	const nullStatus = {
		label: "Henüz Alınmadı",
		color: "bg-gray-200 text-gray-800",
		icon: MinusCircleIcon,
	};

	let status;
	if (settlementStatus === null) {
		status = nullStatus;
	} else {
		status = statusMap[settlementStatus] || defaultStatus;
	}

	const Icon = status.icon;

	return (
		<Badge className={`flex items-center gap-1 select-none ${status.color}`}>
			<Icon className="w-4 h-4" />
			{status.label}
		</Badge>
	);
}
