"use client";

import Deposit from "@/components/dashboard/account-management/Deposit";
import { useParams } from "next/navigation";

export default function DepositPage() {
  const params = useParams()

  const accountId = params.accountId as string
  const clientId = params.clientId as string

  return (
    <div style={{ maxWidth: 600, margin: "auto", padding: 20 }}>
      
      <Deposit accountId={accountId} clientId={clientId}  />
    </div>
  );
}
