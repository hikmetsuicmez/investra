import { cookies } from "next/headers";

export async function POST(req: Request) {
  const { currentPassword, newPassword, confirmPassword } = await req.json();
  const cookieStore = await cookies()
  const token = cookieStore.get("token")?.value;

  if (!token) {
    return new Response(JSON.stringify({ message: "Yetkisiz" }), { status: 401 });
  }

  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/change-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
  });

  const result = await res.json();

  if (!res.ok) {
    return new Response(JSON.stringify(result), { status: res.status });
  }

  return new Response(JSON.stringify({ message: "Şifre güncellendi" }), { status: 200 });
}
