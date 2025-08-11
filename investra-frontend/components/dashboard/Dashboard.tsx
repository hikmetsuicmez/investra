"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useState, useEffect } from "react";
import { Loader2, XCircle, Search, X } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useRouter } from "next/navigation";
import SimulationDateDisplay from "@/components/dashboard/simulation-day/SimulationDayDisplay";

interface DailyValuation {
  clientId: number;
  clientName: string | null;
  totalPortfolioValue: number;
  unrealizedProfitLoss: number;
  dailyChangePercentage: number;
  valuationDate: string;
}

interface Stock {
  id: number;
  symbol: string;
  name: string;
  stockGroup: string;
  currentPrice: number;
}

export default function Dashboard() {
  const [isLoadingInitial, setIsLoadingInitial] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [valuations, setValuations] = useState<DailyValuation[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [isSearchView, setIsSearchView] = useState(false);
  const [stocks, setStocks] = useState<Stock[]>([]);

  const fetchAllValuations = async () => {
    setIsLoadingInitial(true);
    setError(null);
    try {
      const res = await fetch("/api/portfolios/get-current-valuations");
      const data = await res.json();
      if (!res.ok)
        throw new Error(data.message || "Mevcut veriler çekilemedi.");
      setValuations(data.valuations.data);
    } catch (err: any) {
      setError(err.message);
      setValuations(null);
    } finally {
      setIsLoadingInitial(false);
      setIsSearchView(false);
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    setIsSearching(true);
    setError(null);
    try {
      const res = await fetch(
        `/api/portfolios/get-single-client-valuation/${searchTerm.trim()}`
      );
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

  const fetchStocks = async () => {
    try {
      const response = await fetch("/api/stocks/buy/available");
      if (!response.ok) throw new Error("Hisseler alınamadı");
      const data = await response.json();
      setStocks(data.data.slice(0, 10)); 
    } catch (error) {
      console.error("Error fetching stocks:", error);
    }
  };

  useEffect(() => {
    fetchAllValuations();
    fetchStocks();
  }, []);

  const renderValuationTable = () => {
    if (isLoadingInitial) {
      return (
        <div className="text-center p-8">
          <Loader2 className="h-8 w-8 animate-spin mx-auto text-gray-500" />
          <p className="mt-2 text-sm text-gray-500">Veriler Yükleniyor...</p>
        </div>
      );
    }

    if (error) {
      return (
        <div className="p-4 bg-red-50 text-red-600 rounded-lg flex items-center justify-center">
          <XCircle className="mr-2 h-5 w-5" />
          {error}
        </div>
      );
    }

    if (valuations && valuations.length > 0) {
      const displayDate = new Date(
        valuations[0].valuationDate
      ).toLocaleDateString("tr-TR");
      const title = isSearchView
        ? `Arama Sonucu: Müşteri ID ${searchTerm}`
        : `Değerleme Sonuçları - ${displayDate}`;

      return (
        <Card className="overflow-auto shadow-md">
          <CardHeader>
            <CardTitle>{title}</CardTitle>
          </CardHeader>
          <CardContent>
            <CardContent>
              <div className="flex items-center gap-10 mb-2">
                <Input
                  placeholder="Müşteri ID'si ile ara..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                  className="max-w-xs"
                />
                <Button onClick={handleSearch} disabled={isSearching}>
                  {isSearching ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <Search className="mr-2 h-4 w-4" />
                  )}
                  Ara
                </Button>
                {isSearchView && (
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={handleClearSearch}
                    title="Aramayı Temizle"
                  >
                    <X className="h-4 w-4" />
                  </Button>
                )}
              </div>
            </CardContent>
            <div className="border rounded-lg">
              <div className="grid grid-cols-5 font-semibold bg-gray-100 p-3 text-sm text-gray-600">
                <div>Müşteri ID</div>
                <div>Müşteri Adı</div>
                <div className="text-right">Toplam Portföy Değeri</div>
                <div className="text-right">Gerçekleşmemiş K/Z</div>
                <div className="text-right">Günlük Değişim (%)</div>
              </div>
              {valuations.slice(0, 10).map((v) => (
                <div
                  key={v.clientId}
                  className="grid grid-cols-5 items-center p-3 hover:bg-gray-50 transition border-t text-sm"
                >
                  <div className="font-medium">{v.clientId}</div>
                  <div>{v.clientName || "-"}</div>
                  <div className="text-right">
                    {v.totalPortfolioValue.toLocaleString("tr-TR", {
                      style: "currency",
                      currency: "TRY",
                    })}
                  </div>
                  <div
                    className={`text-right font-semibold ${
                      v.unrealizedProfitLoss >= 0
                        ? "text-green-600"
                        : "text-red-600"
                    }`}
                  >
                    {v.unrealizedProfitLoss.toLocaleString("tr-TR", {
                      style: "currency",
                      currency: "TRY",
                    })}
                  </div>
                  <div
                    className={`text-right font-semibold ${
                      v.dailyChangePercentage >= 0
                        ? "text-green-600"
                        : "text-red-600"
                    }`}
                  >
                    %{v.dailyChangePercentage.toFixed(2)}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      );
    }

    return (
      <div className="bg-white rounded-xl shadow p-8 border text-center text-gray-500">
        Gösterilecek değerleme verisi bulunamadı.
      </div>
    );
  };

return (
  <div className="p-1 md:p-15 space-y-10 bg-gray-50 min-h-screen relative">
      <SimulationDateDisplay />
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {renderValuationTable()}

      <Card className="shadow-md overflow-auto">
        <CardHeader>
          <CardTitle>Hisse Senetleri</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Hisse Kodu</TableHead>
                <TableHead>Şirket Adı</TableHead>
                <TableHead>Hisse Türü</TableHead>
                <TableHead>Anlık Fiyat</TableHead>
                <TableHead>İşlemler</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {stocks.map((stock) => (
                <TableRow key={stock.id}>
                  <TableCell>{stock.symbol}</TableCell>
                  <TableCell>{stock.name}</TableCell>
                  <TableCell>{stock.stockGroup}</TableCell>
                  <TableCell>{stock.currentPrice}</TableCell>
                  <TableCell>AL/SAT</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  </div>
);

}
