import { NextRequest, NextResponse } from "next/server";

export function middleware(req: NextRequest) {
    const hasToken = req.cookies.has("token")

    const isAuthPage = req.nextUrl.pathname.startsWith("/auth")

    if (!hasToken && !isAuthPage) {
        return NextResponse.redirect(new URL("/auth/login", req.url))
    }

    if (hasToken && !req.nextUrl.pathname.startsWith("/auth/change-password") && isAuthPage) {
        return NextResponse.redirect(new URL("/dashboard", req.url))
    }

    return NextResponse.next()
}

export const config = {
  matcher: ['/dashboard/:path*', '/auth/:path*'],
};