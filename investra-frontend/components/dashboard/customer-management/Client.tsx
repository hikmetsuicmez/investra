export type Client = {
  id: number | null;

  clientType: "INDIVIDUAL" | "CORPORATE"; 

  email: string | null;

  phone: string | null;

  address: string | null;

  notes: string | null;

  status: "ACTIVE" | "INACTIVE" | "SUSPENDED" | "DELETED"|null; 

  isActive: boolean | null;

  createdAt: string | null; 

  // Bireysel müşteri alanları
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

  // Kurumsal müşteri alanları
  companyName: string | null;

  taxNumber: string | null;

  registrationNumber: string | null;

  companyType: string | null;

  sector: string | null;

  monthlyRevenue: number | null; 
};
