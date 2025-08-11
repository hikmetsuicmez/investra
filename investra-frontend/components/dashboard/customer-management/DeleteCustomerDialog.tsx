"use client";

import {
	AlertDialog,
	AlertDialogTrigger,
	AlertDialogContent,
	AlertDialogHeader,
	AlertDialogTitle,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogCancel,
	AlertDialogAction,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";
import { useState } from "react";
import { DeleteCustomerDialogProps } from "@/types/customers";

export default function DeleteCustomerDialog({ customer, onDeleted, disabled }: DeleteCustomerDialogProps) {
	const [isDeleting, setIsDeleting] = useState(false);

	const handleDelete = async () => {
		setIsDeleting(true);

		const searchTerm = customer.clientType == "INDIVIDUAL" ? customer.nationalityNumber : customer.taxNumber;
		const searchType = customer.clientType == "INDIVIDUAL" ? "TCKN" : "VERGI_NO";

		try {
			const res = await fetch(`/api/clients/delete-client`, {
				method: "PATCH",
				body: JSON.stringify({
					searchTerm: searchTerm,
					searchType: searchType,
					isActive: customer.isActive,
				}),
			});

			if (res.status === 204) {
				console.log("Kullanıcı başarıyla silindi");
				onDeleted?.();
			} else {
				const data = await res.json();
				console.error("Silme hatası:", data.message);
				// optionally show toast or alert here
			}
		} catch (error) {
			console.error("API hatası:", error);
		} finally {
			setIsDeleting(false);
		}
	};

	return (
		<AlertDialog>
			<AlertDialogTrigger asChild>
				<Button variant="ghost" size="icon" disabled={isDeleting || disabled}>
					<Trash2 size={18} color="red" />
				</Button>
			</AlertDialogTrigger>
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle>Personeli Sil</AlertDialogTitle>
					<AlertDialogDescription>
						Bu personeli silmek istediğinize emin misiniz? Bu işlem geri alınamaz.
					</AlertDialogDescription>
				</AlertDialogHeader>
				<AlertDialogFooter>
					<AlertDialogCancel disabled={isDeleting}>İptal</AlertDialogCancel>
					<AlertDialogAction onClick={handleDelete} disabled={isDeleting}>
						{isDeleting ? "Siliniyor..." : "Sil"}
					</AlertDialogAction>
				</AlertDialogFooter>
			</AlertDialogContent>
		</AlertDialog>
	);
}
