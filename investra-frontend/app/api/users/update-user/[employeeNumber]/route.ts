import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function PATCH(req: NextRequest, context: { params: Promise<{ employeeNumber: string }> }) {
	const params = await context.params
    const { employeeNumber } = params;
	const cookieStore = await cookies();
	const token = cookieStore.get("token")?.value; // or whatever your cookie name is

	if (!token) {
		return NextResponse.json({ success: false, message: "Yetkisiz erişim" }, { status: 401 });
	}

	try {
		const body = await req.json();

		const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users/update-user/${employeeNumber}`, {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`,
			},
			body: JSON.stringify(body),
		});

		const result = await response.json();

		if (!response.ok) {
			return NextResponse.json(
				{ success: false, message: result.message || "Güncelleme başarısız" },
				{ status: response.status }
			);
		}

		return NextResponse.json(
			{ success: true, message: "Kullanıcı güncellendi", data: result.data },
			{ status: 200 }
		);
	} catch (error) {
		console.error("PATCH error:", error);
		return NextResponse.json({ success: false, message: "Sunucu hatası" }, { status: 500 });
	}
}
