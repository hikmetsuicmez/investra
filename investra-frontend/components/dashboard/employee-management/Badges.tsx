import React from "react";
import { Badge } from "@/components/ui/badge";
import { ShieldCheck, TrendingUp, Eye, CheckCircle, MinusCircle } from "lucide-react";
import { Role } from "@/types/employees";

export function RoleBadge({ role }: { role: Role }) {
	const roleMap: Record<Role, { label: string; color: string; icon: React.ElementType }> = {
		ADMIN: {
			label: "Admin",
			color: "bg-red-100 text-red-800",
			icon: ShieldCheck,
		},
		TRADER: {
			label: "Trader",
			color: "bg-blue-100 text-blue-800",
			icon: TrendingUp,
		},
		VIEWER: {
			label: "Viewer",
			color: "bg-gray-100 text-gray-800",
			icon: Eye,
		},
	};

	const status = roleMap[role];
	const Icon = status.icon;

	return (
		<Badge className={`flex items-center gap-1 select-none ${status.color} font-semibold`}>
			<Icon className="w-4 h-4" />
			{status.label}
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
