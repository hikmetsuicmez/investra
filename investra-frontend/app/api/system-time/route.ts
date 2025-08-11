import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_API_URL = process.env.NEXT_PUBLIC_API_URL;
const END_OF_DAY_BASE = `${BACKEND_API_URL}/end-of-day`;

export async function GET() {
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    if (!token) {
        return NextResponse.json({ message: "Yetkisiz erişim. Lütfen giriş yapın." }, { status: 401 });
    }

    try {
        const urlToFetch = `${END_OF_DAY_BASE}/simulation-status`;
        
        console.log(`Backend'e istek atılıyor: ${urlToFetch}`);

        const response = await fetch(urlToFetch, {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`,
            },
            cache: 'no-store', 
        });

        const backendResponse = await response.json();

        if (!response.ok || !backendResponse.data) {
            console.error("Backend sunucusundan hatalı yanıt alındı:", backendResponse);
            throw new Error(backendResponse.message || `Simülasyon durumu alınamadı. Sunucu yanıtı: ${response.status}`);
        }
        
        const simulationDate = backendResponse.data.currentSimulationDate;
        
        console.log("Simülasyon tarihi başarıyla çekildi:", simulationDate);

        return NextResponse.json({ date: simulationDate }, { status: 200 });

    } catch (error) {
        console.error("get-current-date rotasında bir hata yakalandı:", error);
        return NextResponse.json({ message: error }, { status: 500 });
    }
}