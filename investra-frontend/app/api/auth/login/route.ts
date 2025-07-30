import { cookies } from 'next/headers';

export async function POST(req: Request) {
  const { email, password } = await req.json();
  const cookieStore = await cookies()


  const loginRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  
  const body = await loginRes.json();

  console.log(body)

  if (body.statusCode != 200) {
    return new Response('Invalid credentials', { status: 401 });
  }

  cookieStore.set('token', body.data.token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    path: '/',
    maxAge: 60 * 60,
  });

  return new Response('Logged in', { status: 200 });
}