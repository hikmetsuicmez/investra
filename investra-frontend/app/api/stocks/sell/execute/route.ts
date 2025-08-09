import { cookies } from "next/headers";

export async function POST(req: Request) {
    const cookieStore = await cookies(); 
    const { order } = await req.json();
    const token = cookieStore.get("token")?.value;

    if (!token) {
        return new Response(JSON.stringify({ message: "Yetkisiz" }), { status: 401 });
    }

    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/stocks/sell/execute`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify( {
            accountId: order.accountId,
            clientId: order.clientId,
            quantity: order.quantity,
            executionType: order.executionType,
            stockId: order.stockId,
            price: order.price,
            previewId: order.previewId
        } )
    });

    const result = await res.json();

    if (result.statusCode != 200 || result.status != 200) {
        return new Response(JSON.stringify(result), { status: result.statusCode || result.status });
    }

    return new Response(JSON.stringify(result), { status: 200 });
}