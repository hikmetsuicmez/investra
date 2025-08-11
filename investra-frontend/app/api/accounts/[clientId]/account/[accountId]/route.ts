import { cookies } from "next/headers";

export async function GET(req: Request, context: { params: Promise<{ clientId: string, accountId: string }> }) {
  try {
    const params = await context.params;
    const { clientId, accountId } = params;

    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    if (!token) {
      return new Response(JSON.stringify({ message: "Yetkisiz" }), { status: 401 });
    }

    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/accounts/${accountId}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
      },
    });

    if (!res.ok) {
      const errorBody = await res.json();
      return new Response(JSON.stringify(errorBody), { status: res.status });
    }

    const accountData = await res.json();
    return new Response(JSON.stringify(accountData), { status: 200 });
  } catch (error) {
    console.error("API error:", error);
    return new Response(JSON.stringify({ message: "Sunucu hatası" }), { status: 500 });
  }
}
