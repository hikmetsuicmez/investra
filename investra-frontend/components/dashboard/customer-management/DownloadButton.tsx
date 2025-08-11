import { Button } from "@/components/ui/button";

type DownloadButtonProps = {
	label: string;
	endpoint: string;
	filename: string;
};

export function DownloadButton({ label, endpoint, filename }: DownloadButtonProps) {
	const handleDownload = async () => {
		const res = await fetch(endpoint);
		if (!res.ok) throw new Error("Download failed");

		const blob = await res.blob();
		const url = window.URL.createObjectURL(blob);

		const link = document.createElement("a");
		link.href = url;
		link.download = filename;
		document.body.appendChild(link);
		link.click();
		link.remove();

		window.URL.revokeObjectURL(url);
	};

	return (
		<Button onClick={handleDownload} className="flex-grow">
			{label}
		</Button>
	);
}
