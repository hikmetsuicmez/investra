export type Account = {
  id: number;
  accountNickname: string;
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
  accountType: 'INVESTMENT' | 'SETTLEMENT'; // Enum değerlerine göre uyarlayabilirsin
  isPrimarySettlement: boolean;
  createdAt: string;
  updatedAt: string;
  clientId: number;
};
