import {useState, useEffect, useRef} from "react";
import {useAuth} from "../context/AuthContext";

export function UserMenu() {
    const {user, logout} = useAuth();
    const [open, setOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    // Close dropdown when clicking outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setOpen(false);
            }
        }

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [menuRef]);

    // Fallback initials if no avatar image
    const initials = user?.username
        ? user.username.slice(0, 2).toUpperCase()
        : "??";

    return (
        <div className="relative inline-block text-left" ref={menuRef}>
            {/* Avatar button */}
            <button
                onClick={() => setOpen(!open)}
                className="flex items-center justify-center w-10 h-10 rounded-full bg-gray-300 text-gray-700 font-bold focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
                {initials}
            </button>

            {/* Dropdown */}
            {open && (
                <div
                    className="absolute right-0 mt-2 w-40 bg-white border border-gray-200 rounded-md shadow-lg z-50"
                >
                    <div className="py-1">
                        <button
                            onClick={() => {
                                logout();
                                setOpen(false);
                            }}
                            className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        >
                            Logout
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
