import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_API_URL =  process.env.NEXT_PUBLIC_API_URL;
const END_OF_DAY_BASE = `${BACKEND_API_URL}/end-of-day`;

export async function GET() {
    const cookieStore = await  cookies();
    const token = cookieStore.get("token")?.value;

    if (!token) {
        return NextResponse.json({ message: "Yetkisiz erişim. Lütfen giriş yapın." }, { status: 401 });
    }

    try {
        const urlToFetch = `${END_OF_DAY_BASE}/client-valuations`;
        const response = await fetch(urlToFetch, {
            method: 'GET',
            headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`,
			},
            cache: 'no-store', 
        });


        if (!response.ok) {
            const errorText = await response.text();
            try {
                const errorData = JSON.parse(errorText);
                console.error("Backend'den gelen JSON hatası:", errorData);
                throw new Error(errorData.message || "Mevcut değerlemeler çekilirken bir hata oluştu.");
            } catch (jsonError) {
                console.error("Backend'den gelen yanıt JSON değil (muhtemelen HTML):", errorText);
                throw new Error(`Backend sunucusu ${response.status} koduyla bir hata döndürdü.`);
            }
        }
        
        const valuationsData = await response.json();
        console.log("Mevcut değerlemeler başarıyla çekildi.");

        return NextResponse.json({ valuations: valuationsData }, { status: 200 });

    } catch (error: any) {
        console.error("get-current-valuations rotasında hata yakalandı:", error.message);
        return NextResponse.json({ message: error.message }, { status: 500 });
    }
}