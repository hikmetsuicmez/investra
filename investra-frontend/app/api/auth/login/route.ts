import { cookies } from 'next/headers';

export async function POST(req: Request) {
  const { email, password } = await req.json();
  const cookieStore = await cookies();

  const loginRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });

  const body = await loginRes.json();

  if (body.statusCode !== 200) {
    return new Response('Invalid credentials', { status: 401 });
  }

  const token = body.data.token;
  const firstLogin = body.data.firstLogin;

  // Set JWT as HTTP-only cookie
  cookieStore.set('token', token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    path: '/',
    maxAge: 8 * 60 * 60,
  });

  // Return redirect info in JSON
  return Response.json({
    success: true,
    redirectTo: firstLogin ? '/auth/change-password' : '/dashboard',
  });
}
