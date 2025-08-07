
"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Account } from "./Account"; 

export default function SeatchAccountByClientId() {
  const { clientId } = useParams();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchAccounts() {
      if (!clientId) return;

      setIsLoading(true);
      setError("");

      try {
        const res = await fetch(`/api/accounts/get-accounts?clientId=${clientId}`);
        const data = await res.json();

        if (res.ok && data.accounts) {
          setAccounts(data.accounts);
        } else {
          setError(data.message || "Hesaplar alınamadı");
        }
      } catch (err) {
        console.error("Fetch error:", err);
        setError("Sunucu hatası");
      } finally {
        setIsLoading(false);
      }
    }

    fetchAccounts();
  }, [clientId]);

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Müşteri Hesapları</h1>

      {isLoading && <p>Yükleniyor...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!isLoading && accounts.length === 0 && !error && (
        <p>Bu müşteriye ait hesap bulunamadı.</p>
      )}

      {accounts.length > 0 && (
        <div className="space-y-2">
          <div className="grid grid-cols-4 font-semibold bg-gray-100 p-2 text-sm md:text-base">
            <div>Hesap No</div>
            <div>Bakiye</div>
            <div>Oluşturulma</div>
            <div>Durum</div>
          </div>

          {accounts.map((account) => (
            <div
              key={account.id}
              className="grid grid-cols-4 items-center p-2 border-b text-sm md:text-base"
            >
              <div>{account.accountNumber}</div>
              <div>{account.balance} ₺</div>
              <div>{new Date(account.createdAt).toLocaleDateString()}</div>
              <div className={account.isActive ? "text-green-600" : "text-red-600"}>
                {account.isActive ? "Aktif" : "Pasif"}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
