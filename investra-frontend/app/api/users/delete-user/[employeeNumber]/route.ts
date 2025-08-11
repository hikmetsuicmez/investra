import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function PATCH(req: NextRequest, context: { params: Promise<{ employeeNumber: string }> }) {
	const params = await context.params
    const { employeeNumber } = params;
	const cookieStore = await cookies();
	const token = cookieStore.get("token")?.value;

	if (!token) {
		return NextResponse.json({ success: false, message: "Yetkisiz erişim" }, { status: 401 });
	}

	try {
		const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users/delete-user/${employeeNumber}`, {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`,
			},
		});

		if (response.ok) {
			return NextResponse.json({success: true, message: "Silme işlemi başarılı"}, { status: 200 }); // No Content
		}

		const result = await response.json();
		
		return NextResponse.json(
			{ success: false, message: result.message || "Silme işlemi başarısız" },
			{ status: result.statusCode }
		);
	} catch (error) {
		console.error("PATCH delete error:", error);
		return NextResponse.json({ success: false, message: "Sunucu hatası" }, { status: 500 });
	}
}
