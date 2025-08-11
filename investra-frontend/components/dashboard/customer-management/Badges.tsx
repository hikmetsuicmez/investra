import React from "react";
import { Badge } from "@/components/ui/badge";
import { CheckCircle, MinusCircle, User, Building } from "lucide-react";
import { ClientType } from "@/types/customers";

export function ClientTypeBadge({ type }: { type: ClientType }) {
	const typeMap: Record<ClientType, { label: string; color: string; icon: React.ElementType }> = {
		INDIVIDUAL: {
			label: "Bireysel",
			color: "bg-indigo-100 text-indigo-800",
			icon: User,
		},
		CORPORATE: {
			label: "Kurumsal",
			color: "bg-purple-100 text-purple-800",
			icon: Building,
		},
	};

	const currentType = typeMap[type];
	const Icon = currentType.icon;

	return (
		<Badge className={`flex items-center gap-1 select-none ${currentType.color} font-semibold`}>
			<Icon className="w-4 h-4" />
			{currentType.label}
		</Badge>
	);
}

type Status = "AKTIF" | "PASIF";

export function StatusBadge({ status }: { status: Status }) {
	const statusMap: Record<Status, { label: string; color: string; icon: React.ElementType }> = {
		AKTIF: {
			label: "Aktif",
			color: "bg-green-100 text-green-800",
			icon: CheckCircle,
		},
		PASIF: {
			label: "Pasif",
			color: "bg-gray-100 text-gray-800",
			icon: MinusCircle,
		},
	};

	const currentStatus = statusMap[status];
	const Icon = currentStatus.icon;

	return (
		<Badge className={`flex items-center gap-1 select-none ${currentStatus.color} font-semibold`}>
			<Icon className="w-4 h-4" />
			{currentStatus.label}
		</Badge>
	);
}
