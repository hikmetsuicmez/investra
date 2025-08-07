import { cookies } from "next/headers";

export async function POST(req: Request) {
	try {
		const cookieStore = await cookies();
		const token = cookieStore.get("token")?.value;

		if (!token) {
			return new Response(JSON.stringify({ message: "Yetkisiz" }), { status: 401 });
		}

		const deposit = await req.json();

		const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/accounts/deposit`, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`,
			},
			body: JSON.stringify(deposit),
		});

		const result = await res.json();

		if (result.statusCode !== 201 && result.status !== 201) {
			return new Response(JSON.stringify(result), {
				status: result.statusCode || result.status || 400,
			});
		}

		return new Response(JSON.stringify({ message: "Bakiye hesaba başarıyla kaydedildi." }), { status: 201 });
	} catch (error) {
		console.error("API error:", error);
		return new Response(JSON.stringify({ message: "Sunucu hatası" }), { status: 500 });
	}
}
