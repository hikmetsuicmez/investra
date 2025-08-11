// Customer Types
export type IndividualCustomer = {
	id: number;
	clientType: "INDIVIDUAL";
	email: string;
	phone: string;
	address: string | null;
	notes: string | null;
	status: string | null;
	isActive: boolean;
	createdAt: string;
	fullName: string;
	nationalityType: string | null;
	taxId: null;
	passportNo: string | null;
	blueCardNo: string | null;
	nationalityNumber: string | null;
	birthDate: string;
	profession: string | null;
	gender: string | null;
	educationStatus: string | null;
	monthlyIncome: string | null;
	estimatedTransactionVolume: string | null;

	// Corporate-only fields set to null
	companyName: null;
	taxNumber: null;
	registrationNumber: null;
	companyType: null;
	sector: null;
	monthlyRevenue: null;
};

export type CorporateCustomer = {
	id: number;
	clientType: "CORPORATE";
	email: string;
	phone: string;
	address: string | null;
	notes: string | null;
	status: string | null;
	isActive: boolean;
	createdAt: string;
	fullName: null;
	nationalityType: null;
	taxId: null;
	passportNo: null;
	blueCardNo: null;
	nationalityNumber: null;
	birthDate: null;
	profession: null;
	gender: null;
	educationStatus: null;
	monthlyIncome: null;
	estimatedTransactionVolume: null;

	companyName: string;
	taxNumber: string;
	registrationNumber: string | null;
	companyType: string;
	sector: string;
	monthlyRevenue: number | null;
};

export type Customer = IndividualCustomer | CorporateCustomer;

// Account Types
export type AccountType = "SETTLEMENT" | "INVESTMENT" | "CURRENT" | "SAVINGS" | "DEPOSIT";

export type Account = {
	id: number;
	nickname: string;
	accountNumber: string;
	iban: string;
	accountNumberAtBroker: string;
	brokerName: string;
	brokerCode: string;
	custodianName: string;
	custodianCode: string;
	currency: string;
	balance: number;
	availableBalance: number;
	accountType: AccountType;
	createdAt: string;
	clientName: string;
	clientId: number;
	primarySettlement: boolean;
};

// Stock Types
export type Stock = {
	id: number;
	name: string;
	symbol: string | null;
	currentPrice: number;
	stockGroup: string;
	isActive: boolean;
	source: string | null;
	availableQuantity?: number;
	closePrice?: number;
	stockCode?: string;
	companyName?: string;
	sector?: string;
};

// Execution Types
export type ExecutionType = "MARKET" | "LIMIT";

// Order Types
export type BuyOrderResults = {
	accountNumber: string;
	operation: string;
	stockName: string;
	stockSymbol: string;
	price: number;
	quantity: number;
	tradeDate: string;
	valueDate: string;
	totalAmount: number;
	stockGroup: string;
	commission: number;
	bsmv: number;
	totalTaxAndCommission: number;
	netAmount: number;
	executionType: string;
	previewId?: string;
};

// Component Props Types
export type StockSelectorProps = {
	selectedStock: Stock;
	setSelectedStock: React.Dispatch<React.SetStateAction<Stock>>;
	clientId?: number
};

export type BuyStockPreviewDialogProps = {
	selectedStock: Stock;
	quantity: number;
	totalCost: number;
	selectedAccount: Account;
	executionType: ExecutionType;
};

export type TradeOrder = {
	id: number;
	clientId: number;
	clientFullName: string;
	stockCode: string;
	orderType: string;
	quantity: number;
	price: number;
	status: string;
	submittedAt: string;
	settledAt: string | null;
	settlementStatus: string;
	tradeDate: string;
	settlementDaysRemaining: number;
};