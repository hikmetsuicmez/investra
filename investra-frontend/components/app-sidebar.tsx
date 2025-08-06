import {
	Sidebar,
	SidebarContent,
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
	List,
	ListChecks,
	Network,
	Users,
	Wallet,
} from "lucide-react";

// Example item config
const items = [
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
				label: "Hisse Senedi Alış",
				icon: <ArrowDownCircle />,
				href: "",
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
		</Sidebar>
	);
}
