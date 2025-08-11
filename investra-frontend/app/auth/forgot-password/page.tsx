"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Mail, Send } from "lucide-react";
import Link from "next/link";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { validateEmail } from "@/lib/validate-email";
import Image from "next/image";

export default function ForgotPassword() {
	const [email, setEmail] = useState("");
	const [isValidEmail, setIsValidEmail] = useState(false);
	const [isEmailSent, setIsEmailSent] = useState(false);
	const [emailSentFailed, setEmailSentFailed] = useState(false);

	const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const value = e.target.value;
		setEmail(value);
		setIsValidEmail(validateEmail(value));
	};

	async function handleSubmit(e: React.FormEvent) {
		e.preventDefault();

		try {
			const response = await fetch("/api/auth/forgot-password", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ email }),
			});

			const data = await response.json();

			if (response.ok) {
				setIsEmailSent(true);
			} else {
				setEmailSentFailed(true);
				console.error("Password reset failed:", data.message || "Unknown error");
			}
		} catch (error) {
			console.error("Network or server error:", error);
		}
	}

	return (
		<div className="w-full h-screen flex items-center justify-center bg-gray-300">
			<Card className="w-full max-w-sm bg-[#f6f5fa]">
				<CardHeader className="text-center">
					<div className="flex justify-center mb-2">
						<Image src={"/images/Investra-Logo.png"} alt="Investra logo" height={100} width={100} />
					</div>
					<CardTitle className="text-slate-800/90 text-2xl font-semibold">Şifre Sıfırlama</CardTitle>
					<CardDescription>E-posta adresinizi girin, şifre sıfırlama bağlantısını size gönderelim.</CardDescription>
				</CardHeader>

				<form onSubmit={handleSubmit}>
					<CardContent className="flex flex-col">
						<div className="grid gap-2 pb-6">
							<Label htmlFor="email">E-posta Adresi</Label>
							<div className="relative">
								<Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="email"
									name="email"
									type="text"
									autoComplete="email"
									required
									className="pl-10"
									value={email}
									onChange={handleChange}
								/>
							</div>
						</div>

						<Alert
							className={`border-green-600/70 border-0 border-l-4 bg-green-600/5 transition-all duration-300 ${
								isEmailSent ? "opacity-100 max-h-32 mb-6" : "opacity-0 max-h-0 h-0 p-0 -z-50"
							}`}
						>
							<AlertDescription className="text-green-600">
								Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.
							</AlertDescription>
						</Alert>

						<Button type="submit" className="w-full bg-blue-600 cursor-pointer" disabled={!isValidEmail}>
							<p>Sıfırlama Bağlantısı Gönder</p>
							<Send />
						</Button>
					</CardContent>
				</form>

				<CardFooter className="flex-col justify-center gap-2">
					<Link href={"/auth/login"} className="group flex-col items-center gap-2 text-sm text-blue-600">
						<div className="flex items-center gap-2">
							<ArrowLeft size={14} />
							<p>Geri Dön</p>
						</div>
						<hr className="border-t border-blue-600 opacity-0 group-hover:opacity-100 transition-opacity duration-200" />
					</Link>
				</CardFooter>
			</Card>
		</div>
	);
}
