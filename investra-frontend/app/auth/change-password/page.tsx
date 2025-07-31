"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KeyRound, Lock, UserCheck, Eye, EyeOff, CheckCircle, XCircle, Check } from "lucide-react";
import clsx from "clsx";
import {
	AlertDialog,
	AlertDialogContent,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Progress } from "@/components/ui/progress";

export default function ChangePassword() {
	const [currentPassword, setCurrentPassword] = useState("");
	const [newPassword, setNewPassword] = useState("");
	const [confirmPassword, setConfirmPassword] = useState("");
	const [showCurrentPassword, setShowCurrentPassword] = useState(false);
	const [showPassword, setShowPassword] = useState(false);
	const [showConfirmPassword, setShowConfirmPassword] = useState(false);
	const [openDialog, setOpenDialog] = useState(false);
	const [progress, setProgress] = useState(0);

	const validatePassword = (pwd: string) => ({
		length: pwd.length >= 8,
		uppercase: /[A-Z]/.test(pwd),
		lowercase: /[a-z]/.test(pwd),
		number: /\d/.test(pwd),
		special: /[^A-Za-z0-9]/.test(pwd),
	});

	const requirements = validatePassword(newPassword);
	const passwordsMatch = newPassword === confirmPassword && confirmPassword.length > 0;
	const isValid = Object.values(requirements).every(Boolean) && passwordsMatch;

	async function handleSubmit(e: React.FormEvent) {
		e.preventDefault();
		if (!isValid) return alert("Lütfen tüm kurallara uygun şifre giriniz.");
		try {
			const res = await fetch("/api/auth/change-password", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					currentPassword,
					newPassword,
					confirmPassword,
				}),
			});

			if (!res.ok) {
				const data = await res.json();
				alert(data.message || "Şifre güncellenemedi.");
				return;
			}

			setOpenDialog(true);
		} catch (err) {
			alert("Sunucu hatası: " + err);
		}
	}

	useEffect(() => {
		if (openDialog) {
			setProgress(100); // Start from 100%
			const interval = setInterval(() => {
				setProgress((prev) => {
					if (prev <= 0) {
						clearInterval(interval);
						setOpenDialog(false);
						return 0;
					}
					return prev - 1.25;
				});
			}, 25); // 100ms × 20 = 2 seconds
			return () => clearInterval(interval);
		}
	}, [openDialog]);

	return (
		<div className="w-full h-screen flex items-center justify-center bg-gray-300">
			<Card className="w-full max-w-sm">
				<CardHeader className="text-center">
					<div className="flex justify-center mb-2">
						<div className="bg-blue-600 p-3 rounded-full">
							<UserCheck size={32} color="white" />
						</div>
					</div>
					<CardTitle className="text-slate-800/90 text-2xl font-semibold">Yeni Şifre Belirle</CardTitle>
					<CardDescription>Güçlü bir şifre belirleyerek hesabınızı güvene alın.</CardDescription>
				</CardHeader>

				<form onSubmit={handleSubmit}>
					<CardContent className="flex flex-col gap-6">
						<div className="grid gap-2">
							<Label htmlFor="currentPassword">Mevcut Şifre</Label>
							<div className="relative">
								<Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="currentPassword"
									type={showCurrentPassword ? "text" : "password"}
									required
									className="pl-10 pr-10"
									value={currentPassword}
									onChange={(e) => setCurrentPassword(e.target.value)}
								/>
								<div
									className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-muted-foreground"
									onMouseDown={() => setShowCurrentPassword(true)}
									onMouseUp={() => setShowCurrentPassword(false)}
									onMouseLeave={() => setShowCurrentPassword(false)}
									onTouchStart={() => setShowCurrentPassword(true)}
									onTouchEnd={() => setShowCurrentPassword(false)}
								>
									{showCurrentPassword ? <EyeOff size={16} /> : <Eye size={16} />}
								</div>
							</div>
						</div>
						{/* Yeni Şifre */}
						<div className="grid gap-2">
							<Label htmlFor="password">Yeni Şifre</Label>
							<div className="relative">
								<Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="password"
									type={showPassword ? "text" : "password"}
									required
									className="pl-10 pr-10"
									value={newPassword}
									onChange={(e) => setNewPassword(e.target.value)}
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
						</div>

						{/* Kurallar */}
						<div className="space-y-1 text-sm">
							{[
								{ label: "En az 8 karakter", valid: requirements.length },
								{ label: "En az 1 büyük harf", valid: requirements.uppercase },
								{ label: "En az 1 küçük harf", valid: requirements.lowercase },
								{ label: "En az 1 rakam", valid: requirements.number },
								{ label: "En az 1 özel karakter", valid: requirements.special },
							].map(({ label, valid }, i) => (
								<div key={i} className="flex items-center gap-2">
									{valid ? (
										<CheckCircle size={14} className="text-green-600" />
									) : (
										<XCircle size={14} className="text-red-600" />
									)}
									<span className={valid ? "text-green-600" : "text-red-600"}>{label}</span>
								</div>
							))}
						</div>

						{/* Şifreyi Onayla */}
						<div className="grid gap-2">
							<Label htmlFor="confirmPassword">Şifreyi Onayla</Label>
							<div className="relative">
								<Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
								<Input
									id="confirmPassword"
									type={showConfirmPassword ? "text" : "password"}
									required
									value={confirmPassword}
									onChange={(e) => setConfirmPassword(e.target.value)}
									className={clsx("pl-10 pr-10", {
										"border-green-500": confirmPassword.length > 0 && passwordsMatch,
										"border-red-500": confirmPassword.length > 0 && !passwordsMatch,
									})}
								/>
								<div className="absolute right-8 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none">
									{confirmPassword.length > 0 &&
										(passwordsMatch ? (
											<CheckCircle size={16} className="text-green-600" />
										) : (
											<XCircle size={16} className="text-red-600" />
										))}
								</div>
								<div
									className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-muted-foreground"
									onMouseDown={() => setShowConfirmPassword(true)}
									onMouseUp={() => setShowConfirmPassword(false)}
									onMouseLeave={() => setShowConfirmPassword(false)}
									onTouchStart={() => setShowConfirmPassword(true)}
									onTouchEnd={() => setShowConfirmPassword(false)}
								>
									{showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
								</div>
							</div>
						</div>

						<Button type="submit" className="w-full bg-blue-600 cursor-pointer" disabled={!isValid}>
							<p>Şifremi Güncelle</p>
							<KeyRound />
						</Button>
					</CardContent>
				</form>
			</Card>

			<AlertDialog open={openDialog} onOpenChange={setOpenDialog}>
				<AlertDialogContent className="!max-w-xs">
					<AlertDialogHeader className="flex flex-col items-center">
						<div className="flex justify-center mb-2">
							<div className="bg-green-600 p-2 rounded-full">
								<Check size={32} color="white" strokeWidth={3} />
							</div>
						</div>
						<AlertDialogTitle>Başarılı!</AlertDialogTitle>
					</AlertDialogHeader>
					<AlertDialogDescription className="text-center">
						Şifreniz başarıyla güncellendi. Ana sayfaya yönderiliyorsunuz.
					</AlertDialogDescription>
					<AlertDialogFooter>
						<Progress value={progress} className="bg-indigo-700/20 [&>*]:bg-slate-800/90" />
					</AlertDialogFooter>
				</AlertDialogContent>
			</AlertDialog>
		</div>
	);
}
