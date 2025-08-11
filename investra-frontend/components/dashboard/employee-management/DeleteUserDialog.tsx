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
import { DeleteUserDialogProps } from "@/types/employees";
import { useState } from "react";
import { toast } from "sonner";

export default function DeleteUserDialog({ user, onDeleted }: DeleteUserDialogProps) {
	const [isDeleting, setIsDeleting] = useState(false);

	const handleDelete = async () => {
		setIsDeleting(true);

		try {
			const res = await fetch(`/api/users/delete-user/${user.employeeNumber}`, {
				method: "PATCH",
			});

			if (res.status === 200) {
				toast.success("Kullanıcı başarıyla silindi");
				onDeleted?.();
			} else {
				const data = await res.json();
				toast.error("Silme hatası: " + data.message);
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
				<Button variant="ghost" size="icon" disabled={isDeleting}>
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
