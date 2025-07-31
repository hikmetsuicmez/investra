import Link from "next/link";

export default function Home() {
	return (
		<div className="flex flex-col gap-4 ">
			<Link href={"/auth/login"}>Login</Link>
			<Link href={"/auth/forgot-password"}>Forgot password</Link>
			<Link href={"/auth/change-password"}>Change password</Link>
		</div>
	);
}
