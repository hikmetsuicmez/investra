import EndOfDayValuationPage from "@/components/dashboard/portfolio-management/EndOfDayValuation";
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

export default async function Page() {
	const role = await getRoleFromCookies();

	return (
		<div>
			<div>
				<EndOfDayValuationPage initialRole={role} />
			</div>
		</div>
	);
}
