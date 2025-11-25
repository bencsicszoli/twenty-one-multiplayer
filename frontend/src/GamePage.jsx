import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { useWebSocket } from "./context/WebSocketContext";
import { usePlayer } from "./context/PlayerContext";
import HandlerPanel from "./HandlerPanel";
import PotPlace from "./PotPlace";
import HandPlace from "./HandPlace";
import { useNavigate } from "react-router-dom";

function GamePage() {
  const location = useLocation();
  const { game } = location.state || {};
  const { player } = usePlayer();
  const [gameState, setGameState] = useState(game || {});
  const { subscribe, send } = useWebSocket();
  const [ownHand, setOwnHand] = useState([]);
  const [ownHandValue, setOwnHandValue] = useState(0);
  const [ownState, setOwnState] = useState("WAITING_CARD");
  const [bet, setBet] = useState(0);
  const [betButtonClicked, setBetButtonClicked] = useState(false);
  const [dealerHand, setDealerHand] = useState([]);
  const [dealerHandValue, setDealerHandValue] = useState(0);
  const [player1PublicHandValue, setPlayer1PublicHandValue] = useState(0);
  const [player2PublicHandValue, setPlayer2PublicHandValue] = useState(0);
  const [player3PublicHandValue, setPlayer3PublicHandValue] = useState(0);
  const [player4PublicHandValue, setPlayer4PublicHandValue] = useState(0);
  const [player1PublicHand, setPlayer1PublicHand] = useState([]);
  const [player2PublicHand, setPlayer2PublicHand] = useState([]);
  const [player3PublicHand, setPlayer3PublicHand] = useState([]);
  const [player4PublicHand, setPlayer4PublicHand] = useState([]);

  const navigate = useNavigate();

  useEffect(() => {
    if (!game || !player) {
      navigate("/");
    }
  },[game, player]);

  

  useEffect(() => {
    subscribe(`/topic/game.${game.gameId}`, onGameUpdate);
    subscribe("/user/queue/private", onHandUpdate);
  }, [game]);

  function onHandUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Private topic update:", message);
    switch (message.type) {
      case "hand.firstUpdate":
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
      case "game.joined":
        console.log("Joined game:", message);
        setGameState(message);
        break;
      case "hand.update":
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
      case "reset.ownHand":
        console.log("Resetting own hand: ", message.message);
        setOwnHand([]);
        setOwnHandValue(0);
        break;
      case "playerState.update":
        console.log("PlayerState update: ", message);
        setOwnState(message.playerState);
        break;
      case "publicHands.update":
        console.log("Public hands update:", message);
        setPlayer1PublicHand(message.publicHands[0].cards);
        setPlayer1PublicHandValue(message.publicHands[0].handValue);
        setPlayer2PublicHand(message.publicHands[1].cards);
        setPlayer2PublicHandValue(message.publicHands[1].handValue);
        setPlayer3PublicHand(message.publicHands[2].cards);
        setPlayer3PublicHandValue(message.publicHands[2].handValue);
        setPlayer4PublicHand(message.publicHands[3].cards);
        setPlayer4PublicHandValue(message.publicHands[3].handValue);
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
      case "player.leaved":
        console.log("Player leaved");
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
              setPlayer1PublicHandValue(message.handValue);
            } else if (prev.turnName === prev.player2) {
              console.log(">>> Updating Player2 public hand");
              setPlayer2PublicHand(message.cards);
              setPlayer2PublicHandValue(message.handValue);
            } else if (prev.turnName === prev.player3) {
              console.log(">>> Updating Player3 public hand");
              setPlayer3PublicHand(message.cards);
              setPlayer3PublicHandValue(message.handValue);
            } else if (prev.tornName === prev.player4) {
              setPlayer4PublicHand(message.cards);
              setPlayer4PublicHandValue(message.handValue);
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
        setPlayer1PublicHandValue(message.publicHands[0].handValue);
        setPlayer2PublicHand(message.publicHands[1].cards);
        setPlayer2PublicHandValue(message.publicHands[1].handValue);
        setPlayer3PublicHand(message.publicHands[2].cards);
        setPlayer3PublicHandValue(message.publicHands[2].handValue);
        setPlayer4PublicHand(message.publicHands[3].cards);
        setPlayer4PublicHandValue(message.publicHands[3].handValue);
        break;
      case "reset.publicHands":
        console.log("Resetting public hands: ", message.message);
        setPlayer1PublicHand([]);
        setPlayer2PublicHand([]);
        setPlayer3PublicHand([]);
        setPlayer4PublicHand([]);
        break;
      case "game.raiseBet":
        console.log("Raise bet: ", message);
        setGameState(message);
        break;
      default:
        console.log("Unknown message type:", message.type);
    }
  }

  function showHand(hand) {
    return hand.map((card, index) => {
      if (hand.length > 5) {
        return (
          <div key={index} className="inline-block -mx-2">
            <img src={card.frontImagePath} alt={card} className="h-24" />
          </div>
        );
      } else if (hand.length === 5) {
        return (
          <div key={index} className="inline-block -mx-1">
            <img src={card.frontImagePath} alt={card} className="h-24" />
          </div>
        );
      } else {
        return (
          <div key={index} className="inline-block mx-1">
            <img src={card.frontImagePath} alt={card} className="h-24" />
          </div>
        );
      }
    });
  }

  function showCardBacks(numberOfCards) {
    const backs = [];
    for (let i = 0; i < numberOfCards; i++) {
      if (numberOfCards > 5) {
        backs.push(
          <div key={i} className="inline-block -mx-2">
            <img src="Back.png" alt="card-back" className="h-24" />
          </div>
        );
      } else if (numberOfCards === 5) {
        backs.push(
          <div key={i} className="inline-block -mx-1">
            <img src="Back.png" alt="card-back" className="h-24" />
          </div>
        );
      } else {
        backs.push(
          <div key={i} className="inline-block mx-1">
            <img src="Back.png" alt="card-back" className="h-24" />
          </div>
        );
      }
    }
    return backs;
  }

  function showRemainingCards(cardsNumber) {
    const cards = [];
    for (let i = 0; i < cardsNumber; i++) {
      cards.push(
        <div key={i} className="inline-block -mx-[30px]">
          <img src="Back.png" alt="card back" className="h-24" />
        </div>
      );
    }
    return cards;
  }

  function showCoins(pot) {
    if (pot < 10) {
      return <img src="two_dollars.png" alt="dollars" />;
    } else {
      return <img src="many_dollars.png" alt="dollars" />;
    }
  }

  function getFirstCard() {
    setOwnHand([]);
    setOwnHandValue(0);
    setPlayer1PublicHand([]);
    setPlayer2PublicHand([]);
    setPlayer3PublicHand([]);
    setPlayer4PublicHand([]);
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

  function handleBetButton() {
    setBetButtonClicked(true);
  }

  function handleBet(e) {
    e.preventDefault();
    sendBetAmount();
    setBetButtonClicked(false);
  }

  function sendBetAmount() {
    send("/app/game.raiseBet", {
      gameId: gameState.gameId,
      turnName: gameState.turnName,
      bet: bet,
    });
  }

  function displayInformation(information) {
    if (information.indexOf("!") !== information.lastIndexOf("!")) {
      return information
        .split("!")
        .filter(Boolean)
        .map((sentence, index) => (
          <span key={index}>
            {sentence.trim()}!
            <br />
          </span>
        ));
    } else {
      return information;
    }
  }

  function leaveGame() {
    send("/app/game.leave", {
      gameId: gameState.gameId,
      playerName: player.playerName,
    });
    navigate("/");
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8">
          {/*Menügombok konténere */}
          <div className="w-full h-1/5 flex justify-around items-center relative -top-3">
            <button
              onClick={leaveGame}
              className="text-blue-500 bg-green-200 font-bold text-3xl border-2 h-1/2 w-1/6"
            >
              Logout
            </button>
            <button className="text-blue-500 bg-green-200 font-bold text-3xl border-2 h-1/2 w-1/6">
              Menu
            </button>
            {gameState.state === "NEW" && (
              <button
                onClick={getFirstCard}
                className="text-blue-500 bg-green-200 font-bold text-3xl border-2 h-1/2 w-1/6"
              >
                New Deal
              </button>
            )}
          </div>

          {/* Játéktér konténere */}
          <div className="flex w-full h-4/5 relative -top-3">
            <div className="w-[38%] h-full flex flex-col">
              {/* Bal felső játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Kezelőgombok helye */}
                {gameState.turnName === player.playerName && (
                  <HandlerPanel
                  onMoreCard={getMoreCard}
                  onBetButton={handleBetButton}
                  onChangeTurn={changeTurn}
                  ownState={ownState}
                  turnName={gameState.turnName}
                  playerName={player.playerName}
                  identifier={gameState.player4}
                />
                )}
                

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  onBetButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player4PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={player4PublicHandValue}
                  playerSeat="player4"
                  playerBalance="player4Balance"
                  cardNumber="player4CardNumber"
                  location="top-2"
                />

                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player4Pot}
                  direction="flex-col-reverse"
                />
              </div>

              {/* Bal alsó játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Kezelőgombok helye */}
                <HandlerPanel
                  onMoreCard={getMoreCard}
                  onBetButton={handleBetButton}
                  onChangeTurn={changeTurn}
                  ownState={ownState}
                  turnName={gameState.turnName}
                  playerName={player.playerName}
                  identifier={gameState.player3}
                />

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  onBetButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player3PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={player3PublicHandValue}
                  playerSeat="player3"
                  playerBalance="player3Balance"
                  cardNumber="player3CardNumber"
                  location="top-76"
                />

                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player3Pot}
                  direction="flex-col"
                />
              </div>
            </div>
            <div className="w-[24%] h-full flex flex-col">
              {/* RemainingCards helye */}
              <div className="w-full h-1/4 flex flex-col">
                <div className="w-full h-1/5">
                  <p className="text-center font-semibold">
                    Remaining cards: {gameState.remainingCards}
                  </p>
                </div>
                <div className="w-full h-4/5 flex place-items-center flex-wrap justify-center">
                  {showRemainingCards(gameState.remainingCards)}
                </div>
              </div>

              {/* Osztó terepe */}
              <div className="w-full h-1/2">
                <div className="w-full h-full flex flex-col">
                  <div className="w-full h-1/3 flex flex-col place-items-center justify-end">
                    <p>Dealer</p>
                    <p>Balance: {gameState.dealerBalance}</p>
                  </div>
                  {gameState.turnName === "Dealer" ? (
                    <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
                      {showHand(dealerHand)}
                    </div>
                  ) : (
                    <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
                      {showCardBacks(gameState.dealerCardNumber)}
                    </div>
                  )}

                  <div className="w-full h-1/6 flex justify-around">
                    {gameState.turnName === "Dealer" && (
                      <p>Sum: {dealerHandValue}</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Értesítések helye */}
              <div className="w-full h-[25%] flex flex-col">
                <div className="w-full h-1/3 flex justify-around place-items-center">
                  <p className="text-center text-2xl font-bold whitespace-pre">
                    Turn: {`  ${gameState.turnName.toUpperCase()}`}
                  </p>
                </div>
                <div className="w-full h-2/3 flex justify-around place-items-center">
                  <p className="text-center text-base font-bold">
                    {displayInformation(gameState.content)}
                  </p>
                </div>
              </div>
            </div>
            <div className="w-[38%] h-full flex flex-col">
              {/* Jobb felső játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player1Pot}
                  direction="flex-col-reverse"
                />

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  onBetButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player1PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={player1PublicHandValue}
                  playerSeat="player1"
                  playerBalance="player1Balance"
                  cardNumber="player1CardNumber"
                  location="top-9"
                />
                {/* Kezelőgombok helye */}
                <HandlerPanel
                  onMoreCard={getMoreCard}
                  onBetButton={handleBetButton}
                  onChangeTurn={changeTurn}
                  ownState={ownState}
                  turnName={gameState.turnName}
                  playerName={player.playerName}
                  identifier={gameState.player1}
                />
              </div>

              {/* Jobb alsó játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player2Pot}
                  direction="flex-col"
                />

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  onBetButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player2PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={player2PublicHandValue}
                  playerSeat="player2"
                  playerBalance="player2Balance"
                  cardNumber="player2CardNumber"
                  location="top-76"
                />
                {/* Kezelőgombok helye */}
                <HandlerPanel
                  onMoreCard={getMoreCard}
                  onBetButton={handleBetButton}
                  onChangeTurn={changeTurn}
                  ownState={ownState}
                  turnName={gameState.turnName}
                  playerName={player.playerName}
                  identifier={gameState.player2}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GamePage;
