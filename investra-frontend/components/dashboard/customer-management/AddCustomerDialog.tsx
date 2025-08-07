"use client";

import { useState } from "react";
import {
	AlertDialog,
	AlertDialogContent,
	AlertDialogHeader,
	AlertDialogTitle,
	AlertDialogCancel,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { CalendarIcon, UserPlus } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { format } from "date-fns";
import { tr } from "date-fns/locale";

interface AddCustomerDialogProps {
	open: boolean;
	onOpenChange: (open: boolean) => void;
}

type CitizenshipType = "tcVatandasi" | "yabanciUyruklu";

export interface IndividualCustomerInfo {
	clientType: "INDIVIDUAL";
	fullName: string;
	citizenshipType: CitizenshipType;
	nationalityNumber: string;
	email: string;
	birthDate: Date;
	profession: string;
	gender: string;
	educationStatus: string;
	phone: string;
	monthlyRevenue: string;
	estimatedTransactionVolume: string;
	notes: string;
	isActive: boolean;
}

type CompanyType = "as" | "ltd" | "kooperatif" | "kollektif" | "komandit";

export interface CorporateCustomerInfo {
	clientType: "CORPORATE";
	companyName: string;
	taxNumber: string;
	registrationNumber: string;
	companyType: CompanyType;
	email: string;
	address: string;
	sector: string;
	phone: string;
	monthlyRevenue: string;
	companyNotes: string;
	isActive: boolean;
}

export default function AddCustomerDialog({ open, onOpenChange }: AddCustomerDialogProps) {
	const [tab, setTab] = useState<"bireysel" | "kurumsal">("bireysel");

	const [individualInfo, setIndividualInfo] = useState<IndividualCustomerInfo>({
		clientType: "INDIVIDUAL",
		fullName: "",
		citizenshipType: "tcVatandasi",
		nationalityNumber: "",
		email: "",
		birthDate: new Date(),
		profession: "",
		gender: "",
		educationStatus: "",
		phone: "",
		monthlyRevenue: "",
		estimatedTransactionVolume: "",
		notes: "",
		isActive: true,
	});

	const [corporateInfo, setCorporateInfo] = useState<CorporateCustomerInfo>({
		clientType: "CORPORATE",
		companyName: "",
		taxNumber: "",
		registrationNumber: "",
		companyType: "as",
		email: "",
		address: "",
		sector: "",
		phone: "",
		monthlyRevenue: "",
		companyNotes: "",
		isActive: true,
	});

	function removeEmptyFields<T>(obj: T): Partial<T> {
		const entries = Object.entries(obj as Record<string, unknown>);

		return Object.fromEntries(
			entries.filter(([_, value]) => {
				if (value === "" || value === null || value === undefined) {
					return false;
				}
				if (value instanceof Date) {
					return true;
				}
				return true;
			})
		) as Partial<T>;
	}

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		const rawPayload = tab === "bireysel" ? individualInfo : corporateInfo;
		const cleanedPayload = removeEmptyFields(rawPayload);
		const payload = { ...cleanedPayload, clientType: rawPayload.clientType };

		try {
			const response = await fetch("/api/clients/create-client", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(payload),
			});

			const result = await response.json();

			if (response.ok) {
				console.log("API Response:", result);
				// Optionally show success message
				onOpenChange(false);
			} else {
				console.error("API Error:", result.message);
			}
		} catch (err) {
			console.error("Request failed:", err);
		}
		onOpenChange(false);
	};

	return (
		<AlertDialog open={open} onOpenChange={onOpenChange}>
			<AlertDialogContent className="max-w-lg rounded-lg shadow-lg p-6 bg-white max-h-4/5 overflow-y-auto">
				<AlertDialogHeader>
					<div className="flex justify-center mb-2">
						<div className="bg-blue-600 p-3 rounded-full">
							<UserPlus size={32} color="white" />
						</div>
					</div>
					<AlertDialogTitle className="text-center text-xl font-semibold mb-4">Müşteri Kayıt Formu</AlertDialogTitle>
				</AlertDialogHeader>

				{/* Tabs for switching customer type */}
				<Tabs value={tab} onValueChange={(value) => setTab(value as "bireysel" | "kurumsal")}>
					<TabsList className="flex justify-center w-full">
						<TabsTrigger value="bireysel">Bireysel</TabsTrigger>
						<TabsTrigger value="kurumsal">Kurumsal</TabsTrigger>
					</TabsList>
					<TabsContent value="bireysel">
						<form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
							<div className="flex flex-col gap-2">
								<Label htmlFor="name">Ad Soyad *</Label>
								<Input
									id="name"
									placeholder="Ad Soyad"
									value={individualInfo.fullName}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, fullName: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="citizenshipType">Vatandaşlık Türü *</Label>
								<Select
									onValueChange={(value) =>
										setIndividualInfo((prev) => ({ ...prev, citizenshipType: value as CitizenshipType }))
									}
									value={individualInfo.citizenshipType}
								>
									<SelectTrigger id="citizenshipType" className="w-full">
										<SelectValue placeholder="Vatandaşlık Tipi" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="tcVatandasi">T.C Vatandaşı</SelectItem>
										<SelectItem value="yabanciUyruklu">Yabancı Uyruklu</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div className="flex flex-col gap-2 col-span-2">
								<Label htmlFor="identityNumber">
									{individualInfo.citizenshipType === "tcVatandasi"
										? "TC Kimlik No / Pasaport No / Mavi Kart No *"
										: "Pasaport No / Yabancı Kimlik No *"}
								</Label>
								<Input
									id="identityNumber"
									placeholder={
										individualInfo.citizenshipType === "tcVatandasi"
											? "TC Kimlik No / Pasaport No / Mavi Kart No"
											: "Pasaport No / Yabancı Kimlik No"
									}
									value={individualInfo.nationalityNumber}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, nationalityNumber: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="email">E-posta *</Label>
								<Input
									id="email"
									placeholder="E-posta"
									type="email"
									value={individualInfo.email}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, email: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="birthDate">Doğum Tarihi</Label>
								<Popover>
									<PopoverTrigger asChild>
										<Button
											variant="outline"
											data-empty={!individualInfo.birthDate}
											className="data-[empty=true]:text-muted-foreground justify-start text-left font-normal"
										>
											<CalendarIcon />
											{individualInfo.birthDate ? (
												format(individualInfo.birthDate, "PPP", { locale: tr })
											) : (
												<span>Pick a date</span>
											)}
										</Button>
									</PopoverTrigger>
									<PopoverContent className="w-auto p-0">
										<Calendar
											mode="single"
											selected={individualInfo.birthDate}
											captionLayout="dropdown"
											onSelect={(date) =>
												setIndividualInfo((prev) => ({
													...prev,
													birthDate: date as Date,
												}))
											}
										/>
									</PopoverContent>
								</Popover>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="profession">Meslek</Label>
								<Input
									id="profession"
									placeholder="Meslek"
									value={individualInfo.profession}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, profession: e.target.value }))}
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="gender">Cinsiyet</Label>
								<Select
									onValueChange={(value) => setIndividualInfo((prev) => ({ ...prev, gender: value }))}
									value={individualInfo.gender}
								>
									<SelectTrigger id="gender" className="w-full">
										<SelectValue placeholder="Cinsiyet" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="erkek">Erkek</SelectItem>
										<SelectItem value="kadın">Kadın</SelectItem>
										<SelectItem value="diğer">Diğer</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="education">Eğitim Durumu</Label>
								<Input
									id="education"
									placeholder="Eğitim Durumu"
									value={individualInfo.educationStatus}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, educationStatus: e.target.value }))}
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="phone">Telefon Numarası *</Label>
								<Input
									id="phone"
									placeholder="Telefon Numarası"
									value={individualInfo.phone}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, phone: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="monthlyIncome">Aylık Gelir *</Label>
								<Input
									id="monthlyIncome"
									placeholder="Aylık Gelir"
									value={individualInfo.monthlyRevenue}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, monthlyRevenue: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="estimatedVolume">Tahmini İşlem Hacmi *</Label>
								<Select
									onValueChange={(value) =>
										setIndividualInfo((prev) => ({ ...prev, estimatedTransactionVolume: value }))
									}
									value={individualInfo.estimatedTransactionVolume}
								>
									<SelectTrigger id="estimatedVolume" className="w-full">
										<SelectValue placeholder="Tahmini İşlem Hacmi" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="0-500.000">0-500.000</SelectItem>
										<SelectItem value="500.000-1.500.000">500.000-1.500.000</SelectItem>
										<SelectItem value="1.500.000-3.000.000">1.500.000-3.000.000</SelectItem>
										<SelectItem value="3.000.000 ve üzeri">3.000.000 ve üzeri</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div className="flex flex-col gap-2 col-span-2">
								<Label htmlFor="notes">Notlar</Label>
								<Textarea
									id="notes"
									placeholder="Notlar"
									value={individualInfo.notes}
									onChange={(e) => setIndividualInfo((prev) => ({ ...prev, notes: e.target.value }))}
									className="resize-none"
								/>
							</div>

							{/* Buttons */}
							<div className="col-span-2 flex justify-end gap-4 mt-4">
								<AlertDialogCancel type="button" className="px-6 py-2 border rounded-md hover:bg-gray-100">
									İptal
								</AlertDialogCancel>
								<Button type="submit" className="bg-blue-600 hover:bg-blue-700 px-6 py-2 text-white rounded-md">
									Kaydet
								</Button>
							</div>
						</form>
					</TabsContent>

					<TabsContent value="kurumsal">
						<form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
							<div className="flex flex-col gap-2 col-span-2">
								<Label htmlFor="companyName">Kurum Ticari Adı *</Label>
								<Input
									id="companyName"
									placeholder="Kurum Ticari Adı"
									value={corporateInfo.companyName}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, companyName: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="taxNumber">Vergi No *</Label>
								<Input
									id="taxNumber"
									placeholder="Vergi No"
									value={corporateInfo.taxNumber}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, taxNumber: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="registryNumber">Sicil No *</Label>
								<Input
									id="registryNumber"
									placeholder="Sicil No"
									value={corporateInfo.registrationNumber}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, registrationNumber: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="companyType">Şirket Türü *</Label>
								<Select
									onValueChange={(value) =>
										setCorporateInfo((prev) => ({ ...prev, companyType: value as CompanyType }))
									}
									value={corporateInfo.companyType}
								>
									<SelectTrigger id="companyType" className="w-full">
										<SelectValue placeholder="Şirket Türü" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="as">A.Ş</SelectItem>
										<SelectItem value="ltd">LTD</SelectItem>
										<SelectItem value="kooperatif">Kooperatif</SelectItem>
										<SelectItem value="kollektif">Kollektif</SelectItem>
										<SelectItem value="komandit">Komandit</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="companyEmail">E-posta *</Label>
								<Input
									id="companyEmail"
									placeholder="E-posta"
									type="email"
									value={corporateInfo.email}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, email: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="sector">Sektör/Faaliyet Alanı *</Label>
								<Input
									id="sector"
									placeholder="Sektör/Faaliyet Alanı"
									value={corporateInfo.sector}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, sector: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="companyAddress">Adres *</Label>
								<Input
									id="companyAddress"
									placeholder="Adres"
									value={corporateInfo.address}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, address: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="phoneCompany">Telefon Numarası *</Label>
								<Input
									id="phoneCompany"
									placeholder="Telefon Numarası"
									value={corporateInfo.phone}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, phone: e.target.value }))}
									required
								/>
							</div>

							<div className="flex flex-col gap-2">
								<Label htmlFor="revenue">Ciro *</Label>
								<Select
									onValueChange={(value) => setCorporateInfo((prev) => ({ ...prev, monthlyRevenue: value }))}
									value={corporateInfo.monthlyRevenue}
								>
									<SelectTrigger id="revenue" className="w-full">
										<SelectValue placeholder="Ciro *" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="0-500.000">0-500.000</SelectItem>
										<SelectItem value="500.000-1.500.000">500.000-1.500.000</SelectItem>
										<SelectItem value="1.500.000-3.000.000">1.500.000-3.000.000</SelectItem>
										<SelectItem value="3.000.000 ve üzeri">3.000.000 ve üzeri</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div className="flex flex-col gap-2 col-span-2">
								<Label htmlFor="companyNotes">Notlar</Label>
								<Textarea
									id="companyNotes"
									placeholder="Notlar"
									value={corporateInfo.companyNotes}
									onChange={(e) => setCorporateInfo((prev) => ({ ...prev, companyNotes: e.target.value }))}
									className="resize-none"
								/>
							</div>

							{/* Buttons */}
							<div className="col-span-2 flex justify-end gap-4 mt-4">
								<AlertDialogCancel type="button" className="px-6 py-2 border rounded-md hover:bg-gray-100">
									İptal
								</AlertDialogCancel>
								<Button type="submit" className="bg-blue-600 hover:bg-blue-700 px-6 py-2 text-white rounded-md">
									Kaydet
								</Button>
							</div>
						</form>
					</TabsContent>
				</Tabs>
			</AlertDialogContent>
		</AlertDialog>
	);
}
