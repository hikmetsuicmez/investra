"use client";
import CreateCustomerAccount from "@/components/dashboard/account-management/CreateCustomerAccount";
import { useParams } from "next/navigation";


export default function Page() {
    const params = useParams()
  
    const clientId = params.clientId as string

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-6">
      <div className="p-6 max-w-5xl w-full">
        <h1 className="text-2xl font-bold mb-4">Hesap Açılışı</h1>
        <h3>Oluşturulacak müşterinin bilgilerini giriniz.</h3>
        <CreateCustomerAccount clientId={clientId} />
      </div>
    </div>
  );
}
