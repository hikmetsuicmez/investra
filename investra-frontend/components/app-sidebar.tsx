"use client";

import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarGroup,
	SidebarGroupContent,
	SidebarHeader,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
	SidebarMenuSub,
} from "@/components/ui/sidebar";

import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import Link from "next/link";

import {
	ArrowDownCircle,
	ArrowUpCircle,
	BriefcaseIcon,
	ChartLine,
	ChevronDown,
	ChevronsUpDown,
	HomeIcon,
	Key,
	List,
	ListChecks,
	LogOut,
	Network,
	Settings,
	Users,
	Wallet,
} from "lucide-react";

import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { Separator } from "./ui/separator";
import Image from "next/image";
import { useState } from "react";
import { Role } from "@/types/employees";
import SimulationDateDisplay from "./dashboard/simulation-day/SimulationDayDisplay";

type SidebarItemType = {
	label: string;
	icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
	href?: string;
	subitems: SidebarItemType[];
	rolesAllowed?: Role[];
};

const items: SidebarItemType[] = [
	{
		label: "Ana Sayfa",
		icon: HomeIcon,
		href: "/dashboard",
		subitems: [],
		rolesAllowed: ["ADMIN", "TRADER", "VIEWER"],
	},
	{
		label: "Portföyüm",
		icon: BriefcaseIcon,
		href: "/dashboard/portfolio-management",
		subitems: [
			{
				label: "Gün Sonu Değerleme",
				icon: ChartLine,
				href: "/dashboard/portfolio-management",
				subitems: [
					{
						label: "Müşteri Değerleme",
						icon: List,
						href: "/dashboard/portfolio-management",
						subitems: [],
						rolesAllowed: ["ADMIN", "TRADER", "VIEWER"],
					},
					{
						label: "Hisse Senedi Kapanışı",
						icon: List,
						href: "/dashboard/stock-management/list-closing-price",
						subitems: [],
						rolesAllowed: ["ADMIN", "TRADER", "VIEWER"],
					},
				],
				rolesAllowed: ["ADMIN", "TRADER", "VIEWER"],
			},

			{
				label: "Müşteri Listesi",
				icon: List,
				href: "/dashboard/customer-management",
				subitems: [],
				rolesAllowed: ["ADMIN", "TRADER"],
			},
		],
		rolesAllowed: ["ADMIN", "TRADER", "VIEWER"],
	},
	{
		label: "Hesap İşlemleri",
		icon: Users,
		href: "/dashboard/customer-search",
		subitems: [],
		rolesAllowed: ["ADMIN", "TRADER"],
	},
	{
		label: "Bakiye İşlemi",
		icon: Wallet,
		href: "/",
		subitems: [
			{
				label: "Bakiye İşlemleri",
				icon: List,
				href: "/dashboard/customer-search",
				subitems: [],
				rolesAllowed: ["ADMIN", "TRADER"],
			},
		],
		rolesAllowed: ["ADMIN", "TRADER"],
	},
	{
		label: "Personel Yönetimi",
		icon: Network,
		href: "/dashboard/employee-management",
		subitems: [],
		rolesAllowed: ["ADMIN"],
	},
	{
		label: "Hisse Senedi İşlemleri",
		icon: ChartLine,
		subitems: [
			{
				label: "Hisse Senedi Alış",
				icon: ArrowDownCircle,
				href: "/dashboard/stock-management/buy",
				subitems: [],
				rolesAllowed: ["ADMIN", "TRADER"],
			},
			{
				label: "Hisse Senedi Satış",
				icon: ArrowUpCircle,
				href: "/dashboard/stock-management/sell",
				subitems: [],
				rolesAllowed: ["ADMIN", "TRADER"],
			},
			{
				label: "Emir Takibi",
				icon: ListChecks,
				href: "/dashboard/stock-management/order-tracking",
				subitems: [],
				rolesAllowed: ["ADMIN", "TRADER"],
			},
		],
		rolesAllowed: ["ADMIN", "TRADER"],
	},
];

export function SidebarItem({ item, role }: { item: SidebarItemType; role: Role }) {
	const hasSubitems = item.subitems && item.subitems.length > 0;
	const disabled = item.rolesAllowed && !item.rolesAllowed.includes(role);

	if (!hasSubitems) {
		return (
			<SidebarMenuItem>
				<SidebarMenuButton size="lg" asChild>
					{disabled ? (
						<div className="flex gap-2 items-center opacity-50 cursor-not-allowed">
							<item.icon className="size-4" />
							<p>{item.label}</p>
						</div>
					) : (
						<Link href={item.href || "#"} className="flex gap-2 items-center">
							<item.icon className="size-4" />
							<p>{item.label}</p>
						</Link>
					)}
				</SidebarMenuButton>
			</SidebarMenuItem>
		);
	}

	return (
		<Collapsible>
			<SidebarMenuItem>
				<CollapsibleTrigger asChild disabled={disabled}>
					<SidebarMenuButton size="lg" asChild>
						<div
							className={`flex justify-between w-full items-center gap-2 ${
								disabled ? "opacity-50 cursor-not-allowed" : ""
							}`}
						>
							<div className="flex items-center gap-2 text-sm">
								<item.icon className="size-4" />
								<p>{item.label}</p>
							</div>
							<ChevronDown />
						</div>
					</SidebarMenuButton>
				</CollapsibleTrigger>
				{!disabled && (
					<CollapsibleContent>
						<SidebarMenuSub className="mr-0 pr-0">
							{item.subitems.map((subitem) => (
								<SidebarItem key={subitem.label} item={subitem} role={role} />
							))}
						</SidebarMenuSub>
					</CollapsibleContent>
				)}
			</SidebarMenuItem>
		</Collapsible>
	);
}

export function AppSidebar({ initialRole }: { initialRole: Role }) {
	const [role, setRole] = useState<Role>(initialRole);

	const handleLogout = async () => {
		try {
			const response = await fetch("/api/auth/logout", {
				method: "POST",
			});
			if (response.ok) {
				window.location.href = "/auth/login";
			} else {
				console.error("Logout failed:", response.status);
			}
		} catch (error) {
			console.error("Logout error:", error);
		}
	};

	return (
		<Sidebar variant="sidebar" className="h-screen font-medium">
			<SidebarHeader className="h-20 bg-[#f6f5fa]">
				<div className="h-full flex justify-center gap-2 items-center px-4">
					<Image src={"/images/Investra-Logo.png"} alt="Investra logo" height={100} width={100} />
				</div>
			</SidebarHeader>
			<Separator />
			<SidebarContent>
				<SidebarGroup>
					<SidebarContent>
						<SimulationDateDisplay />
					</SidebarContent>
				</SidebarGroup>
				<Separator />
				<SidebarGroup>
					<SidebarGroupContent>
						<SidebarMenu>
							{items.map((item) => (
								<SidebarItem key={item.label} item={item} role={role} />
							))}
						</SidebarMenu>
					</SidebarGroupContent>
				</SidebarGroup>
			</SidebarContent>
			<SidebarFooter className="border-t">
				<SidebarMenu>
					<SidebarMenuItem>
						<DropdownMenu>
							<DropdownMenuTrigger className="w-full">
								<SidebarMenuButton size="lg" asChild>
									<div className="flex gap-2 select-none">
										<Settings />
										<p className="flex-grow">Seçenekler</p>
										<ChevronsUpDown />
									</div>
								</SidebarMenuButton>
							</DropdownMenuTrigger>
							<DropdownMenuContent>
								<DropdownMenuItem>
									<Link href={"/auth/change-password"} className="flex gap-2 select-none w-[200px] items-center">
										<Key color="black" />
										<p className="flex-grow">Şifreni değiştir</p>
									</Link>
								</DropdownMenuItem>
								<DropdownMenuItem onClick={handleLogout}>
									<div className="flex gap-2 select-none items-center cursor-pointer w-[200px]">
										<LogOut color="black" />
										<p className="flex-grow">Çıkış Yap</p>
									</div>
								</DropdownMenuItem>
							</DropdownMenuContent>
						</DropdownMenu>
					</SidebarMenuItem>
				</SidebarMenu>
			</SidebarFooter>
		</Sidebar>
	);
}
