import { useNavigate, Link } from "react-router-dom";
import { useEffect } from "react";
import bellSvg from "./assets/bell.svg";
import heartSvg from "./assets/heart.svg";
import acornSvg from "./assets/acorn.svg";
import leafSvg from "./assets/leaf.svg";
import { usePlayer } from "./context/PlayerContext";
import { useWebSocket } from "./context/WebSocketContext";


function MenuPage() {
  const navigate = useNavigate();
  const { player, logout } = usePlayer();
  const { connected, subscribe, send } = useWebSocket();

    // Ha nincs bejelentkezett játékos, vissza a loginra
  useEffect(() => {
    if (!player) navigate(`/`);
  }, [player, navigate]);

  // Feliratkozás a game state-re
  useEffect(() => {
    if (!connected) return;

    const subscription = subscribe("/user/queue/private", onGameStateReceived);

    return () => subscription?.unsubscribe?.();
  }, [connected, subscribe]);

  function onGameStateReceived(payload) {
    const message = JSON.parse(payload.body);
    if (message.type === "game.joined" && message.gameId) {
      navigate("/game", { state: { game: message } });
    }
  }

  function joinGame() {
    if (!connected) {
      console.warn("WebSocket not connected yet, cannot join game.");
      return;
    }

    send("/app/game.join", { playerName: player.playerName });
  }
/*
  useEffect(() => {
    if (!player) navigate(`/`);
  }, [player, navigate]);

  useEffect(() => {
    if (connected) {
      // Feliratkozás globális játék eseményekre
      subscribe("/topic/game.state", onGameStateReceived);
    }
  }, [connected]);

  function onGameStateReceived(payload) {
    const message = JSON.parse(payload.body);

    if (message.type === "game.joined" && message.gameId) {
      navigate("/game", { state: { game: message } });
    }
  }

  function joinGame() {
    send("/app/game.join", { playerName: player.playerName });
  }
*/
  const handleHelpClick = () => {
    window.open("https://hu.wikipedia.org/wiki/Huszonegyes");
  };

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
            Select an option:
          </h2>

          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <div>
                <button
                  onClick={joinGame}
                  className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-semibold rounded-lg text-lg px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  PLAY
                </button>
              </div>
              <div>
                <Link to={`/statistics`}>
                  <button className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
                    Statistics
                  </button>
                </Link>
              </div>
              <div>
                <Link to={`/editpage`}>
                  <button className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
                    Edit profile
                  </button>
                </Link>
              </div>
              <div>
                <Link to={`/`}>
                  <button className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
                    Bug report
                  </button>
                </Link>
              </div>
              <div>
                <button
                  onClick={handleHelpClick}
                  className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                >
                  Rules
                </button>
              </div>
              <div>
                <Link to={`/`}>
                  <button
                    onClick={logout}
                    className="w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
                  >
                    Logout
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

export default MenuPage;
