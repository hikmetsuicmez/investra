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
	SidebarMenuSubButton,
	SidebarMenuSubItem,
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

// Example item config
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
		href: "/dashboard/portfolio",
		subitems: [],
	},
	{
		label: "Müşteri Yönetimi",
		icon: <Users />,
		href: "/dashboard/customer-management",
		subitems: [],
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
			},
			{
				label: "Hisse Senedi Alış",
				icon: <ArrowDownCircle />,
				href: "/dashboard/stock-management/buy",
			},
			{
				label: "Hisse Senedi Satış",
				icon: <ArrowUpCircle />,
				href: "",
			},
			{
				label: "Emir Takibi",
				icon: <ListChecks />,
				href: "",
			},
		],
	},
];

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
							{items.map((item, index) =>
								item.subitems && item.subitems.length > 0 ? (
									<Collapsible key={index} className="group/collapsible">
										<SidebarMenuItem>
											<CollapsibleTrigger asChild>
												<SidebarMenuButton size={"lg"} asChild>
													<div className="flex gap-2 select-none">
														{item.icon}
														<p className="grow">{item.label}</p>
														<ChevronDown className="transition-transform group-data-[state=open]/collapsible:rotate-180" />
													</div>
												</SidebarMenuButton>
											</CollapsibleTrigger>
											<CollapsibleContent>
												<SidebarMenuSub>
													{item.subitems.map((subitem, subIndex) => (
														<SidebarMenuSubItem key={subIndex}>
															<SidebarMenuSubButton size="md" asChild>
																<Link href={subitem.href} className="flex gap-2 select-none">
																	{subitem.icon}
																	<p className="line-clamp-1">{subitem.label}</p>
																</Link>
															</SidebarMenuSubButton>
														</SidebarMenuSubItem>
													))}
												</SidebarMenuSub>
											</CollapsibleContent>
										</SidebarMenuItem>
									</Collapsible>
								) : (
									<SidebarMenuItem key={index}>
										<SidebarMenuButton size={"lg"} asChild>
											<Link href={item.href || "#"} className="flex gap-2">
												{item.icon}
												<p>{item.label}</p>
											</Link>
										</SidebarMenuButton>
									</SidebarMenuItem>
								)
							)}
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
									<div className="flex gap-2 select-none ">
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
