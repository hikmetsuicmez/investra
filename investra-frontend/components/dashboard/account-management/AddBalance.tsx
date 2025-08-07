"use client";

import { useState } from "react";
import { useParams } from "next/navigation";

import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from "@/components/ui/card";

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function AddBalance() {
  const { accountId, clientId } = useParams();

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [transactionDate, setTransactionDate] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    if (!amount || !transactionDate) {
      setError("Lütfen miktar ve işlem tarihini giriniz.");
      setIsLoading(false);
      return;
    }

    const deposit = {
      accountId: accountId ?? null,
      clientId: clientId ?? null,
      description,
      amount: Number(amount),
      transactionDate,
    };

    try {
      const res = await fetch(`/api/accounts/${accountId}/deposit`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(deposit),
      });
      if (!res.ok) {
        throw new Error("İşlem başarısız oldu.");
      }

      setDescription("");
      setAmount("");
      setTransactionDate("");
    } catch (err: any) {
      setError(err.message || "Bir hata oluştu.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="max-w-md mx-auto mt-8">
      <CardHeader>
        <CardTitle>Hesaba Bakiye Ekle</CardTitle>
        <CardDescription>
          Lütfen işleminizi detaylandırarak formu doldurun.
        </CardDescription>
      </CardHeader>

      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-4">
          <div>
            <Label htmlFor="description">Açıklama</Label>
            <Input
              id="description"
              type="text"
              placeholder="İşlem açıklaması"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div>
            <Label htmlFor="amount">Miktar</Label>
            <Input
              id="amount"
              type="number"
              step="0.01"
              min="0"
              placeholder="Miktar giriniz"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
          </div>

          <div>
            <Label htmlFor="transactionDate">İşlem Tarihi</Label>
            <Input
              id="transactionDate"
              type="date"
              value={transactionDate}
              onChange={(e) => setTransactionDate(e.target.value)}
            />
          </div>

          {error && (
            <Alert variant="destructive" className="mt-2">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
        </CardContent>

        <CardFooter>
          <Button type="submit" disabled={isLoading} className="w-full">
            {isLoading ? "Gönderiliyor..." : "Gönder"}
          </Button>
        </CardFooter>
      </form>
    </Card>
  );
}
