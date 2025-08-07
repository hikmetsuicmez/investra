"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KeyRound, Lock, User, UserLock, Eye, EyeOff, ArrowRight } from "lucide-react";
import Link from "next/link";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { validateEmail } from "@/lib/validate-email";
import { redirect } from "next/navigation";

export default function Login() {
	const [showPassword, setShowPassword] = useState(false);
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [wrongCredentials, setWrongCredentials] = useState(false);

	const isFormValid = validateEmail(email) && password.length >= 8;

	async function handleSubmit(e: React.FormEvent) {
		e.preventDefault();

		if (!isFormValid) return;

		setIsSubmitting(true);

		const res = await fetch("/api/auth/login", {
			method: "POST",
			body: JSON.stringify({ email, password }),
			headers: { "Content-Type": "application/json" },
		});

		setIsSubmitting(false);

		if (res.ok) {
			setWrongCredentials(false);

			const { redirectTo } = await res.json();

			redirect(redirectTo);
		} else {
			setWrongCredentials(true);
		}
	}

	return (
		<div className="w-full h-screen flex items-center justify-center bg-gray-300">
			<Card className="w-full max-w-sm bg-gray-50">
				<CardHeader className="text-center">
					<div className="flex justify-center mb-2">
						<div className="bg-blue-600 p-3 rounded-full">
							<UserLock size={32} color="white" />
						</div>
					</div>
					<CardTitle className="text-slate-800/90 text-2xl font-semibold">Sisteme Giriş</CardTitle>
					<CardDescription>E-posta adresinizi ve şifrenizi girerek sisteme giriş yapın.</CardDescription>
				</CardHeader>

				<form onSubmit={handleSubmit}>
					<CardContent className="flex flex-col">
						<div className="grid gap-2 pb-6">
							<Label htmlFor="email" className="text-slate-800/90">
								E-posta
							</Label>
							<div className="relative">
								<User className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="email"
									name="email"
									type="text"
									autoComplete="email"
									required
									className="pl-10"
									value={email}
									onChange={(e) => setEmail(e.target.value)}
								/>
							</div>
						</div>

						<div className="grid gap-2">
							<Label htmlFor="password" className="text-slate-800/90">
								Şifre
							</Label>
							<div className="relative">
								<Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="password"
									name="password"
									type={showPassword ? "text" : "password"}
									autoComplete="current-password"
									required
									className="pl-10 pr-10"
									value={password}
									onChange={(e) => setPassword(e.target.value)}
								/>
								<div
									className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-muted-foreground"
									onMouseDown={() => setShowPassword(true)}
									onMouseUp={() => setShowPassword(false)}
									onMouseLeave={() => setShowPassword(false)}
									onTouchStart={() => setShowPassword(true)}
									onTouchEnd={() => setShowPassword(false)}
								>
									{showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
								</div>
							</div>

							<p
								className={`text-xs text-red-600 mt-1 overflow-hidden transition-all duration-300 ${
									password.length > 0 && password.length < 8 ? "opacity-100 max-h-6 pb-2" : "opacity-0 max-h-0"
								}`}
							>
								Şifre en az 8 karakter olmalıdır.
							</p>

							<Alert
								variant="destructive"
								className={`border-red-600/70 border-0 border-l-4 bg-red-600/5 transition-all duration-300 ${
									wrongCredentials ? "opacity-100 max-h-32 mb-6" : "opacity-0 max-h-0 h-0 p-0 -z-50"
								}`}
							>
								<AlertDescription>Kullanıcı bilgileriniz hatalıdır. Lütfen tekrar deneyiniz.</AlertDescription>
							</Alert>
						</div>

						<hr className="border-t border-neutral-200 pb-6" />

						<Button type="submit" className="w-full bg-blue-600 cursor-pointer" disabled={!isFormValid || isSubmitting}>
							<p>{isSubmitting ? "Giriş Yapılıyor..." : "Giriş Yap"}</p>
							<ArrowRight />
						</Button>
					</CardContent>
				</form>

				<CardFooter className="flex-col justify-center gap-2">
					<Link href="/auth/forgot-password" className="group flex-col justify-center gap-2 text-sm text-blue-600">
						<div className="flex items-center gap-2">
							<KeyRound size={14} />
							<p>Şifremi Unuttum?</p>
						</div>
						<hr className="border-t border-blue-600 opacity-0 group-hover:opacity-100 transition-opacity duration-200" />
					</Link>
				</CardFooter>
			</Card>
		</div>
	);
}
