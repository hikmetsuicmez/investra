export async function POST(req: Request) {
  const { email } = await req.json();

  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/forgot-password?email=${email}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  });

  const result = await res.json();

  console.log(result)

  if (result.statusCode != 200) {
    return new Response(JSON.stringify(result), { status: result.statusCode });
  }

  return new Response(JSON.stringify({ message: "Şifre sıfırlama bağlantısı gönderildi." }), {status: 200,});
}