"use client";
import SearchClientBySearchTerm from "@/components/dashboard/customer-management/SearchClient";

export default function Page() {
	return (
		<div className="min-h-screen flex flex-col items-center justify-center p-6">
			<div className="p-6 max-w-5xl w-full">
				<h1 className="text-2xl font-bold mb-4">Müşteri Arama</h1>
				<h3> İşlem yapmak istediğiniz müşteriyi seçiniz.</h3>
				<SearchClientBySearchTerm />
			</div>
		</div>
	);
}
