"use client";

import {
  AlertDialog,
  AlertDialogTrigger,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Pencil } from "lucide-react";
import { Client } from "@/types/customers";
import { useState } from "react";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

interface EditCustomerDialogProps {
  customer: Client;
}

export default function EditCustomerDialog({
  customer,
}: EditCustomerDialogProps) {
  const [email, setEmail] = useState(customer.email || "");
  const [phone, setPhone] = useState(customer.phone || "");
  const [notes, setNotes] = useState(customer.notes || "");
  const router = useRouter();
  // Bireysel alanlar
  const [fullName, setFullName] = useState(customer.fullName || "");
  const [nationalityNumber, setNationalityNumber] = useState(
    customer.nationalityNumber || ""
  );
  const [birthDate, setBirthDate] = useState(customer.birthDate || "");

  // Kurumsal alanlar
  const [companyName, setCompanyName] = useState(customer.companyName || "");
  const [taxNumber, setTaxNumber] = useState(customer.taxNumber || "");
  const [companyType, setCompanyType] = useState(customer.companyType || "");
  const [sector, setSector] = useState(customer.sector || "");

  const handleSave = async () => {
    const updatedFields: Record<string, any> = {
      clientType: customer.clientType,
    };
    // Ortak alanlar
    if (email !== (customer.email || "")) updatedFields.email = email;
    if (phone !== (customer.phone || "")) updatedFields.phone = phone;
    if (notes !== (customer.notes || "")) updatedFields.notes = notes;

    // Bireysel alanlar
    if (customer.clientType === "INDIVIDUAL") {
      if (fullName !== (customer.fullName || ""))
        updatedFields.fullName = fullName;
      if (nationalityNumber !== (customer.nationalityNumber || ""))
        updatedFields.nationalityNumber = nationalityNumber;
      if (birthDate !== (customer.birthDate || ""))
        updatedFields.birthDate = birthDate;
    } else if (customer.clientType === "CORPORATE") {
      if (companyName !== (customer.companyName || ""))
        updatedFields.companyName = companyName;
      if (taxNumber !== (customer.taxNumber || ""))
        updatedFields.taxNumber = taxNumber;
      if (companyType !== (customer.companyType || ""))
        updatedFields.companyType = companyType;
      if (sector !== (customer.sector || "")) updatedFields.sector = sector;
    }

    if (Object.keys(updatedFields).length === 1) {
      toast("Değişiklik yapılmadı.");
      return;
    }
    try {
      const response = await fetch(
        `/api/clients/update-client/${customer.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(updatedFields),
        }
      );

      if (!response.ok) {
        toast("Müşteri  bilgileri değiştirilemedi.");
      } else {
        toast.success("Müşteri bilgileri başarıyla değiştirildi.", {
          onDismiss: () => {
            router.refresh();
          },
        });
      }
    } catch (error) {
      console.error(error);
      toast("Bir hata oluştu.");
    }
  };

  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant="ghost" size="sm">
          Düzenle
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Müşteri Düzenle</AlertDialogTitle>
        </AlertDialogHeader>

        {/* Ortak alanlar */}
        <div className="space-y-4">
          <div>
            <Label>Email</Label>
            <Input value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div>
            <Label>Telefon</Label>
            <Input value={phone} onChange={(e) => setPhone(e.target.value)} />
          </div>
          <div>
            <Label>Notlar</Label>
            <Input value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>

          {/* Bireysel müşteri alanları */}
          {customer.clientType === "INDIVIDUAL" && (
            <>
              <div>
                <Label>Ad Soyad</Label>
                <Input
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>
              <div>
                <Label>TCKN</Label>
                <Input
                  value={nationalityNumber}
                  onChange={(e) => setNationalityNumber(e.target.value)}
                />
              </div>
              <div>
                <Label>Doğum Tarihi</Label>
                <Input
                  type="date"
                  value={birthDate}
                  onChange={(e) => setBirthDate(e.target.value)}
                />
              </div>
            </>
          )}

          {/* Kurumsal müşteri alanları */}
          {customer.clientType === "CORPORATE" && (
            <>
              <div>
                <Label>Şirket Adı</Label>
                <Input
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                />
              </div>
              <div>
                <Label>Vergi Numarası</Label>
                <Input
                  value={taxNumber}
                  onChange={(e) => setTaxNumber(e.target.value)}
                />
              </div>
              <div>
                <Label>Şirket Türü</Label>
                <Input
                  value={companyType}
                  onChange={(e) => setCompanyType(e.target.value)}
                />
              </div>
              <div>
                <Label>Sektör</Label>
                <Input
                  value={sector}
                  onChange={(e) => setSector(e.target.value)}
                />
              </div>
            </>
          )}
        </div>

        <AlertDialogFooter>
          <AlertDialogCancel>Vazgeç</AlertDialogCancel>
          <AlertDialogAction onClick={handleSave}>Kaydet</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
