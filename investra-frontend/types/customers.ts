// Client type for customer data
export type Client = {
  id: number | null;
  clientType: "INDIVIDUAL" | "CORPORATE"; 
  email: string | null;
  phone: string | null;
  address: string | null;
  notes: string | null;
  status: "ACTIVE" | "INACTIVE" | "SUSPENDED" | "DELETED" | null; 
  isActive: boolean | null;
  createdAt: string | null; 

  // Individual customer fields
  fullName: string | null;
  nationalityType: boolean | null;
  taxId: string | null;
  passportNo: string | null;
  blueCardNo: string | null;
  nationalityNumber: string | null;
  birthDate: string | null;
  profession: string | null;
  gender: "MALE" | "FEMALE" | "OTHER" | null;
  educationStatus: string | null;
  monthlyIncome: number | null;
  estimatedTransactionVolume: "0-500.000" | "500.000-1.500.000" | "1.500.000-3.000.000" | "3.000.000 ve üzeri" | null; 

  // Corporate customer fields
  companyName: string | null;
  taxNumber: string | null;
  registrationNumber: string | null;
  companyType: string | null;
  sector: string | null;
  monthlyRevenue: number | null; 
};

// Citizenship type for individual customers
export type CitizenshipType = "tcVatandasi" | "yabanciUyruklu";

// Company type for corporate customers
export type CompanyType = "as" | "ltd" | "kooperatif" | "kollektif" | "komandit";

// Individual customer information interface
export interface IndividualCustomerInfo {
	id?: number; 
	clientType: "INDIVIDUAL";
	fullName: string;
	citizenshipType: CitizenshipType;
	nationalityNumber: string;
	email: string;
	birthDate: Date;
	profession: string;
	gender: string;
	educationStatus: string;
	phone: string;
	monthlyRevenue: string;
	estimatedTransactionVolume: string;
	notes: string;
	isActive: boolean;
	taxType?: string;
}

// Corporate customer information interface
export interface CorporateCustomerInfo {
	id?: number; 
	clientType: "CORPORATE";
	companyName: string;
	taxNumber: string;
	companyType: CompanyType;
	email: string;
	address: string;
	sector: string;
	phone: string;
	monthlyRevenue: string;
	companyNotes: string;
	isActive: boolean;
	taxType?: string;
}

// Props for AddCustomerDialog component
export interface AddCustomerDialogProps {
	open: boolean;
	onOpenChange: (open: boolean) => void;
}

// Props for DeleteCustomerDialog component
export interface DeleteCustomerDialogProps {
	customer: IndividualCustomerInfo | CorporateCustomerInfo;
	onDeleted?: () => void;
	disabled: boolean;
}

// Props for CustomerTable component
export interface CustomerTableProps {
	customers: (IndividualCustomerInfo | CorporateCustomerInfo)[];
}

// Customer display mapping result
export interface CustomerDisplayInfo {
	name: string;
	type: string;
	phone: string;
	email: string;
	status: string;
}

// Search types for client search
export type SearchType = "TCKN" | "VERGI_ID" | "MAVI_KART_NO" | "PASSPORT_NO" | "VERGI_NO" | "ISIM";

// Client type enum
export type ClientType = "INDIVIDUAL" | "CORPORATE";

// Customer status enum
export type CustomerStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED" | "DELETED";

// Gender enum
export type Gender = "MALE" | "FEMALE" | "OTHER";

// Estimated transaction volume enum
export type EstimatedTransactionVolume = "0-500.000" | "500.000-1.500.000" | "1.500.000-3.000.000" | "3.000.000 ve üzeri"; 