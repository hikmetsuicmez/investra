export async function POST(req: Request) {
  const { newPassword, confirmPassword, token } = await req.json();

  if (!token) {
    return new Response(JSON.stringify({ message: "Yetkisiz" }), { status: 401 });
  }

  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/reset-password?token=${token}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ password: newPassword, confirmPassword }),
  });

  const result = await res.json();

  if (!res.ok) {
    return new Response(JSON.stringify(result), { status: res.status });
  }

  return new Response(JSON.stringify({ message: "Şifre güncellendi" }), { status: 200 });
}
