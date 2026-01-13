import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import bellSvg from "../assets/bell.svg";
import heartSvg from "../assets/heart.svg";
import acornSvg from "../assets/acorn.svg";
import leafSvg from "../assets/leaf.svg";
import { usePlayer } from "../context/PlayerContext.jsx";
import CardTableDecoration from "../pageComponents/CardTableDecoration.jsx";
import InputField from "../pageComponents/InputField.jsx";

function LoginPage() {
  const { setPlayer, token, setToken } = usePlayer();
  const [playerName, setPlayerName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (token) localStorage.setItem("jwtToken", token);
  }, [token]);

  async function postLogin() {
    try {
      const response = await fetch(`/api/user/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ playerName, password }),
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      const { jwt } = await response.json();
      localStorage.setItem("jwtToken", jwt);
      setToken(jwt);

      const playerRes = await fetch(`/api/user/me`, {
        headers: { Authorization: "Bearer " + jwt },
      });
      if (!playerRes.ok) throw new Error("Could not fetch user info");
      const playerData = await playerRes.json();
      setPlayer(playerData);

      setIsLoggedIn(true);
      setError(null);
    } catch (error) {
      setError(error.message);
    }
  }

  function switchToRegister() {
    navigate(`/register`);
  }

  function handleLogin(e) {
    e.preventDefault();
    postLogin();
  }

  useEffect(() => {
    if (isLoggedIn) {
      navigate("/menu");
    }
  }, [isLoggedIn, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          
          <CardTableDecoration />

          <h2 className="text-4xl font-extrabold  mb-11 drop-shadow-lg text-center text-white">
            Welcome to 21 Card Game!
          </h2>
          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">
                Sign in to your account
              </h1>
              <form className="space-y-4 md:space-y-6" onSubmit={handleLogin}>
                <InputField
                  htmlFor="username"
                  labelText="Your username"
                  inputType="text"
                  inputName="username"
                  inputId="username"
                  placeholderText="username"
                  inputValue={playerName}
                  onInputValue={setPlayerName}
                />
                <InputField
                  htmlFor="pwd"
                  labelText="Password"
                  inputType="password"
                  inputName="pwd"
                  inputId="pwd"
                  placeholderText="••••••••"
                  inputValue={password}
                  onInputValue={setPassword}
                  autoComplete="off"
                />
                
                {error && (
                  <p className="text-sm font-light text-red-500 dark:text-red-400">
                    {error}
                  </p>
                )}
                <div className="flex items-center justify-between">
                  <div className="flex items-start">
                    <div className="flex items-center h-5">
                      <input
                        id="remember"
                        aria-describedby="remember"
                        type="checkbox"
                        className="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-primary-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-primary-600 dark:ring-offset-gray-800"
                      />
                    </div>
                    <div className="ml-3 text-sm">
                      <label
                        htmlFor="remember"
                        className="text-gray-500 dark:text-gray-300"
                      >
                        Remember me
                      </label>
                    </div>
                  </div>
                  <a
                    href="#"
                    className="text-sm font-light text-gray-500 dark:text-white"
                  >
                    Forgot password?
                  </a>
                </div>
                <button
                  type="submit"
                  className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 cursor-pointer font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Sign in
                </button>
              </form>
              <div className="flex flex-col items-center justify-center gap-2 mt-4">
                <p className="text-sm font-light text-gray-500 dark:text-white">
                  Don’t have an account yet?{" "}
                </p>
                <button
                  onClick={switchToRegister}
                  className="w-full text-white bg-[hsl(221,45%,40%)] hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 cursor-pointer font-medium rounded-lg text-sm px-5 py-2.5 text-center"
                >
                  Sign up
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
