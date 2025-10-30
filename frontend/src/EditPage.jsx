import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import bellSvg from "./assets/bell.svg";
import heartSvg from "./assets/heart.svg";
import acornSvg from "./assets/acorn.svg";
import leafSvg from "./assets/leaf.svg";
import { usePlayer } from "./context/PlayerContext";

function EditPage() {
  const API_URL = import.meta.env.VITE_API_URL;
  const navigate = useNavigate();
  const { player } = usePlayer();

  useEffect(() => {
    if (!player) {
      navigate(`/`);
    }
  }, [player, navigate, API_URL]);

  const [name, setName] = useState(player?.playerName || "");
  const [email, setEmail] = useState(player?.email || "");
  const [password, setPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");

  async function handleDelete() {
    try {
      const response = await fetch(`${API_URL}/api/user/delete`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
        },
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      setMessage("");
      console.log(`User ${name} has been deleted`);
      navigate(`/`);
    } catch (error) {
      setMessage(error.message);
      console.error("Error deleting user:", error);
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    setMessage("");
    try {
      const token = (() => {
        try {
          return localStorage.getItem("jwtToken");
        } catch {
          return null;
        }
      })();
      const res = await fetch("/api/user/me", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: "Bearer " + token } : {}),
        },
        body: JSON.stringify({
          playerName: name,
          email,
          password,
          newPassword:
            newPassword && newPassword === confirmPassword ? newPassword : "",
        }),
      });
      const data = await res.json();
      console.log(data);
      if (!res.ok) throw new Error(data.message || "Failed to update profile");
      setMessage(data.message);
      setName("");
      setTimeout(() => navigate("/"), 3000);
    } catch (err) {
      setMessage(err.message);
    }
  }

  function handleCancel() {
    navigate("/menu");
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          <img
            src={heartSvg}
            alt="Heart"
            className="absolute top-6 left-8 w-20 md:w-28 opacity-80 -rotate-6 pointer-events-none"
          />
          <img
            src={acornSvg}
            alt="Acorn"
            className="absolute top-6 right-8 w-20 md:w-28 opacity-80 rotate-12 pointer-events-none"
          />
          <img
            src={bellSvg}
            alt="Bell"
            className="absolute bottom-8 left-8 w-20 md:w-28 opacity-80 -rotate-12 pointer-events-none"
          />
          <img
            src={leafSvg}
            alt="Leaf"
            className="absolute bottom-8 right-8 w-20 md:w-28 opacity-80 rotate-6 pointer-events-none"
          />
          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">
                Edit your account:
              </h1>
              <form className="space-y-4 md:space-y-6" onSubmit={handleSave}>
                <div>
                  <label
                    htmlFor="username"
                    className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
                  >
                    Your username:
                  </label>
                  <input
                    type="text"
                    name="username"
                    id="username"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="username"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label
                    htmlFor="email"
                    className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
                  >
                    Email address:
                  </label>
                  <input
                    type="email"
                    name="email"
                    id="email"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </div>
                <div>
                  <label
                    htmlFor="password"
                    className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
                  >
                    Your password:
                  </label>
                  <input
                    type="password"
                    name="password"
                    id="password"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="........"
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="off"
                    required
                  />
                </div>
                <div>
                  <label
                    htmlFor="new-password"
                    className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
                  >
                    New password:
                  </label>
                  <input
                    type="password"
                    name="new-password"
                    id="new-password"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="••••••••"
                    onChange={(e) => setNewPassword(e.target.value)}
                    autoComplete="off"
                  />
                </div>
                <div>
                  <label
                    htmlFor="confirm-password"
                    className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
                  >
                    Confirm password:
                  </label>
                  <input
                    type="password"
                    name="confirm-password"
                    id="confirm-password"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="••••••••"
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="off"
                  />
                </div>
                <div>
                  {confirmPassword && newPassword !== confirmPassword && (
                    <div>New passwords do not match</div>
                  )}
                </div>
                {message && (
                  <p className="text-sm font-light text-red-500 dark:text-red-400">
                    {message}
                  </p>
                )}
                <button
                  type="submit"
                  className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Update credentials
                </button>
                <div>
                  <button
                    onClick={handleDelete}
                    type="button"
                    className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                  >
                    Delete account
                  </button>
                </div>
                <div>
                  <button
                    onClick={handleCancel}
                    type="button"
                    className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EditPage;
