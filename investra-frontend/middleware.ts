import { NextRequest, NextResponse } from "next/server";

export function middleware(req: NextRequest) {
    const hasToken = req.cookies.has("token");
    const role = req.cookies.get("role")?.value;

    const isAuthPage = req.nextUrl.pathname.startsWith("/auth");
    const isEmployeeManagement = req.nextUrl.pathname.startsWith("/dashboard/employee-management");

    if (!hasToken && !isAuthPage) {
        return NextResponse.redirect(new URL("/auth/login", req.url));
    }

    if (hasToken) {
        if (isEmployeeManagement && role !== "ADMIN") {
            // Redirect non-admin users away from employee management page
            return NextResponse.redirect(new URL("/dashboard", req.url));
        }
    }

    if (hasToken && !req.nextUrl.pathname.startsWith("/auth/change-password") && isAuthPage) {
        return NextResponse.redirect(new URL("/dashboard", req.url));
    }

    return NextResponse.next();
}

export const config = {
  matcher: ['/dashboard/:path*', '/auth/:path*'],
};
