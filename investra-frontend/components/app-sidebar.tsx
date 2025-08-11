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
import { useRouter } from "next/navigation";
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
import { Separator } from "./ui/separator";

type SidebarItemType = {
	label: string;
	icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
	href?: string;
	onClick?: () => void;
	subitems: SidebarItemType[];
};

export function SidebarItem({ item }: { item: SidebarItemType }) {
	const hasSubitems = item.subitems && item.subitems.length > 0;

	if (!hasSubitems) {
		return (
			<SidebarMenuItem>
				<SidebarMenuButton
					size="lg"
					asChild={!item.onClick}
					onClick={item.onClick}
				>
					{item.onClick ? (
						<div className="flex gap-2 items-center cursor-pointer">
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
				<CollapsibleTrigger asChild>
					<SidebarMenuButton size="lg" asChild>
						<div className="flex justify-between w-full items-center gap-2">
							<div className="flex items-center gap-2 text-sm">
								<item.icon className="size-4" />
								<p>{item.label}</p>
							</div>
							<ChevronDown />
						</div>
					</SidebarMenuButton>
				</CollapsibleTrigger>
				<CollapsibleContent>
					<SidebarMenuSub className="mr-0 pr-0">
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
	const router = useRouter();

	const items: SidebarItemType[] = [
		{
			label: "Ana Sayfa",
			icon: HomeIcon,
			href: "/dashboard",
			subitems: [],
		},
		{
			label: "Portföyüm",
			icon: Wallet,
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
						},
						{
							label: "Hisse Senedi Kapanışı",
							icon: List,
							href: "/dashboard/stock-management/list-closing-price",
							subitems: [],
						},
					],
				},
			],
		},
		{
			label: "Müşteri Yönetimi",
			icon: Users,
			href: "/dashboard/customer-management",
			subitems: [],
		},
		{
			label: "Personel Yönetimi",
			icon: Network,
			href: "/dashboard/employee-management",
			subitems: [],
		},
		{
			label: "Hisse Senedi İşlemleri",
			icon: ChartLine,
			subitems: [
				{
					label: "Hisse Senedi Listeleme",
					icon: List,
					href: "/dashboard/stock-management/list",
					subitems: [],
				},
				{
					label: "Hisse Senedi Alış",
					icon: ArrowDownCircle,
					href: "/dashboard/stock-management/buy",
					subitems: [],
				},
				{
					label: "Hisse Senedi Satış",
					icon: ArrowUpCircle,
					href: "/dashboard/stock-management/sell",
					subitems: [],
				},
				{
					label: "Emir Takibi",
					icon: ListChecks,
					href: "/dashboard/stock-management/order-tracking",
					subitems: [],
				},
			],
		},
		{
			label: "Hesap İşlemleri",
			icon: Users,
			onClick: () => router.push("/dashboard/account-management/search-client?source=account"),
			subitems: [],
		},
		{
			label: "Bakiye İşlemleri",
			icon: Wallet,
			onClick: () => router.push("/dashboard/account-management/search-client?source=balance"),
			subitems: [],
		},
	];

	const handleLogout = async () => {
		try {
			const response = await fetch("/api/auth/logout", { method: "POST" });
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
			<SidebarHeader className="h-20">
				<div className="h-full flex gap-2 items-center px-4">
					<p>V</p>
					<p className="font-bold">INVESTRA</p>
				</div>
			</SidebarHeader>
			<Separator />
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
