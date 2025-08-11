import { AppSidebar } from "@/components/app-sidebar";
import { SidebarProvider } from "@/components/ui/sidebar";
import { Toaster } from "@/components/ui/sonner";
import { Role } from "@/types/employees";
import { cookies } from "next/headers";

async function getRoleFromCookies(): Promise<Role> {
	const cookieStore = await cookies();
	const roleCookie = cookieStore.get("role")?.value;

	if (roleCookie === "ADMIN" || roleCookie === "TRADER" || roleCookie === "VIEWER") {
		return roleCookie;
	}
	return "VIEWER"; // fallback default
}

export default async function Layout({ children }: { children: React.ReactNode }) {
	const role = await getRoleFromCookies();

	return (
		<SidebarProvider>
			<AppSidebar initialRole={role} />
			<main className="w-full">{children}</main>
			<Toaster position="bottom-right" />
		</SidebarProvider>
	);
}
