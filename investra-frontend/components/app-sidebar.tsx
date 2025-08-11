"use client";

import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarGroup,
	SidebarGroupContent,
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

const items = [
	{
		label: "Ana Sayfa",
		icon: <HomeIcon />,
		href: "/dashboard",
		subitems: [],
	},
	{
		label: "Portföyüm",
		icon: <Wallet />,
		href: "/dashboard/portfolio-management",
		subitems: [
			{
				label: "Gün Sonu Değerleme",
				icon: <ChartLine />,
				href: "/dashboard/portfolio-management",
				subitems: [
					{
						label: "Müşteri Değerleme",
						icon: <List />,
						href: "/dashboard/portfolio-management",
						subitems: [],
					},
					{
						label: "Hisse Senedi Kapanışı",
						icon: <List />,
						href: "/dashboard/stock-management/list-closing-price",
						subitems: [],
					},
				],
			},
		],
	},
	{
		label: "Müşteri Yönetimi",
		icon: <Users />,
		href: "/dashboard/customer-management",
		subitems: [
			{
				label: "Müşteri İşlemleri",
				icon: <List />,
				href: "/dashboard/customer-management",
				subitems: [],
			},
			{
				label: "Hesap İşlemleri",
				icon: <List />,
				href: "/dashboard/customer-search",
				subitems: [],
			},
		],
	},
	{
		label: "Personel Yönetimi",
		icon: <Network />,
		href: "/dashboard/employee-management",
		subitems: [],
	},
	{
		label: "Hisse Senedi İşlemleri",
		icon: <ChartLine />,
		subitems: [
			{
				label: "Hisse Senedi Listeleme",
				icon: <List />,
				href: "/dashboard/stock-management/list",
				subitems: [],
			},
			{
				label: "Hisse Senedi Alış",
				icon: <ArrowDownCircle />,
				href: "/dashboard/stock-management/buy",
				subitems: [],
			},
			{
				label: "Hisse Senedi Satış",
				icon: <ArrowUpCircle />,
				href: "/dashboard/stock-management/sell",
				subitems: [],
			},
			{
				label: "Emir Takibi",
				icon: <ListChecks />,
				href: "/dashboard/stock-management/order-tracking",
				subitems: [],
			},
		],
	},
];

export function SidebarItem({ item }) {
	const hasSubitems = item.subitems && item.subitems.length > 0;

	if (!hasSubitems) {
		return (
			<SidebarMenuItem>
				<SidebarMenuButton size="lg" asChild>
					<Link href={item.href || "#"} className="flex gap-2 items-center">
						{item.icon}
						<p>{item.label}</p>
					</Link>
				</SidebarMenuButton>
			</SidebarMenuItem>
		);
	}

	return (
		<Collapsible>
			<SidebarMenuItem>
				<CollapsibleTrigger asChild>
					<SidebarMenuButton size="lg" asChild>
						<button className="flex justify-between w-full items-center gap-2">
							<div className="flex items-center gap-2">
								{item.icon}
								<p>{item.label}</p>
							</div>
							<ChevronDown className="transition-transform data-[state=open]:rotate-180" />
						</button>
					</SidebarMenuButton>
				</CollapsibleTrigger>
				<CollapsibleContent>
					<SidebarMenuSub>
						{item.subitems.map((subitem) => (
							<SidebarItem key={subitem.label} item={subitem} />
						))}
					</SidebarMenuSub>
				</CollapsibleContent>
			</SidebarMenuItem>
		</Collapsible>
	);
}

export function AppSidebar() {
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
			<SidebarContent>
				<SidebarMenu>
					<SidebarGroup>
						<SidebarGroupContent>
							{items.map((item) => (
								<SidebarItem key={item.label} item={item} />
							))}
						</SidebarGroupContent>
					</SidebarGroup>
				</SidebarMenu>
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
									<Link
										href={"/auth/change-password"}
										className="flex gap-2 select-none w-[200px] items-center"
									>
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
