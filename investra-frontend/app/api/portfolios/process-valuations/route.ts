import { NextResponse } from "next/server";
import { cookies } from "next/headers";


const BACKEND_API_URL = process.env.NEXT_PUBLIC_API_URL;

const END_OF_DAY_BASE = `${BACKEND_API_URL}/end-of-day`;

export async function POST() {
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;
    
    if (!token) {
        return NextResponse.json({ message: "Yetkisiz erişim. Lütfen giriş yapın." }, { status: 401 });
    }

    try {
        const valuationResponse = await fetch(`${END_OF_DAY_BASE}/advance-full-day`, {
            method: 'POST',
            headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`,
			},
        });

        if (!valuationResponse.ok) {
            const errorData = await valuationResponse.json().catch(() => ({ message: "Değerleme işlemi sırasında backend'de bir hata oluştu." }));
            throw new Error(errorData.message);
        }

        const resultsResponse = await fetch(`${END_OF_DAY_BASE}/client-valuations`, {
            method: 'GET',
            headers: {
				"Content-Type": "application/json",
				Authorization: `Bearer ${token}`,
			},
        });

        if (!resultsResponse.ok) {
            const errorData = await resultsResponse.json().catch(() => ({ message: "Hesaplanan değerleme sonuçları çekilirken bir hata oluştu." }));
            throw new Error(errorData.message);
        }
        
        cookieStore.set("token", "", { 
            httpOnly: true, 
            path: "/", 
            maxAge: -1 
        });

        return NextResponse.json({ 
            success: true,
            message: "Gün sonu değerlemesi başarıyla tamamlandı. Çıkış yapılıyor..." 
        }, { status: 200 });


    } catch (error: any) {
        console.error("API rotasında hata yakalandı:", error.message);
        return NextResponse.json({ message: error.message || "Sunucu tarafında beklenmedik bir hata oluştu." }, { status: 500 });
    }
}