"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useRouter } from "next/navigation";
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogAction,
} from "@/components/ui/alert-dialog";

export default function Deposit({  clientId,accountId}: { clientId: string,accountId: string }) {
  const [description, setDescription] = useState("")
  const [amount, setAmount] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState("")
  const router = useRouter();

  const [showSuccess, setShowSuccess] = useState(false)
  const [confirmationData, setConfirmationData] = useState<{
    accountNumber: string;
    amount: number
    date: string
    description: string
  } | null>(null)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")

    if (!amount) {
      setError("Lütfen tutar giriniz.")
      setIsLoading(false)
      return
    }

    const deposit = {
      accountId: Number(accountId),
      clientId: Number(clientId),
      description,
      amount: Number(amount), 
    }


    try {
      const res = await fetch(`/api/accounts/${clientId}/account/${accountId}/deposit`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(deposit),
      })
      const data = await res.json()

      if (!res.ok) {
        throw new Error(data?.message || "İşlem başarısız oldu.")
      }

      setConfirmationData({
        accountNumber: data.data.accountNumber,
        amount: Number(amount),
        date: new Date().toLocaleDateString("tr-TR"),
        description: description || "—",
      })
      setShowSuccess(true)

      setDescription("")
      setAmount("")
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
      {showSuccess && confirmationData && (
        <AlertDialog open={showSuccess} onOpenChange={setShowSuccess}>
          <AlertDialogContent className="bg-green-50 text-green-800">
            <AlertDialogHeader>
              <AlertDialogTitle className="text-green-700 text-lg font-bold">
                ✔ İşlem Başarılı
              </AlertDialogTitle>
              <AlertDialogDescription className="mt-2 text-sm text-green-800 space-y-1">
                <p>
                  <strong>Yükleme Yapılan Tutar:</strong>{" "}
                  {confirmationData.amount.toLocaleString("tr-TR")} TRY
                </p>
                <p>
                  <strong>İşlem Tarihi:</strong> {confirmationData.date}
                </p>
                <p>
                  <strong>Hesap:</strong> {confirmationData.accountNumber}
                </p>
                <p>
                  <strong>Açıklama:</strong> {confirmationData.description}
                </p>
              </AlertDialogDescription>
            </AlertDialogHeader>

            <AlertDialogFooter>
              <AlertDialogAction
                onClick={() => {
                  setShowSuccess(false);
                  router.push("/dashboard");
                }}
              >
                Tamam
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      )}
    </div>
  )
}
