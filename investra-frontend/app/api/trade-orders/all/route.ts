import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function GET() {
  const cookieStore = await cookies();    
  const token = cookieStore.get("token")?.value;

    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/trade-orders/all`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        });

        const result = await res.json();

        if (result.statusCode !== 200) {
            return NextResponse.json(
                { success: false, message: result.message || "Bir hata oluştu" },
                { status: result.statusCode }
            );
        }

        return NextResponse.json(
            {
                success: true,
                message: result.message || "Emirler başarıyla alındı",
                data: result.data || [],
            },
            { status: 200 }
        );
    } catch (error) {
        console.error("API error:", error);
        return NextResponse.json(
            { success: false, message: "Sunucu hatası" },
            { status: 500 }
        );
    }
}
