"use client";
import SearchClientBySearchTerm from "@/components/dashboard/account-management/SearchAccountByClientId";

export default function Page() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-6">
      <div className="p-6 max-w-5xl w-full">
        <h1 className="text-2xl font-bold mb-4">Bakiye Yükleme - Hesaplar</h1>
        <h3> Bakiye yüklemesi yapılacak hesabı seçin</h3>
      <SearchClientBySearchTerm />
      </div>  
    </div>
  );
}
