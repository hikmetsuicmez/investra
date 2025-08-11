import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET(req: NextRequest, context: { params: Promise<{ clientId: string }> }) {
    const params = await context.params;
    const { clientId } = params;

  if (!clientId) {
    return NextResponse.json({ error: 'Missing clientId' }, { status: 400 });
  }

  // Get token from cookies
  const cookieStore = await cookies();
  const token = cookieStore.get('token')?.value;

  if (!token) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const fileRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/portfolio-reports/${clientId}/export/excel`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!fileRes.ok) {
    return NextResponse.json({ error: 'Failed to fetch file' }, { status: fileRes.status });
  }

  const fileBuffer = Buffer.from(await fileRes.arrayBuffer());

  const contentDisposition = fileRes.headers.get('content-disposition') || `attachment; filename="report-${clientId}.xlsx"`;

  return new NextResponse(fileBuffer, {
    headers: {
      'Content-Type':
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'Content-Disposition': contentDisposition,
    },
  });
}
