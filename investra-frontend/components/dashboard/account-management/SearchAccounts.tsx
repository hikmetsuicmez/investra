"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Account } from "./Account";
import { useRouter } from "next/navigation";

export default function SearchAccountByClientId() {
  const { clientId } = useParams();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();

  const [activeDropdown, setActiveDropdown] = useState<number | null>(null);

  useEffect(() => {
    async function fetchAccounts() {
      if (!clientId) return;

      setIsLoading(true);
      setError("");

      try {
        const res = await fetch(
          `/api/accounts/get-accounts?clientId=${clientId}`
        );
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

  const toggleDropdown = (accountId: number) => {
    if (activeDropdown === accountId) {
      setActiveDropdown(null);
    } else {
      setActiveDropdown(accountId);
    }
  };

  const handleAction = (accountId: number, action: "deposit" | "withdrawal") => {
    setActiveDropdown(null);
    router.push(`/accounts/${clientId}/account/${accountId}/${action}`);
  };
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
          <div className="grid grid-cols-5 font-semibold bg-gray-100 p-2 text-sm md:text-base">
            <div>Hesap No</div>
            <div>Bakiye</div>
            <div>Oluşturulma</div>
            <div>Durum</div>
            <div>İşlem</div>
          </div>

          {accounts.map((account) => (
            <div
              key={account.id}
              className="grid grid-cols-5 items-center p-2 border-b text-sm md:text-base relative"
            >
              <div>{account.accountNumber}</div>
              <div>{account.balance} ₺</div>
              <div>{new Date(account.createdAt).toLocaleDateString()}</div>
              <div
                className={account.isActive ? "text-green-600" : "text-red-600"}
              >
                {account.isActive ? "Aktif" : "Pasif"}
              </div>

              <div>
                <button
                  onClick={() => toggleDropdown(account.id)}
                  className="bg-blue-500 text-white px-3 py-1 rounded"
                >
                  İşlem
                </button>

                {activeDropdown === account.id && (
                  <div className="absolute right-0 mt-8 bg-white border rounded shadow-lg z-10 w-40">
                    <button
                      onClick={() => handleAction(account.id, "deposit")}
                      className="block w-full text-left px-4 py-2 hover:bg-gray-100"
                    >
                      Bakiye Yükleme
                    </button>
                    <button
                      onClick={() => handleAction(account.id, "withdrawal")}
                      className="block w-full text-left px-4 py-2 hover:bg-gray-100"
                    >
                      Bakiye Çıkışı
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
