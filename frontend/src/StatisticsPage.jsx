import { useNavigate, Link } from "react-router-dom";
import { useEffect } from "react";
import bellSvg from "./assets/bell.svg";
import heartSvg from "./assets/heart.svg";
import acornSvg from "./assets/acorn.svg";
import leafSvg from "./assets/leaf.svg";
import {usePlayer} from './context/PlayerContext';

function StatisticsPage() {
 // const location = useLocation();
  const navigate = useNavigate();
  //const user = location.state;
  const {player} = usePlayer();

  useEffect(() => {
    if (!player) {
      navigate(`/`);
    }
  }, [player, navigate]);

  if (!player) {
  return <p>Redirecting...</p>;
}
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-[80rem]">
        <div className="w-full h-[42rem] bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
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
          <h2 className="text-4xl font-extrabold mb-11 drop-shadow-lg text-center text-white">
            Account statistics
          </h2>

          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <div>
                <div className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"></div>
                <div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                  Name: {player.playerName}
                </div>
                <div className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"></div>
                <div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                  Played games: {player.games}
                </div>
                <div className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"></div>
                <div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                  Won games: {player.wins}
                </div>
                <div className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"></div>
                <div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                  Lost games: {player.losses}
                </div>
                <div className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"></div>
                <div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                  {player.playerName}'s balance: {player.balance}
                </div>
              </div>

              <div>
                <Link to={`/editpage`} state={player}>
                  <button className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
                    Update player
                  </button>
                </Link>
              </div>
              <div>
                <Link to={`/menu`} state={player}>
                  <button className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
                    Back to menu
                  </button>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StatisticsPage;