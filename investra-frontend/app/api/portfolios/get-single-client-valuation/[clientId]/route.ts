import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_API_URL = process.env.BACKEND_API_URL || process.env.NEXT_PUBLIC_API_URL;
// DİKKAT: Java backend'inize bağlanırken /api/v1 yolunu eklemeyi unutmayın!
const END_OF_DAY_BASE = `${BACKEND_API_URL}/end-of-day`; 

export async function GET(request: Request) {
  // URL'den clientId'yi manuel olarak alıyoruz
  const url = new URL(request.url);
  const pathSegments = url.pathname.split('/');
  const clientId = pathSegments[pathSegments.length - 1]; // URL'in son parçası clientId'dir

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    return NextResponse.json({ message: "Yetkisiz erişim." }, { status: 401 });
  }
  if (!clientId || isNaN(parseInt(clientId, 10))) {
    return NextResponse.json({ message: "Geçerli bir Müşteri ID'si gereklidir." }, { status: 400 });
  }

  try {
    const fetchUrl = `${END_OF_DAY_BASE}/client-valuation/${clientId}`;
    console.log(`Tek müşteri aranıyor (Alternatif Metot): GET ${fetchUrl}`);

    const response = await fetch(fetchUrl, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
      cache: 'no-store',
    });

    const backendResponseData = await response.json();

    if (!response.ok) {
      throw new Error(backendResponseData.message || `Müşteri ${clientId} için değerleme alınamadı.`);
    }
    
    return NextResponse.json({ valuation: backendResponseData.data }, { status: 200 });

  } catch (error) {
    return NextResponse.json({ message: error }, { status: 500 });
  }
}