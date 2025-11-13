import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { useWebSocket } from "./context/WebSocketContext";
import { usePlayer } from "./context/PlayerContext";

function GamePage() {
  const location = useLocation();
  const { game } = location.state || {};
  const { player } = usePlayer();

  const [gameState, setGameState] = useState(game || {});
  const { subscribe, send } = useWebSocket();
  /*
  const [computerBalance, setComputerBalance] = useState(
    game?.dealerBalance ? game.dealerBalance : 100
  );
  const [remainingCards, setRemainingCards] = useState(
    game?.remainingCards ? game.remainingCards : 32
  );
  const [player1Name, setPlayer1Name] = useState(
    game?.player1 ? game.player1 : "Player 1"
  );
  const [player1Balance, setPlayer1Balance] = useState(
    game?.player1Balance ? game.player1Balance : 100
  );
  
  const [player1Pot, setPlayer1Pot] = useState(
    game?.player1Pot ? game.player1Pot : 0
  );
  const [player1CardsNumber, setPlayer1CardsNumber] = useState(
    game?.player1CardNumber ? game.player1CardNumber : 0
  );
  const [player2Name, setPlayer2Name] = useState(
    game?.player2 ? game.player2 : "Player 2"
  );
  const [player2Balance, setPlayer2Balance] = useState(
    game?.player2Balance ? game.player2Balance : 100
  );
  const [player2Pot, setPlayer2Pot] = useState(
    game?.player2Pot ? game.player2Pot : 0
  );
  const [player2CardsNumber, setPlayer2CardsNumber] = useState(
    game?.player2CardNumber ? game.player2CardNumber : 0
  );
  const [player3Name, setPlayer3Name] = useState(
    game?.player3 ? game.player3 : "Player 3"
  );
  const [player3Balance, setPlayer3Balance] = useState(
    game?.player3Balance ? game.player3Balance : 100
  );
  const [player3Pot, setPlayer3Pot] = useState(
    game?.player3Pot ? game.player3Pot : 0
  );
  const [player3CardsNumber, setPlayer3CardsNumber] = useState(
    game?.player3CardNumber ? game.player3CardNumber : 0
  );
  */
  const [ownHand, setOwnHand] = useState([]);
  const [ownHandValue, setOwnHandValue] = useState(0);
  const [dealerHand, setDealerHand] = useState([]);

  useEffect(() => {
    subscribe(`/topic/game.${game.gameId}`, onGameUpdate);
    subscribe("/user/queue/private", onHandUpdate);
  }, [game]);

  function onHandUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Private topic update:", message);
    switch (message.type) {
      case "hand.update":
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        break;
        case "game.joined":
        console.log("Joined game:", message);
        setGameState(message);
        break;
      default:
        console.log("Unknown private message type:", message.type);
    }
  
    // Kezeljük a privát kéz frissítést itt
  }

  function onGameUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Game update:", message);

    switch (message.type) {
      case "game.pullCard":
        console.log("Pull card message received");
        setGameState(message);
        break;
      case "player.joined":
        console.log("Another player joined:", message);
        setGameState(message);
        break;
      default:
        console.log("Unknown message type:", message.type);
    }
  }

  function showHand(hand) {
    return hand.map((card, index) => (
      <div key={index} className="inline-block mx-1">
        <img
          src={`${card.frontImagePath}`}
          alt={card}
          className="h-16 w-auto"
        />
      </div>
    ));
  }
/*
  function showHandValue(hand) {
    return hand.reduce((sum, card) => sum + card.cardValue, 0);
  }
    */

  function showCardBacks(numberOfCards) {
    const backs = [];
    for (let i = 0; i < numberOfCards; i++) {
      backs.push(
        <div key={i} className="inline-block mx-1">
          <img src="Back.jpg" alt="card-back" className="h-16 w-auto" />
        </div>
      );
    }
    return backs;
  }

  function getFirstCard() {
    send("/app/game.firstCard", { gameId: gameState.gameId });
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-[80rem]">
        <div className="w-full h-[42rem] bg-poker-table rounded-[70px] shadow-2xl relative text-white px-6 sm:px-8">
          <div className="h-1/3 text-center">
            <p className="text-center">Computer</p>
            <p className="text-center">{gameState.dealerBalance} $</p>
            {gameState.state === "NEW" && (
              <button onClick={getFirstCard} className="h-8 w-12 border">
                DEAL!
              </button>
            )}

            <div className="h-20 w-3xs m-auto">{showHand(dealerHand)}</div>
          </div>
          <div className="h-1/3 flex items-center justify-center">
            <div className="flex">
              <div className="flex flex-col border">
                <button className="border">Pass</button>
                <button className="border">Raise bet</button>
                <button className="border">Enough</button>
              </div>
              <div>
                <p className="text-center">{gameState.player3}'s balance:</p>
                <p className="text-center">{gameState.player3} $</p>
                <div>
                  <div className="h-20 w-3xs">{}</div>
                  <p className="text-center">{gameState.player3}</p>
                  <p className="text-center">Sum: {}</p>
                </div>
              </div>
              <div>
                <p className="text-center">{gameState.player3}'s bet:</p>
                <p className="text-center">{gameState.player3Pot}</p>
              </div>
            </div>
            <div>
              <div>
                <p className="text-center">Remaining cards:</p>
                <div className="h-20 w-10 m-auto">
                  <img src="Back.jpg" alt="card-back" />
                </div>
                <p className="text-center">{gameState.remainingCards}</p>
              </div>
            </div>
            <div className="flex">
              <div>
                <p className="text-center">{gameState.player1}'s bet:</p>
                <p className="text-center">{gameState.player1Pot}</p>
              </div>
              <div>
                <p className="text-center">{gameState.player1}'s balance:</p>
                <p className="text-center">{gameState.player1Balance} $</p>
                <div>
                  {gameState.player1 === player.playerName ? (
                    <>
                      <div className="h-20 w-3xs text-center">
                        {showHand(ownHand)}
                      </div>
                      <p className="text-center">
                        Sum: {ownHandValue}
                      </p>
                      <p className="text-center">{gameState.player1}</p>
                    </>
                  ) : (
                    <>
                      <div className="h-20 w-3xs text-center">
                        {showCardBacks(gameState.player1CardNumber)}
                      </div>
                      <p className="text-center">{gameState.player1}</p>
                    </>
                  )}
                </div>
              </div>
              <div className="flex flex-col border">
                <button className="border">Pass</button>
                <button className="border">Raise bet</button>
                <button className="border">Enough</button>
              </div>
            </div>
          </div>
          <div className="h-1/3 flex items-center justify-center">
            <div>
              <p className="text-center">{gameState.player2}'s bet:</p>
              <p className="text-center">{gameState.player2Pot}</p>
              <div>
                <p className="text-center">{gameState.player2}'s balance:</p>
                <p className="text-center">{gameState.player2Balance} $</p>
                <div className="h-20 w-3xs text-center">{}</div>
                <p className="text-center">{gameState.player2}</p>
                <p className="text-center">Sum: {}</p>
              </div>
            </div>
            <div className="flex flex-col border">
              <button className="border">Pass</button>
              <button className="border">Raise bet</button>
              <button className="border">Enough</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GamePage;
