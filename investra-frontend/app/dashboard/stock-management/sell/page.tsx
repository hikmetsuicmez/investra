"use client";

import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { ChartLineIcon, CreditCardIcon, SearchIcon } from "lucide-react";
import { useEffect, useState } from "react";
import { Customer, Account, ExecutionType, AccountType, Stock } from "@/types/stocks";
import SellStockPreviewDialog from "@/components/dashboard/stock-management/SellStockPreviewDialog";
import { SellStockSelector } from "@/components/dashboard/stock-management/SellStockSelector";

export default function StockSell() {
	const [customers, setCustomers] = useState<Customer[]>([]);
	const [searchTerm, setSearchTerm] = useState("");
	const [accountsByClient, setAccountsByClient] = useState<Account[]>([]);
	const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
	const [selectedStock, setSelectedStock] = useState<Stock>({
		id: 0,
		name: "",
		symbol: "",
		currentPrice: 0,
		stockGroup: "",
		isActive: true,
		source: "",
		category: "",
	});
	const [executionType, setExecutionType] = useState<ExecutionType>("MARKET");
	const [price, setPrice] = useState(0);
	const [quantity, setQuantity] = useState(0);
	const [cost, setCost] = useState(0);
	const [commission, setCommission] = useState(0);
	const [bsmv, setBsmv] = useState(0);
	const [totalCost, setTotalCost] = useState(0);

	function getAccountLabel(key: AccountType): string {
		const accountTypes: Record<AccountType, string> = {
			SETTLEMENT: "Takas Hesabı",
			INVESTMENT: "Yatırım Hesabı",
			CURRENT: "Cari Hesap",
			SAVINGS: "Tasarruf Hesabı",
			DEPOSIT: "Mevduat Hesabı",
		};
		return accountTypes[key] || "Unknown Account Type";
	}

	function formatToTurkishLira(amount: number) {
		return amount.toLocaleString("tr-TR", { style: "currency", currency: "TRY" });
	}

	const handleQuantityChange = (q: number) => {
		setQuantity(q);
		const newCost = price * q;
		const newCommission = newCost * 0.002;
		const newBsmv = newCommission * 0.05;
		setCost(newCost);
		setCommission(newCommission);
		setBsmv(newBsmv);
		setTotalCost(newCost - newCommission - newBsmv);
	};

	const handleTotalCostChange = (tc: number) => {
		if (price <= 0) return;

		const newQuantity = Math.round(tc / price);
		const safeQuantity = Math.max(newQuantity, 0);

		const newCost = safeQuantity * price;
		const newCommission = newCost * 0.002;
		const newBsmv = newCommission * 0.05;
		const newTotalCost = newCost - newCommission - newBsmv;

		setQuantity(safeQuantity);
		setCost(newCost);
		setCommission(newCommission);
		setBsmv(newBsmv);
		setTotalCost(newTotalCost);
	};

	async function fetchCustomers() {
		try {
			const res = await fetch("/api/clients/active-clients");
			if (!res.ok) throw new Error("Failed to fetch active customers");
			const data = await res.json();
			setCustomers(data.data || []);
		} catch (error) {
			console.error("Error fetching customers:", error);
		}
	}

	async function fetchAccounts(clientId: string) {
		try {
			const res = await fetch(`/api/accounts/client/${clientId}`);
			if (!res.ok) {
				console.error(`Failed to fetch accounts for client ${clientId}:`, res.status, res.statusText);
				return;
			}
			const json = await res.json();

			if (!json.success) {
				console.error(`API error for client ${clientId}:`, json.message);
				return;
			}

			const accounts = json.data;

			if (!Array.isArray(accounts)) {
				console.error(`Invalid accounts data for client ${clientId}: expected array, got`, accounts);
				return;
			}

			setAccountsByClient((prev) => [...prev, ...accounts]);
		} catch (error) {
			console.error("Network or unexpected error fetching accounts for client:", clientId, error);
		}
	}

	useEffect(() => {
		fetchCustomers();
	}, []);

	useEffect(() => {
		// Fetch accounts for customers without accounts loaded yet
		customers.forEach((customer) => {
			fetchAccounts(customer.id.toString());
		});
	}, [customers]);

	// Handle initial stock selection - set price to current stock price
	useEffect(() => {
		if (selectedStock.id > 0) {
			setPrice(selectedStock.currentPrice);
		}
	}, [selectedStock.id]);

	// Handle execution type changes
	useEffect(() => {
		// Only set price to stock's current price when execution type changes to MARKET
		// This preserves user's manual input for LIMIT orders
		if (executionType === "MARKET") {
			setPrice(selectedStock.currentPrice);
		}
	}, [executionType, selectedStock.currentPrice]);

	// Recalculate costs when price changes
	useEffect(() => {
		if (price > 0 && quantity > 0) {
			const newCost = price * quantity;
			const newCommission = newCost * 0.002;
			const newBsmv = newCommission * 0.05;
			setCost(newCost);
			setCommission(newCommission);
			setBsmv(newBsmv);
			setTotalCost(newCost - newCommission - newBsmv);
		}
	}, [price, quantity]);

	const allAccounts: Account[] = Object.values(accountsByClient).flat();

	const lowerSearch = searchTerm.toLowerCase();

	const filteredAccounts = allAccounts.filter((acc) => {
		const clientName = acc.clientName ?? "";

		return (
			clientName.toLowerCase().includes(lowerSearch) ||
			acc.accountNumber.toLowerCase().includes(lowerSearch) ||
			acc.iban.toLowerCase().includes(lowerSearch)
		);
	});

	return (
		<div className="flex flex-col gap-6 min-h-screen overflow-auto bg-gray-100 p-6">
			<div className="flex justify-between items-center p-4 flex-shrink-0">
				<h1 className="text-2xl font-semibold">Hisse Senedi Satış</h1>
			</div>

			<Card className="max-h-[400px] overflow-y-auto">
				<CardContent className="gap-4 h-full flex flex-col">
					<div className="flex items-center gap-2 shrink-0">
						<SearchIcon size={20} />
						<h1 className="text-lg font-semibold">Hesap Arama</h1>
					</div>
					<Input
						placeholder="Müşteri Adı / Hesap No / IBAN"
						value={searchTerm}
						onChange={(e) => setSearchTerm(e.target.value)}
						className="shrink-0"
					/>

					<div className="flex flex-col gap-4">
						{filteredAccounts.length > 0 ? (
							filteredAccounts.map((acc, idx) => {
								const customer = customers.find((c) => c.id === acc.clientId);
								const nationalId = customer?.clientType === "INDIVIDUAL" ? customer.nationalityNumber : null;
								const taxNumber = customer?.clientType === "CORPORATE" ? customer.taxNumber : null;

								const isSelected = selectedAccount?.id === acc.id;

								return (
									<div
										key={idx}
										onClick={() => setSelectedAccount(acc)}
										className={`cursor-pointer px-4 py-10 flex items-center rounded-lg shadow border ${
											isSelected ? "border-blue-300 bg-blue-600/5" : "border-gray-200 bg-white"
										}`}
									>
										<div className="flex-grow">
											<p className="font-medium">
												{acc.clientName} - {getAccountLabel(acc.accountType)}
											</p>
											<div className="flex">
												<p className="text-sm text-gray-700 pr-2 border-r border-gray-700">IBAN: {acc.iban}</p>
												<p className="text-sm text-gray-700 px-2 border-r border-gray-700">
													{nationalId ? `TCKN: ${nationalId}` : taxNumber ? `Vergi No: ${taxNumber}` : ""}
												</p>
												<p className="text-sm text-gray-700 px-2">Hesap No: {acc.accountNumber}</p>
											</div>
										</div>
										<div className="flex flex-col items-end">
											<p className="text-sm text-gray-700">Bakiye</p>
											<p className="text-lg text-green-600 font-medium">{formatToTurkishLira(acc.availableBalance)}</p>
										</div>
									</div>
								);
							})
						) : (
							<p className="text-gray-500">Hesap bulunamadı.</p>
						)}
					</div>
				</CardContent>
			</Card>

			{/* Example: Showing selected account details below */}
			{selectedAccount && (
				<Card className="overflow-hidden">
					<CardContent className="flex-grow space-y-4">
						<div className="flex items-center gap-2">
							<ChartLineIcon size={20} />
							<h1 className="text-lg font-semibold">Satış İşlemi - {selectedAccount.clientName}</h1>
						</div>

						<div>
							<Label>Paranın Çekileceği Hesap</Label>
							<Card className="py-0 shadow-none rounded-lg mt-1 bg-gray-50">
								<CardContent className="p-3">
									<div className="flex items-center gap-2">
										<CreditCardIcon size={16} className="text-gray-500" />
										<p className="flex-grow font-medium">
											{getAccountLabel(selectedAccount.accountType)}: {selectedAccount.accountNumber}
										</p>
										<p className="text-green-600 font-medium">
											Bakiye: {formatToTurkishLira(selectedAccount.availableBalance)}
										</p>
									</div>
								</CardContent>
							</Card>
						</div>

						<div>
							<Label>Hisse Senedi Seç</Label>
							<SellStockSelector
								selectedStock={selectedStock}
								setSelectedStock={setSelectedStock}
								clientId={selectedAccount.clientId}
							/>
						</div>

						<div>
							<p className="text-red-500 text-sm font-medium mb-1">Emir Tipi *</p>
							<RadioGroup
								defaultValue="MARKET"
								value={executionType}
								onValueChange={(val: ExecutionType) => setExecutionType(val)}
							>
								<div className="flex items-center space-x-2">
									<RadioGroupItem value="MARKET" id="MARKET" />
									<Label htmlFor="MARKET">Piyasa Emri (Mevcut piyasa fiyatı)</Label>
								</div>
								<div className="flex items-center space-x-2">
									<RadioGroupItem value="LIMIT" id="LIMIT" />
									<Label htmlFor="LIMIT">Limit Emri (Manuel fiyat belirleme)</Label>
								</div>
							</RadioGroup>
						</div>

						<div className="flex gap-4">
							<div className="w-full">
								<Label className="mb-1">Fiyat (TL)</Label>
								<Input
									value={price.toFixed(2)}
									min={0}
									placeholder={executionType === "MARKET" ? "Piyasa Fiyatı" : "Limit Fiyat"}
									onChange={(e) => setPrice(Number(e.target.value) || 0)}
									type="number"
									disabled={executionType === "MARKET"}
									className={executionType === "MARKET" ? "bg-gray-100 cursor-not-allowed" : ""}
								/>
								{executionType === "MARKET" && (
									<p className="text-xs text-gray-500 mt-1">Piyasa fiyatı otomatik olarak güncellenir</p>
								)}
								{executionType === "LIMIT" && (
									<p className="text-xs text-gray-500 mt-1">
										Mevcut piyasa fiyatı: {selectedStock.currentPrice.toFixed(2)} TL
									</p>
								)}
							</div>
							<div className="w-full">
								<Label className="mb-1">Adet</Label>
								<Input
									value={quantity}
									min={0}
									step={1}
									placeholder="Adet"
									onChange={(e) => handleQuantityChange(Number(e.target.value) || 0)}
									type="number"
								/>
							</div>
							<div className="w-full">
								<Label className="mb-1">Toplam Maliyet</Label>
								<Input
									value={cost.toFixed(2)}
									min={0}
									step={price}
									placeholder="Toplam Maliyet"
									onChange={(e) => handleTotalCostChange(Number(e.target.value) || 0)}
									type="number"
								/>
							</div>
						</div>

						<div className="p-4 rounded bg-blue-50">
							<h1 className="mb-2">Komisyon ve Vergi Hesaplaması</h1>
							<div className="grid grid-cols-2 gap-4">
								<div className="py-1 flex justify-between items-center">
									<p className="text-gray-500">İşlem Tutarı:</p>
									<p>{formatToTurkishLira(cost)}</p>
								</div>
								<div className="py-1 flex justify-between items-center">
									<p className="text-gray-500">Komisyon (%0.2):</p>
									<p>{formatToTurkishLira(commission)}</p>
								</div>
								<div className="py-1 flex justify-between items-center">
									<p className="text-gray-500">BSMV (%5):</p>
									<p>{formatToTurkishLira(bsmv)}</p>
								</div>
								<div className="py-1 flex justify-between items-center col-span-2 border-t">
									<p className="text-gray-500">Toplam Maliyet:</p>
									<p className="text-red-500">{formatToTurkishLira(totalCost)}</p>
								</div>
							</div>
						</div>

						<SellStockPreviewDialog
							quantity={quantity}
							price={price}
							selectedStock={selectedStock}
							totalCost={totalCost}
							selectedAccount={selectedAccount}
							executionType={executionType}
						/>
					</CardContent>
				</Card>
			)}
		</div>
	);
}
