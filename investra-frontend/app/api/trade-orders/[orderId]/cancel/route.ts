import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest, context: { params: Promise<{ orderId: string }> }) {
    const params = await context.params
    const { orderId } = params;
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    if (!token) {
            return NextResponse.json({ success: false, message: "Yetkisiz erişim" }, { status: 401 });
    }

    try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/trade-orders/{orderId}/cancel?orderId=${orderId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        });

        const result = await response.json();

        if (result.statusCode === 200) {
            return NextResponse.json(result, {status: 200})
        }

        return NextResponse.json(
            { success: false, message: result.message || "Müşteri hesabı çekme işlemi başarısız" },
            { status: response.status }
        );
    } catch (error) {
        console.error("GET error:", error);
        return NextResponse.json({ success: false, message: "Sunucu hatası" }, { status: 500 });
    }
}