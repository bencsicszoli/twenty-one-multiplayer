import { useNavigate, Link } from "react-router-dom";
import { useEffect } from "react";
import { usePlayer } from "./context/PlayerContext";
import StatisticsElement from "./pageComponents/StatisticsElement";
import CardTableDecoration from "./pageComponents/CardTableDecoration";

function StatisticsPage() {
  const navigate = useNavigate();
  const { player } = usePlayer();

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
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          <CardTableDecoration />
          <h2 className="text-3xl font-extrabold mb-11 drop-shadow-lg text-center text-white md:text-4xl">
            Account statistics
          </h2>

          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-8 sm:p-8">
              <div>
                <StatisticsElement
                  label="Player name"
                  gameData={player.playerName}
                />
                <StatisticsElement
                  label="Played games"
                  gameData={player.games}
                />
                <StatisticsElement
                  label="Won games"
                  gameData={player.wins}
                />
                <StatisticsElement
                  label="Lost games"
                  gameData={player.losses}
                />
                <StatisticsElement
                  label={`${player.playerName}'s balance`}
                  gameData={player.balance}
                />
              </div>
              <div>
                <Link to={`/menu`} state={player}>
                  <button className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 rounded-lg text-lg font-bold px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
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
