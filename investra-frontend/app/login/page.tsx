"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KeyRound, Lock, User, UserLock, Eye, EyeOff, ArrowRight } from "lucide-react";
import Link from "next/link";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function Login() {
	const [showPassword, setShowPassword] = useState(false);
	const [username, setUsername] = useState("");
	const [password, setPassword] = useState("");
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [wrongCredentials, setWrongCredentials] = useState(false);

	// Validation: username not empty, password min 8 chars
	const isFormValid = username.trim() !== "" && password.length >= 8;

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();

		if (!isFormValid) return; // extra safety

		setIsSubmitting(true);

		// Simulate login process (replace with real login logic)
		setTimeout(() => {
			setIsSubmitting(false);
			// Reset fields or redirect on success here
		}, 2000);
	};

	return (
		<div className="w-full h-screen flex items-center justify-center bg-auth-gradient">
			<Card className="w-full max-w-sm">
				<CardHeader className="text-center">
					<div className="flex justify-center mb-2">
						<div className="bg-auth-gradient p-3 rounded-full">
							<UserLock size={32} color="white" />
						</div>
					</div>
					<CardTitle className="text-indigo-700/70 text-2xl font-semibold">Sisteme Giriş</CardTitle>
					<CardDescription>Kullanıcı adınızı ve şifrenizi girerek sisteme giriş yapın.</CardDescription>
				</CardHeader>

				<form onSubmit={handleSubmit}>
					<CardContent className="flex flex-col">
						<div className="grid gap-2 pb-6">
							<Label htmlFor="username">Kullanıcı adı</Label>
							<div className="relative">
								<User className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="username"
									name="username"
									type="text"
									autoComplete="username"
									required
									className="pl-10"
									value={username}
									onChange={(e) => setUsername(e.target.value)}
								/>
							</div>
						</div>

						<div className="grid gap-2">
							<Label htmlFor="password">Şifre</Label>
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
									wrongCredentials ? "opacity-100 max-h-32 mb-6" : "opacity-0 max-h-0 h-0"
								}`}
							>
								<AlertDescription>Kullanıcı bilgileriniz hatalıdır. Lütfen tekrar deneyiniz.</AlertDescription>
							</Alert>
						</div>

						{/* <div onClick={() => setWrongCredentials(!wrongCredentials)}>aaaa</div> */}

						<hr className="border-t border-neutral-200 pb-6" />

						<Button type="submit" className="w-full bg-auth-gradient" disabled={!isFormValid || isSubmitting}>
							<p>{isSubmitting ? "Giriş Yapılıyor..." : "Giriş Yap"}</p>
							<ArrowRight />
						</Button>
					</CardContent>
				</form>

				<CardFooter className="flex-col justify-center gap-2">
					<Link href="/forgot-password" className="flex items-center gap-2 text-sm text-indigo-700/70">
						<KeyRound size={14} />
						<p>Şifremi Unuttum?</p>
					</Link>
				</CardFooter>
			</Card>
		</div>
	);
}
