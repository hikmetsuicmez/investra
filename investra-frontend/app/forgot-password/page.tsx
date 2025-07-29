"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ArrowLeft, Mail, Send } from "lucide-react";
import Link from "next/link";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function ForgotPassword() {
	const [email, setEmail] = useState("");
	const [isValidEmail, setIsValidEmail] = useState(false);
	const [isEmailSent, setIsEmailSent] = useState(false);

	/**
	 * Validates whether a given string is a properly formatted email address.
	 *
	 * This function uses a regular expression to check that the input:
	 * - Contains exactly one "@" symbol,
	 * - Has no whitespace characters,
	 * - Has at least one character before and after the "@" symbol,
	 * - Contains a period (".") after the "@" with at least one character on each side.
	 *
	 * @param {string} email - The email address string to validate.
	 * @returns {boolean} - Returns true if the email matches the expected format, false otherwise.
	 */
	const validateEmail = (email: string) => {
		const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return regex.test(email);
	};

	const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const value = e.target.value;
		setEmail(value);
		setIsValidEmail(validateEmail(value));
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		// add your submission logic here
	};

	return (
		<div className="w-full h-screen flex items-center justify-center bg-auth-gradient">
			<Card className="w-full max-w-sm">
				<CardHeader className="text-center">
					<div className="flex justify-center mb-2">
						<div className="bg-auth-gradient p-3 rounded-full">
							<Mail size={32} color="white" />
						</div>
					</div>
					<CardTitle className="text-indigo-700/70 text-2xl font-semibold">Şifre Sıfırlama</CardTitle>
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
								isEmailSent ? "opacity-100 max-h-32 mb-6" : "opacity-0 max-h-0 h-0"
							}`}
						>
							<AlertDescription className="text-green-600">
								Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.
							</AlertDescription>
						</Alert>

						<Button type="submit" className="w-full bg-auth-gradient" disabled={!isValidEmail}>
							<p>Sıfırlama Bağlantısı Gönder</p>
							<Send />
						</Button>
					</CardContent>
				</form>

				<CardFooter className="flex-col justify-center gap-2">
					<Link href={"/login"} className="flex items-center gap-2 text-sm text-neutral-500">
						<ArrowLeft size={14} />
						<p>Giriş Sayfasına Dön</p>
					</Link>
				</CardFooter>
			</Card>
		</div>
	);
}
