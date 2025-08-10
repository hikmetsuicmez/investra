"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useState, useEffect } from "react";
import { Loader2, XCircle, RefreshCw, Search, X } from "lucide-react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { useRouter } from "next/navigation"; 
import SimulationDateDisplay from "@/components/dashboard/simulation-day/SimulationDayDisplay";


interface DailyValuation {
    clientId: number;
    clientName: string | null;
    totalPortfolioValue: number;
    unrealizedProfitLoss: number;
    dailyChangePercentage: number;
    valuationDate: string;
    positions: any[]; 
}

export default function EndOfDayValution() {
  const [isLoadingInitial, setIsLoadingInitial] = useState(true); 
  const [isLoadingProcess, setIsLoadingProcess] = useState(false); 
  const [isSearching, setIsSearching] = useState(false);
  const router = useRouter(); 

  const [valuations, setValuations] = useState<DailyValuation[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [isSearchView, setIsSearchView] = useState(false);


  const fetchAllValuations = async () => {
    setIsLoadingInitial(true);
    setError(null);
    try {
      const res = await fetch("/api/portfolios/get-current-valuations");
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Mevcut veriler çekilemedi.");
      
      setValuations(data.valuations.data);

    } catch (err: any) {
      setError(err.message);
      setValuations(null);
    } finally {
      setIsLoadingInitial(false);
      setIsSearchView(false);
    }
  };
  
  useEffect(() => {
    fetchAllValuations();
  }, []);

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    setIsSearching(true);
    setError(null);
    try {
      const res = await fetch(`/api/portfolios/get-single-client-valuation/${searchTerm.trim()}`);
      const data = await res.json();
      if (!res.ok) throw new Error(data.message);
      
      setValuations(data.valuation ? [data.valuation] : []);
      setIsSearchView(true);

    } catch (err: any) {
      setError(err.message);
      setValuations(null);
    } finally {
      setIsSearching(false);
    }
  };

  const handleClearSearch = () => {
    setSearchTerm("");
    fetchAllValuations();
  };

  const handleProcessAllValuations = async () => {
    setIsLoadingProcess(true);
    setError(null);
    try {
      const res = await fetch("/api/portfolios/process-valuations", { method: "POST" });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Gün sonu işlemi başarısız.");
      
      alert("Gün sonu değerlemesi başarıyla tamamlandı. Çıkış yapılıyor."); 
      router.push("/auth/login"); 

    } catch (err: any) {
      setError(err.message);
    } finally {
      setIsLoadingProcess(false);
    }
  };

  const renderValuationTable = () => {
    if (isLoadingInitial) {
      return <div className="text-center p-8"><Loader2 className="h-8 w-8 animate-spin mx-auto" /></div>;
    }
    if (error) {
      return <div className="p-4 bg-red-50 text-red-600 rounded-lg flex items-center justify-center"><XCircle className="mr-2 h-4 w-4" />{error}</div>;
    }
    if (valuations && valuations.length > 0) {
      return (
        <div className="bg-white rounded-xl shadow overflow-auto border">
          <SimulationDateDisplay />

          <h2 className="p-4 text-lg font-semibold border-b">
            Değerleme Sonuçları - {new Date(valuations[0].valuationDate).toLocaleDateString('tr-TR')}
          </h2>
          <div className="grid grid-cols-5 font-semibold bg-gray-100 p-4 text-sm">
            <div>Müşteri ID</div>
            <div>Müşteri Adı</div>
            <div>Toplam Portföy Değeri</div>
            <div>Gerçekleşmemiş K/Z</div>
            <div>Günlük Değişim (%)</div>
          </div>
          {valuations.map((v) => (
            <div key={v.clientId} className="grid grid-cols-5 items-center p-4 hover:bg-gray-50 transition border-t">
              <div>{v.clientId}</div>
              <div>{v.clientName || "-"}</div>
              <div>{v.totalPortfolioValue.toLocaleString("tr-TR", { style: "currency", currency: "TRY" })}</div>
              <div className={v.unrealizedProfitLoss >= 0 ? 'text-green-600' : 'text-red-600'}>{v.unrealizedProfitLoss.toLocaleString("tr-TR", { style: "currency", currency: "TRY" })}</div>
              <div className={v.dailyChangePercentage >= 0 ? 'text-green-600' : 'text-red-600'}>%{v.dailyChangePercentage.toFixed(2)}</div>
            </div>
          ))}
        </div>
      );
    }
    return (
      <div className="bg-white rounded-xl shadow p-6 border text-center text-gray-500">
        Gösterilecek değerleme verisi bulunamadı.
      </div>
    );
  };

  return (
    <div className="flex flex-col h-screen bg-gray-100 p-6 space-y-6">
      <div className="p-4 mb-4 bg-white rounded-xl shadow space-y-4">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-semibold">Gün Sonu Değerleme</h1>
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button disabled={isLoadingProcess || isLoadingInitial}>
                {isLoadingProcess ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <RefreshCw className="mr-2 h-4 w-4" />}
                Yeni Gün Değerlemesini Başlat
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>İşlemi Onaylıyor musunuz?</AlertDialogTitle>
                <AlertDialogDescription>
                  Bu işlem tarihi bir gün ilerletecek ve tüm müşteriler için portföyleri yeniden değerlendirecektir. Devam etmek istiyor musunuz?
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>İptal</AlertDialogCancel>
                <AlertDialogAction onClick={handleProcessAllValuations}>Evet, Başlat</AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
        
        <div className="flex items-center gap-2 border-t pt-4">
          <Input 
            placeholder="Müşteri ID'si ile ara..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            className="max-w-xs"
          />
          <Button onClick={handleSearch} disabled={isSearching}>
            {isSearching ? <Loader2 className="mr-2 h-4 w-4 animate-spin"/> : <Search className="mr-2 h-4 w-4"/>}
            Ara
          </Button>
          {isSearchView && (
            <Button variant="outline" size="icon" onClick={handleClearSearch}>
              <X className="h-4 w-4"/>
            </Button>
          )}
        </div>
      </div>
      
      {renderValuationTable()}
    </div>
  );
}