"use client";

import Withdrawal from "@/components/dashboard/account-management/Withdrawal";
import { useParams } from "next/navigation";

export default function WithdrawalPage() {
  const params = useParams()

  const accountId = params.accountId as string
  const clientId = params.clientId as string

  return (
    <div style={{ maxWidth: 1000, margin: "auto", padding: 20 }}>
      
      <Withdrawal accountId={accountId} clientId={clientId}  />
    </div>
  );
}
