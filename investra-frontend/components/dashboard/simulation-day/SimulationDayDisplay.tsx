"use client";

import { ClockIcon } from "lucide-react";
import { useEffect, useState } from "react";

export default function SimulationDateDisplay() {
	const [date, setDate] = useState<string | null>(null);
	const [error, setError] = useState<string | null>(null);

	useEffect(() => {
		async function fetchSimulationDate() {
			try {
				const res = await fetch("/api/system-time", {
					cache: "no-store",
				});
				const data = await res.json();

				if (!res.ok) {
					setError(data.message || "Bir hata oluştu");
					return;
				}

				setDate(data.date);
			} catch (err: unknown) {
				if (err instanceof Error) {
					setError(err.message);
				} else {
					setError("Bir hata oluştu.");
				}
			}
		}

		fetchSimulationDate();
	}, []);

	return (
		<div>
			{error && <span>Hata: {error}</span>}
			{!error &&
				(date ? (
					<div className="flex items-center gap-4 pb-1 pt-2 pl-10">
						<div className="rounded-full p-2 bg-gray-200">
							<ClockIcon size={24} />
						</div>
						<div className="flex flex-col items-center">
							<p className="font-normal text-sm text-gray-700">Sistem tarihi:</p>
							<p>{date}</p>
						</div>
					</div>
				) : (
					<span>Yükleniyor...</span>
				))}
		</div>
	);
}
