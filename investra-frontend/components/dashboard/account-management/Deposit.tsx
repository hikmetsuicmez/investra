"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { CalendarIcon } from "lucide-react"
import { useRouter } from "next/navigation";

export default function Deposit({  clientId,accountId}: { clientId: string,accountId: string }) {
  const [description, setDescription] = useState("")
  const [amount, setAmount] = useState("")
  const [transactionDate, setTransactionDate] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState("")
  const router = useRouter();
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")

    if (!amount || !transactionDate) {
      setError("Lütfen tutar ve işlem tarihini giriniz.")
      setIsLoading(false)
      return
    }

    const deposit = {
      accountId: Number(accountId),
      clientId: Number(clientId),
      description,
      amount: Number(amount),
      transactionDate,
    }


    try {
      console.log(deposit)
      const res = await fetch(`/api/accounts/${clientId}/account/${accountId}/deposit`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(deposit),
      })
      if (!res.ok) throw new Error("İşlem başarısız oldu.")

      setDescription("")
      setAmount("")
      setTransactionDate("")
      router.push("/dashboard") 
    } catch (err: any) {
      setError(err.message || "Bir hata oluştu.")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <Card className="w-full max-w-2xl shadow-md border">
        <CardHeader>
          <CardTitle>Bakiye Yükleme İşlemi</CardTitle>
          <CardDescription>Aşağıdaki formu doldurarak bakiye yükleme işlemini gerçekleştirin.</CardDescription>
        </CardHeader>

        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="amount">Tutar <span className="text-destructive">*</span></Label>
              <Input
                id="amount"
                type="text"
                placeholder="Örn: 10000"
                value={amount}
                onChange={(e) => {
                  const val = e.target.value.replace(/[.,]/g, '')
                  if (/^\d*$/.test(val)) {
                    setAmount(val)
                  }
                }}
                required
              />

            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Açıklama (Opsiyonel)</Label>
              <Textarea
                id="description"
                placeholder="thy için yatırılan para"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="transactionDate">İşlem Tarihi <span className="text-destructive">*</span></Label>
              <div className="relative">
                <Input
                  id="transactionDate"
                  type="date"
                  value={transactionDate}
                  onChange={(e) => setTransactionDate(e.target.value)}
                  required
                />
                <CalendarIcon className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground size-4" />
              </div>
              <p className="text-sm text-muted-foreground">Yatırım hesabına paranın yattığı tarih</p>
            </div>

            <Alert variant="warning" className="border-l-4 border-yellow-500">
              <AlertTitle>Önemli Bilgi:</AlertTitle>
              <AlertDescription>
                Bakiye yükleme işlemi, yatırım hesabındaki mevcut bakiye durumuna göre değerlendirilir. Eğer yeterli bakiye yoksa provizyon alma seçeneği sunulacaktır.
              </AlertDescription>
            </Alert>

            {error && <p className="text-sm text-red-500">{error}</p>}

            <div className="flex justify-between">
              <Button type="button" variant="outline">Geri Dön</Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? "Yükleniyor..." : "✓ Onayla"}
              </Button>
            </div>
          </CardContent>
        </form>
      </Card>
    </div>
  )
}
