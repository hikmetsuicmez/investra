import { cookies } from "next/headers";

export async function POST(req: Request) {
    const cookieStore = await cookies(); 
    const { employee } = await req.json();
    const token = cookieStore.get("token")?.value;

    if (!token) {
        return new Response(JSON.stringify({ message: "Yetkisiz" }), { status: 401 });
    }

    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users/create-user`, {
    method: "POST",
    headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
        firstName: employee.name,
        lastName: employee.surname,
        nationalityNumber: employee.nationalId,
        sicilNo: employee.registrationNumber,
        phoneNumber: employee.phone,
        email: employee.email,
        role: employee.role,
        firstLogin: true,
    })
    });

    const result = await res.json();

    if (result.statusCode != 201 || result.status != 201) {
        return new Response(JSON.stringify(result), { status: result.statusCode || result.status });
    }

    return new Response(JSON.stringify({ message: "Personel kaydı başarılı." }), { status: 200 });
}