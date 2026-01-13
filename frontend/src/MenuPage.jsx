import { useNavigate, Link } from "react-router-dom";
import { useEffect } from "react";
import { usePlayer } from "./context/PlayerContext";
import { useWebSocket } from "./context/WebSocketContext";
import CardTableDecoration from "./pageComponents/CardTableDecoration";
import LinkButton from "./pageComponents/LinkButton";

function MenuPage() {
  const navigate = useNavigate();
  const { player, setPlayer, setToken } = usePlayer();
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

  const handleLogout = () => {
    setToken(null);
    setPlayer(null);
    navigate("/");
  }
  
  const handleHelpClick = () => {
    window.open("https://hu.wikipedia.org/wiki/Huszonegyes");
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative text-white px-6 sm:px-8">
          <CardTableDecoration />
          <h2 className="text-3xl font-extrabold mb-11 drop-shadow-lg text-center text-white md:text-4xl">
            Select an option:
          </h2>

          <div className="w-full bg-white border border-gray-200 rounded-lg shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 sm:max-w-md xl:p-0">
            <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <LinkButton buttonText="PLAY" fontStyle="font-extrabold text-xl" onHandleClick={joinGame} />
              <LinkButton whereToLink={`/statistics`} buttonText="Statistics" fontStyle="font-medium text-lg"/>
              <LinkButton whereToLink={`/editpage`} buttonText="Edit profile" fontStyle="font-medium text-lg" />
              <LinkButton whereToLink={`/`} buttonText="Bug report" fontStyle="font-medium text-lg" />
              <LinkButton buttonText="Rules" onHandleClick={handleHelpClick} fontStyle="font-medium text-lg" />
              <LinkButton whereToLink={`/`} buttonText="Logout" onHandleClick={handleLogout} fontStyle="font-medium text-lg"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MenuPage;
