"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useRouter } from "next/navigation"

type Account = {
  id: string
  currency: string
  availableBalance: number
  currentBalance: number
}

const dummyAccounts: Account[] = [
  {
    id: "1234567890",
    currency: "TRY",
    availableBalance: 95450.75,
    currentBalance: 125450.75,
  },
  {
    id: "1234567891",
    currency: "USD",
    availableBalance: 3750.25,
    currentBalance: 8750.25,
  },
]

export default function Withdrawal({ clientId }: { clientId: string }) {
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null)
  const [description, setDescription] = useState("")
  const [amount, setAmount] = useState("")
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (!amount || !selectedAccount) {
      setError("Lütfen bir hesap ve tutar giriniz.")
      return
    }

    if (Number(amount) > selectedAccount.availableBalance) {
      setError("Tutar, kullanılabilir bakiyeden büyük olamaz.")
      return
    }

    const withdrawal = {
      accountId: Number(selectedAccount.id),
      clientId: Number(clientId),
      description,
      amount: Number(amount),
    }

    setIsLoading(true)
    try {
      const res = await fetch(`/api/accounts/${clientId}/account/${selectedAccount.id}/withdrawal`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(withdrawal),
      })
      if (!res.ok) throw new Error("İşlem başarısız oldu.")

      setDescription("")
      setAmount("")
      router.push("/dashboard")
    } catch (err: any) {
      setError(err.message || "Bir hata oluştu.")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <Card className="w-full max-w-3xl shadow-md border">
        <CardHeader>
          <CardTitle>Hesap Seçimi ve Bakiye Çıkışı</CardTitle>
          <CardDescription>Lütfen bakiye çıkışı yapılacak hesabı ve işlem detaylarını girin.</CardDescription>
        </CardHeader>

        <CardContent className="space-y-6">
          <div className="space-y-4">
            <Label className="text-md font-semibold">Müşteri Hesapları</Label>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {dummyAccounts.map((acc) => (
                <Button
                  key={acc.id}
                  variant={selectedAccount?.id === acc.id ? "default" : "outline"}
                  onClick={() => setSelectedAccount(acc)}
                  className="text-left h-auto p-4 flex flex-col items-start"
                >
                  <div className="text-sm text-muted-foreground">Hesap No: {acc.id}</div>
                  <div className="text-sm">Kullanılabilir Bakiye: <strong>{acc.availableBalance} {acc.currency}</strong></div>
                  <div className="text-sm">Güncel Bakiye: {acc.currentBalance} {acc.currency}</div>
                </Button>
              ))}
            </div>
          </div>

          {selectedAccount && (
            <form onSubmit={handleSubmit} className="space-y-6 pt-4 border-t mt-4">
              <div className="space-y-2">
                <Label htmlFor="amount">Tutar <span className="text-destructive">*</span></Label>
                <Input
                  id="amount"
                  type="text"
                  placeholder={`Örn: 1000 (${selectedAccount.currency})`}
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
                  - Bakiye çıkışı sadece seçilen hesabın <strong>kullanılabilir bakiyesi</strong> kadar olabilir.<br />
                  - Para birimi <strong>{selectedAccount.currency}</strong> olarak otomatik belirlenmiştir.
                </AlertDescription>
              </Alert>

              {error && <p className="text-sm text-red-500">{error}</p>}

              <div className="flex justify-between">
                <Button type="button" variant="outline" onClick={() => setSelectedAccount(null)}>← Hesap Seçimini Değiştir</Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? "Yükleniyor..." : "✓ Onayla"}
                </Button>
              </div>
            </form>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
