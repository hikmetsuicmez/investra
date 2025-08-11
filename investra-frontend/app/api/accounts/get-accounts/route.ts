import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const clientId = request.nextUrl.searchParams.get("clientId");

  if (!clientId) {
    return NextResponse.json(
      { success: false, message: "clientId parametresi eksik" },
      { status: 400 }
    );
  }

  try {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/accounts/client/${clientId}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    const result = await res.json();
    if (result.statusCode !== 200) {
      return NextResponse.json(
        { success: false, message: result.message || "Hesaplar alınamadı" },
        { status: result.statusCode }
      );
    }

    return NextResponse.json(
      {
        success: true,
        message: result.message,
        accounts: result.data || [],
      },
      { status: 200 }
    );
  } catch (error) {
    console.error("Hesaplar çekilirken hata:", error);
    return NextResponse.json(
      { success: false, message: "Sunucu hatası" },
      { status: 500 }
    );
  }
}
