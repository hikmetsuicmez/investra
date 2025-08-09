"use client";
import SearchClientBySearchTerm from "@/components/dashboard/customer-management/SearchClient";

export default function Page() {
	return (
		<div className="min-h-screen flex flex-col items-center justify-center p-6">
			<div className="p-6 max-w-5xl w-full">
				<SearchClientBySearchTerm />
			</div>
		</div>
	);
}
