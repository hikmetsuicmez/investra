import React, { Dispatch, SetStateAction, useEffect, useState } from "react";

import { Popover, PopoverTrigger, PopoverContent } from "@radix-ui/react-popover"; // or your popover library

import { Command, CommandInput, CommandList, CommandEmpty, CommandGroup, CommandItem } from "cmdk"; // example for command palette UI
import { Button } from "@/components/ui/button";
import { ChevronDownIcon } from "lucide-react";

export type Stock = {
	id: number;
	name: string;
	symbol: string | null;
	currentPrice: number;
	stockGroup: string;
	isActive: boolean;
	source: string | null;
};

type StockSelectorProps = {
	selectedStock: Stock;
	setSelectedStock: Dispatch<SetStateAction<Stock>>;
};

export const StockSelector: React.FC<StockSelectorProps> = ({ selectedStock, setSelectedStock }) => {
	const [stocks, setStocks] = useState<Stock[]>([]);
	const [loading, setLoading] = useState(false);
	const [query, setQuery] = useState("");
	const [selected, setSelected] = useState<Stock | null>(null);
	const [open, setOpen] = useState(false);

	async function fetchStocks() {
		const res = await fetch("/api/stocks/buy/available");
		if (!res.ok) throw new Error("Failed to fetch stocks");
		const data = await res.json();
		setStocks(data.data || []);
		setSelectedStock(data.data[0]);
	}

	useEffect(() => {
		setLoading(true);
		fetchStocks();
		setLoading(false);
	}, []);

	useEffect(() => {
		if (selectedStock && stocks.length) {
			const found = stocks.find((stock) => stock.id === selectedStock.id);
			if (found) setSelected(found);
		}
	}, [selectedStock, stocks]);

	useEffect(() => {
		if (!open) {
			setQuery("");
		}
	}, [open]);

	// Filter stocks by query
	const filteredStocks = query
		? stocks.filter((stock) => stock.name.toLowerCase().includes(query.toLowerCase()))
		: stocks;

	// Handle selection
	const onSelectStock = (stock: Stock) => {
		setSelected(stock);
		setSelectedStock(stock);
		setOpen(false);
	};

	return (
		<Popover open={open} onOpenChange={setOpen}>
			<PopoverTrigger aria-label="Select stock" asChild>
				<Button
					className="mt-1 inline-flex items-center justify-between w-64 px-4 py-2 border rounded-md cursor-pointer bg-white"
					variant={"ghost"}
				>
					{selected ? selected.name : "Hisse seçin"}
					<ChevronDownIcon />
				</Button>
			</PopoverTrigger>

			<PopoverContent className="w-64 p-0 mt-1 bg-white border rounded-md shadow-lg z-30">
				<Command>
					<CommandInput
						placeholder="Hisse arama"
						value={query}
						onValueChange={setQuery}
						autoFocus
						className="p-2 border-b w-full"
					/>
					<CommandList className="h-60 overflow-auto">
						{loading && <CommandEmpty>Loading...</CommandEmpty>}
						{!loading && filteredStocks.length === 0 && <CommandEmpty>No stocks found.</CommandEmpty>}

						<CommandGroup>
							{filteredStocks.map((stock) => (
								<CommandItem
									key={stock.id}
									onSelect={() => onSelectStock(stock)}
									className={`cursor-pointer px-3 py-1 rounded ${
										selected?.id === stock.id ? "bg-blue-500 text-white" : ""
									}`}
								>
									{stock.name}
								</CommandItem>
							))}
						</CommandGroup>
					</CommandList>
				</Command>
			</PopoverContent>
		</Popover>
	);
};
