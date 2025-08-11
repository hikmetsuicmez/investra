import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";
import { Stock} from "@/types/stocks";

type BackendStock = {
	stockId: number;
	stockCode: string | null;
	stockName: string;
	stockGroup: string;
	availableQuantity: number;
	currentPrice: number;
	avgPrice: number;
    category: string;
};

export async function GET(req: NextRequest, context: { params: Promise<{ clientId: string }> }) {
    const params = await context.params
    const { clientId } = params;
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    if (!token) {
            return NextResponse.json({ success: false, message: "Yetkisiz erişim" }, { status: 401 });
    }

    try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/stocks/sell/client/${clientId}/stocks`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        });

        const result = await response.json();


        const backendList: BackendStock[] = result.data;

        const stocks: Stock[] = backendList.map(item => ({
            id: item.stockId,
            name: item.stockName,
            symbol: item.stockCode,
            currentPrice: item.currentPrice,
            stockGroup: item.stockGroup,
            isActive: true, // you decide your own logic
            source: null,   // or set dynamically if applicable
            availableQuantity: item.availableQuantity,
            category: item.category
        }));

        if (result.statusCode === 200) {
            return NextResponse.json(stocks, {status: 200})
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