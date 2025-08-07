"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useState } from "react";
import { useRouter } from "next/navigation"; 
import { Client } from "./Client";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export default function SearchClientBySearchTerm () {
  const [searchTerm, setSearchTerm] = useState("");
  const [client, setClient] = useState<Client | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [notFound, setNotFound] = useState(false);
  const [searchType, setSearchType] = useState<
    "TCKN" | "VERGI_ID" | "MAVI_KART_NO" | "PASSPORT_NO" | "VERGI_NO" | "ISIM"
  >("TCKN");

  const router = useRouter(); 

  async function handleSearch() {
    if (!searchTerm.trim()) return;

    setIsLoading(true);
    setNotFound(false);

    try {
      const token = localStorage.getItem("token");
      const res = await fetch("/api/clients/get-client-by-search-term", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          searchTerm: searchTerm.trim(),
          searchType,
          isActive: true,
        }),
      });

      const data = await res.json();

      if (res.ok && data.client) {
        setClient(data.client);
        setNotFound(false);
      } else {
        setClient(null);
        setNotFound(true);
      }
    } catch (err) {
      console.error("Arama hatası:", err);
      setClient(null);
      setNotFound(true);
    } finally {
      setIsLoading(false);
    }
  }

  function handleSelectCustomer() {
    if (client?.id) {
      router.push(`/dashboard/account-selection/${client.id}`);

    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row gap-2">
        <Input
          placeholder="TCKN / Müşteri No / İsim / Soyisim"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full"
        />

        <Select value={searchType} onValueChange={(value) => setSearchType(value as any)}>
          <SelectTrigger className="w-40">
            <SelectValue placeholder="Arama Tipi" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TCKN">TCKN</SelectItem>
            <SelectItem value="VERGI_ID">Vergi Kimlik No</SelectItem>
            <SelectItem value="MAVI_KART_NO">Mavi Kart No</SelectItem>
            <SelectItem value="PASSPORT_NO">Pasaport No</SelectItem>
            <SelectItem value="VERGI_NO">Vergi No</SelectItem>
            <SelectItem value="ISIM">İsim</SelectItem>
          </SelectContent>
        </Select>

        <Button onClick={handleSearch} disabled={isLoading}>
          Ara
        </Button>
      </div>

      {client && (
        <div className="overflow-auto border rounded-md">
          <div className="grid grid-cols-7 font-semibold bg-gray-100 p-4 text-sm md:text-base">
            <div>Ad Soyad</div>
            <div>Müşteri No</div>
            <div>TCKN</div>
            <div>Vergi No</div>
            <div>Telefon</div>
            <div>E-posta</div>
            <div>Durum</div>
          </div>

          <div
            className="grid grid-cols-7 items-center p-4 hover:bg-gray-50 cursor-pointer transition"
            onClick={handleSelectCustomer}
          >
            <div>{client.fullName}</div>
            <div>{client.id}</div>
            <div>{client.nationalityNumber}</div>
            <div>{client.taxId || "-"}</div>
            <div>{client.phone || "-"}</div>
            <div>{client.email || "-"}</div>
            <div className={client.isActive ? "text-green-600" : "text-red-600"}>
              {client.isActive ? "Aktif" : "Pasif"}
            </div>
          </div>
        </div>
      )}

      {notFound && <p className="text-sm text-red-500">Müşteri bulunamadı.</p>}
    </div>
  );
}
