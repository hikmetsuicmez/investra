import Link from "next/link";

export default function Home() {
	return (
		<div className="flex flex-col gap-4 ">
			<Link href={"/login"}>Login</Link>
			<Link href={"/forgot-password"}>Forgot password</Link>
			<Link href={"/change-password"}>Change password</Link>
		</div>
	);
}
