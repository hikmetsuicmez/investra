"use client";
import SearchClientBySearchTerm from "@/components/dashboard/customer-management/SearchClientBySearchTerm";

export default function Page() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-6">
      <div className="p-6 max-w-5xl w-full">
        <h1 className="text-2xl font-bold mb-4">Bakiye Yükleme - Müşteri Arama</h1>
        <h3> Bakiye yüklemek istediğiniz müşteriyi aşağıdaki arama kutusunu kullanarak bulun.</h3>
      <SearchClientBySearchTerm />
      </div>  
    </div>
  );
}
