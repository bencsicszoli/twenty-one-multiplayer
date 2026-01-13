import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import bellSvg from '../assets/bell.svg';
import heartSvg from '../assets/heart.svg';
import acornSvg from '../assets/acorn.svg';
import leafSvg from '../assets/leaf.svg';
import CardTableDecoration from '../pageComponents/CardTableDecoration';
import InputField from '../pageComponents/InputField';

function RegistrationPage() {
  const [playerName, setPlayerName] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [regError, setRegError] = useState(null);
  const navigate = useNavigate();

  async function postRegistration() {
    try {
      const response = await fetch(`/api/user/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ playerName, password, email }),
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      navigate(`/`);
    } catch (e) {
      setRegError(e.message);
    }
  }

  function handleRegistration(e) {
    e.preventDefault();
    postRegistration();
  }

  function switchToLogin() {
    navigate(`/`);
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          
          <CardTableDecoration />

          <h2 className="text-4xl font-extrabold mb-11 drop-shadow-lg text-center text-white">
            Welcome to 21 The Card Game!
          </h2>

          <div className="z-0 w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">
                Create an account
              </h1>
              <form className="space-y-4 md:space-y-6" onSubmit={handleRegistration}>
                <InputField
                  htmlFor="usrnm"
                  labelText="Your username"
                  inputType="text"
                  inputName="username"
                  inputId="usrnm"
                  placeholderText="username"
                  inputValue={playerName}
                  onInputValue={setPlayerName}
                />
                <InputField
                  htmlFor="email"
                  labelText="Your email"
                  inputType="email"
                  inputName="email"
                  inputId="email"
                  placeholderText="name@company.com"
                  inputValue={email}
                  onInputValue={setEmail}
                />
                <InputField
                  htmlFor="password"
                  labelText="Password"
                  inputType="password"
                  inputName="password"
                  inputId="password"
                  placeholderText="••••••••"
                  inputValue={password}
                  onInputValue={setPassword}
                />
                
                {regError && <p className="text-sm font-light text-red-500 dark:text-red-400">{regError}</p>}
                <button
                  type="submit"
                  className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Create an account
                </button>
                <div className='flex gap-10'>
                  
                  <p className=" text-gray-500 dark:text-gray-300">
                  Already have an account?</p>
                  <button
                    onClick={switchToLogin}
                    className="font-medium text-blue-600 hover:underline dark:text-blue-400"
                  >
                    Login here
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

export default RegistrationPage;