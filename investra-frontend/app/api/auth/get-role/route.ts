import { cookies } from 'next/headers';
import { NextRequest } from 'next/server';

export async function GET(request: NextRequest) {
  const cookieStore = await cookies();
  const role = cookieStore.get('role')?.value;

  return Response.json({role: role});
}
