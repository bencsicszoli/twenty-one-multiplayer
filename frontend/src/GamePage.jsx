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
  const [ownHand, setOwnHand] = useState([]);
  const [ownHandValue, setOwnHandValue] = useState(0);
  const [dealerHand, setDealerHand] = useState([]);
  const [dealerHandValue, setDealerHandValue] = useState(0);
  const [player1PublicHand, setPlayer1PublicHand] = useState([]);
  const [player2PublicHand, setPlayer2PublicHand] = useState([]);
  const [player3PublicHand, setPlayer3PublicHand] = useState([]);

  useEffect(() => {
    subscribe(`/topic/game.${game.gameId}`, onGameUpdate);
    subscribe("/user/queue/private", onHandUpdate);
  }, [game]);
  /*
  useEffect(() => {
    if (gameState.turnName === "Dealer") {
      performDealerTurn();
    }
  }, [gameState.turnName]);
*/
  function onHandUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Private topic update:", message);
    switch (message.type) {
      case "hand.firstUpdate":
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        break;
      case "game.joined":
        console.log("Joined game:", message);
        setGameState(message);
        break;
      case "hand.update":
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        /*
        if (message.playerState === "MUCH" || message.playerState === "ENOUGH" || message.playerState === "FIRE") {
          changeTurn();
        }
        */
        break;
      case "reset.ownHand":
        console.log("Resetting own hand: ", message.message);
        setOwnHand([]);
        setOwnHandValue(0);
        break;
      default:
        console.log("Unknown private message type:", message.type);
    }
  }

  function onGameUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Game update:", message);

    switch (message.type) {
      case "player.joined":
        console.log("Another player joined:", message);
        setGameState(message);
        break;

      case "game.firstCard":
        console.log("First card dealt:", message);
        setGameState(message);
        break;

      case "game.pullCard":
        console.log("Next card:", message);
        setGameState(message);
        break;

      case "hand.update":
        console.log("Public hand update:", message);

        setGameState((prev) => {
          // prev = LEGFRISSEBB gameState, nem a régi bezárt closure!
          if (message.playerState === "MUCH") {
            if (prev.turnName === prev.player1) {
              console.log(">>> Updating Player1 public hand");
              setPlayer1PublicHand(message.cards);
            } else if (prev.turnName === prev.player2) {
              console.log(">>> Updating Player2 public hand");
              setPlayer2PublicHand(message.cards);
            } else if (prev.turnName === prev.player3) {
              console.log(">>> Updating Player3 public hand");
              setPlayer3PublicHand(message.cards);
            }
          }

          // gameState ezen üzenet alatt nem változik, úgyhogy visszaadjuk a régit
          return prev;
        });

        break;
      case "game.passTurn":
        console.log("Next turn:", message);
        setGameState(message);
        break;

      case "game.dealerTurn":
        console.log("Dealer's turn:", message);
        setGameState(message);
        break;
      case "dealerHand.update":
        console.log("Dealer hand update:", message);
        setDealerHand(message.cards);
        setDealerHandValue(message.handValue);
        break;
      case "publicHands.update":
        console.log("Public hands update:", message);
        setPlayer1PublicHand(message.publicHands[0].cards);
        setPlayer2PublicHand(message.publicHands[1].cards);
        setPlayer3PublicHand(message.publicHands[2].cards);
        /*
        for (const playerHand of message.publicHands) {
          if (playerHand.playerName === gameState.player1) {
            setPlayer1PublicHand(playerHand.cards);
          } else if (playerHand.playerName === gameState.player2) {
            setPlayer2PublicHand(playerHand.cards);
          } else if (playerHand.playerName === gameState.player3) {
            setPlayer3PublicHand(playerHand.cards);
          }
        }
          */
        break;
      case "reset.publicHands":
        console.log("Resetting public hands: ", message.message);
        setPlayer1PublicHand([]);
        setPlayer2PublicHand([]);
        setPlayer3PublicHand([]);
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
    setOwnHand([]);
    setOwnHandValue(0);
    setPlayer1PublicHand([]);
    setPlayer2PublicHand([]);
    setPlayer3PublicHand([]);
    send("/app/game.firstRound", { gameId: gameState.gameId });
  }

  function changeTurn() {
    send("/app/game.passTurn", {
      gameId: gameState.gameId,
      turnName: gameState.turnName,
    });
  }

  function getMoreCard() {
    //csak a turnPlayer hívhatja meg
    send("/app/game.pullCard", {
      gameId: gameState.gameId,
      turnName: gameState.turnName,
    });
  }

  function performDealerTurn() {
    send("/app/game.dealerTurn", { gameId: gameState.gameId });
  }
  /*
  async function clearGame() {
    const jwt = localStorage.getItem("jwtToken");
    console.log("jwttoken:", jwt);
    const response = await fetch(`/api/user/clean`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt },
    });
    if (!response.ok) {
      console.error("Failed to clear game");
      return;
    }
    const clearedGame = await response.json();
    console.log("Game cleared:", clearedGame);
  }
    */

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-[80rem]">
        <div className="w-full h-[42rem] bg-poker-table rounded-[70px] shadow-2xl relative text-white px-6 sm:px-8">
          <div className="h-1/3 text-center">
            <p className="text-center">Computer</p>
            <p className="text-center">{gameState.dealerBalance} $</p>
            {gameState.state === "NEW" && (
              <button onClick={getFirstCard} className="h-8 w-14 border">
                DEAL!
              </button>
            )}
            {gameState.turnName === "Dealer" ? (
              <div className="h-20 w-3xs m-auto">{showHand(dealerHand)}</div>
            ) : (
              <div className="h-20 w-3xs m-auto">
                {showCardBacks(gameState.dealerCardNumber)}
              </div>
            )}
            {gameState.turnName === "Dealer" && (
              <p className="text-center">Sum: {dealerHandValue}</p>
            )}
          </div>
          <div className="h-1/3 flex items-center justify-center">
            <div className="flex">
              <div className="flex flex-col">
                <button onClick={getMoreCard} className="border">
                  More
                </button>
                <button className="border">Raise bet</button>
                <button onClick={changeTurn} className="border">
                  Enough
                </button>
              </div>
              <div>
                <p className="text-center">{gameState.player3}'s balance:</p>
                <p className="text-center">{gameState.player3Balance} $</p>
                <div>
                  {player && gameState.player3 === player.playerName ? (
                    <>
                      <div className="h-20 w-3xs text-center">
                        {showHand(ownHand)}
                      </div>
                      <p className="text-center">Sum: {ownHandValue}</p>
                      <p className="text-center">{gameState.player3}</p>
                    </>
                  ) : (
                    <>
                      {player3PublicHand.length > 0 ? (
                        <div className="h-20 w-3xs text-center">
                          {showHand(player3PublicHand)}
                        </div>
                      ) : (
                        <div className="h-20 w-3xs text-center">
                          {showCardBacks(gameState.player3CardNumber)}
                        </div>
                      )}

                      <p className="text-center">{gameState.player3}</p>
                    </>
                  )}
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
                  {player && gameState.player1 === player.playerName ? (
                    <>
                      <div className="h-20 w-3xs text-center">
                        {showHand(ownHand)}
                      </div>
                      <p className="text-center">Sum: {ownHandValue}</p>
                      <p className="text-center">{gameState.player1}</p>
                    </>
                  ) : (
                    <>
                      {player1PublicHand.length > 0 ? (
                        <div className="h-20 w-3xs text-center">
                          {showHand(player1PublicHand)}
                        </div>
                      ) : (
                        <div className="h-20 w-3xs text-center">
                          {showCardBacks(gameState.player1CardNumber)}
                        </div>
                      )}

                      <p className="text-center">{gameState.player1}</p>
                    </>
                  )}
                </div>
              </div>
              <div className="flex flex-col">
                <button onClick={getMoreCard} className="border">
                  More
                </button>
                <button className="border">Raise bet</button>
                <button onClick={changeTurn} className="border">
                  Enough
                </button>
              </div>
            </div>
          </div>
          <div className="h-1/3 flex items-center justify-center">
            <div></div>
            <div>
              <p className="text-center">{gameState.player2}'s bet:</p>
              <p className="text-center">{gameState.player2Pot}</p>
              <div>
                <p className="text-center">{gameState.player2}'s balance:</p>
                <p className="text-center">{gameState.player2Balance} $</p>
                <div>
                  {player && gameState.player2 === player.playerName ? (
                    <>
                      <div className="h-20 w-3xs text-center">
                        {showHand(ownHand)}
                      </div>
                      <p className="text-center">Sum: {ownHandValue}</p>
                      <p className="text-center">{gameState.player2}</p>
                    </>
                  ) : (
                    <>
                      {player2PublicHand.length > 0 ? (
                        <div className="h-20 w-3xs text-center">
                          {showHand(player2PublicHand)}
                        </div>
                      ) : (
                        <div className="h-20 w-3xs text-center">
                          {showCardBacks(gameState.player2CardNumber)}
                        </div>
                      )}

                      <p className="text-center">{gameState.player2}</p>
                    </>
                  )}
                </div>
              </div>
            </div>
            <div className="flex flex-col border">
              <button onClick={getMoreCard} className="border">
                More
              </button>
              <button className="border">Raise bet</button>
              <button onClick={changeTurn} className="border">
                Enough
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GamePage;
