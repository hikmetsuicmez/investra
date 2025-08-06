import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function PATCH(req: NextRequest) {
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    if (!token) {
        return NextResponse.json({ success: false, message: "Yetkisiz erişim" }, { status: 401 });
    }

    const body = await req.json()

    try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/clients/delete-client`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(body)
        });

        if (response.status === 200) {
            return new NextResponse(null, { status: 200 });
        }

        const result = await response.json();

        return NextResponse.json(
            { success: false, message: result.message || "Silme işlemi başarısız" },
            { status: response.status }
        );
    } catch (error) {
        console.error("PATCH delete error:", error);
        return NextResponse.json({ success: false, message: "Sunucu hatası" }, { status: 500 });
    }
}
