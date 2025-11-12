import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import bellSvg from '../assets/bell.svg';
import heartSvg from '../assets/heart.svg';
import acornSvg from '../assets/acorn.svg';
import leafSvg from '../assets/leaf.svg';
import { usePlayer } from '../context/PlayerContext.jsx';

function LoginPage() {
  const { setPlayer, token, setToken } = usePlayer();
  const [playerName, setPlayerName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (token) localStorage.setItem('jwtToken', token);
  }, [token]);

  async function postLogin() {
    try {
        const response = await fetch(`/api/user/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ playerName, password }),
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      const { jwt } = await response.json();
      localStorage.setItem('jwtToken', jwt);
      setToken(jwt);

      const playerRes = await fetch(`/api/user/me`, {
        headers: { Authorization: 'Bearer ' + jwt },
      });
      if (!playerRes.ok) throw new Error('Could not fetch user info');
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
      navigate('/menu');
    }
  }, [isLoggedIn, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-[80rem]">
        <div className="w-full h-[42rem] bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          <img
            src={bellSvg}
            alt="Bell"
            className="absolute top-6 right-8 w-20 md:w-28 opacity-80 rotate-12 pointer-events-none"
          />
          <img
            src={heartSvg}
            alt="Heart"
            className="absolute bottom-8 left-8 w-20 md:w-28 opacity-80 -rotate-12 pointer-events-none"
          />
          <img
            src={acornSvg}
            alt="Acorn"
            className="absolute bottom-8 right-8 w-20 md:w-28 opacity-80 rotate-6 pointer-events-none"
          />
          <img
            src={leafSvg}
            alt="Leaf"
            className="absolute top-6 left-8 w-20 md:w-28 opacity-80 -rotate-6 pointer-events-none"
          />

          <h2 className="text-4xl font-extrabold  mb-11 drop-shadow-lg text-center text-white">
            Welcome to 21 The Card Game!
          </h2>
          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">
                Sign in to your account
              </h1>
              <form className="space-y-4 md:space-y-6" onSubmit={handleLogin}>
                <div>
                  <label htmlFor="username" className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">
                    Your username
                  </label>
                  <input
                    type="text"
                    name="username"
                    id="username"
                    className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="username"
                    value={playerName}
                    onChange={(e) => setPlayerName(e.target.value)}
                  />
                </div>
                <div>
                  <label htmlFor="password" className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">
                    Password
                  </label>
                  <input
                    type="password"
                    name="pwd"
                    id="password"
                    className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    placeholder="••••••••"
                    value={password}
                    autoComplete='off'
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                {error && <p className="text-sm font-light text-red-500 dark:text-red-400">{error}</p>}
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
                      <label htmlFor="remember" className="text-gray-500 dark:text-gray-300">
                        Remember me
                      </label>
                    </div>
                  </div>
                  <a href="#" className="text-sm font-light text-gray-500 dark:text-white">
                    Forgot password?
                  </a>
                </div>
                <button
                  type="submit"
                  className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Sign in
                </button>
                <p className="text-sm font-light text-gray-500 dark:text-white">Don’t have an account yet? </p>
              </form>
              <button onClick={switchToRegister} className="text-sm font-light text-gray-500 dark:text-white">
                Sign up
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;