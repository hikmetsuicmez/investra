import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  try {
    const body = await request.json();
    console.log(body)
    const { searchTerm, searchType, isActive } = body;
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/clients/client`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        searchTerm,
        searchType,
        isActive,
      }),
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
        message: result.message || "Müşteri başarıyla bulundu",
        client: result.data || null,
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

export async function GET() {
  return NextResponse.json(
    { message: "GET method not allowed. Please POST to this endpoint." },
    { status: 405 }
  );
}
