"use client";

import { useEffect, useState } from "react";

export default function SimulationDateDisplay() {
  const [date, setDate] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchSimulationDate() {
      try {
        const res = await fetch("/api/system-time", {
          cache: "no-store", 
        });
        const data = await res.json();

        if (!res.ok) {
          setError(data.message || "Bir hata oluştu");
          return;
        }

        setDate(data.date);
      } catch (err: any) {
        setError(err.message || "Sunucu hatası");
      }
    }

    fetchSimulationDate();
  }, []);

  return (
    <div style={{
      position: "fixed",
      top: 1,
      right: 47,
      backgroundColor: "#222",
      color: "#fff",
      padding: "8px 12px",
      borderRadius: 6,
      fontSize: 14,
      zIndex: 9999,
    }}>
      {error && <span>Hata: {error}</span>}
      {!error && (date ? <span>Sistem tarihi: {date}</span> : <span>Yükleniyor...</span>)}
    </div>
  );
}
