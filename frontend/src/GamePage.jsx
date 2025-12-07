import { useState, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { useWebSocket } from "./context/WebSocketContext";
import { usePlayer } from "./context/PlayerContext";
import HandlerPanel from "./gameComponents/HandlerPanel";
import PotPlace from "./gameComponents/PotPlace";
import HandPlace from "./gameComponents/HandPlace";
import { useNavigate } from "react-router-dom";
import MenuButton from "./gameComponents/MenuButton";
import DealerHandPlace from "./gameComponents/DealerHandPlace";
import RemainingCardsPlace from "./gameComponents/RemainingCardsPlace";
import MessagesPlace from "./gameComponents/MessagesPlace";

function GamePage() {
  const location = useLocation();
  const { game } = location.state || {};
  const { player } = usePlayer();
  const [gameState, setGameState] = useState(game || {});
  const { subscribe, send } = useWebSocket();
  const [ownHand, setOwnHand] = useState([]);
  const [ownHandValue, setOwnHandValue] = useState(0);
  const [ownState, setOwnState] = useState("WAITING_CARD");
  const [ohneAceAnnounced, setOhneAceAnnounced] = useState(false);
  const [bet, setBet] = useState(0);
  const [betButtonClicked, setBetButtonClicked] = useState(false);
  const [dealerHand, setDealerHand] = useState([]);
  const [dealerShownCards, setDealerShownCards] = useState(game?.dealerPublicHand?.cards || []);
  const [dealerShownHandValue, setDealerShownHandValue] = useState(game?.dealerPublicHand?.handValue || 0);
  const [dealerHandValue, setDealerHandValue] = useState(0);
  const [dealerTurnFinished, setDealerTurnFinished] = useState(false);
  const [player1PublicHand, setPlayer1PublicHand] = useState([]);
  const [player2PublicHand, setPlayer2PublicHand] = useState([]);
  const [player3PublicHand, setPlayer3PublicHand] = useState([]);
  const [player4PublicHand, setPlayer4PublicHand] = useState([]);
  const [player1PublicActiveHand, setPlayer1PublicActiveHand] = useState(game?.player1PublicHand?.cards || []);
  const [player2PublicActiveHand, setPlayer2PublicActiveHand] = useState(game?.player2PublicHand?.cards || []);
  const [player3PublicActiveHand, setPlayer3PublicActiveHand] = useState(game?.player3PublicHand?.cards || []);
  const [player4PublicActiveHand, setPlayer4PublicActiveHand] = useState(game?.player4PublicHand?.cards || []);
  const [player1PublicHandValue, setPlayer1PublicHandValue] = useState(game?.player1PublicHand?.handValue || 0);
  const [player2PublicHandValue, setPlayer2PublicHandValue] = useState(game?.player2PublicHand?.handValue || 0);
  const [player3PublicHandValue, setPlayer3PublicHandValue] = useState(game?.player3PublicHand?.handValue || 0);
  const [player4PublicHandValue, setPlayer4PublicHandValue] = useState(game?.player4PublicHand?.handValue || 0);
  const [normalInfo, setNormalInfo] = useState(game?.content || "");
  const [finalInfo, setFinalInfo] = useState("");

  const navigate = useNavigate();
  const mounted = useRef(false);

  useEffect(() => {
    if (game === null || player === null) {
      navigate("/");
    }
  }, [game, player]);

  useEffect(() => {
    subscribe(`/topic/game.${game.gameId}`, onGameUpdate);
    subscribe("/user/queue/private", onHandUpdate);
  }, [game, subscribe]);

  function onHandUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Private topic update:", message);
    switch (message.type) {
      case "hand.firstUpdate": //privát első osztás
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
        /*
      case "game.joined": //privát csatlakozás, ugyanaz, mint a publikus pullCard-é
        console.log("Joined game:", message);
        setGameState(message);
        setDealerShownCards(message.dealerPublicHand.cards);
        setDealerShownHandValue(message.dealerPublicHand.handValue); //setOwnState???
        */
        /*
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message.dealerPublicHand.cards);
            console.log("showDealerHand called");
          }
        }
          
        break;
        */
      case "hand.update": //privát kéz frissítés
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
      case "reset.ownHand":
        console.log("Resetting own hand: ", message.message);
        setOwnHand([]);
        setOwnHandValue(0);
        break;
      case "hand.withOhneAce": //privát kéz frissítés ohne ace állapotban
        console.log("Hand ohne ace:", message);
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
      case "playerState.update": //playerState változtatás, ohne ásznál használom
        console.log("PlayerState update: ", message);
        setOwnState(message.playerState);
        break;
      case "game.throwAce": //privát ász eldobás
        console.log("Player threw ace:", message);
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
      default:
        console.log("Unknown private message type:", message.type);
    }
  }

  function onGameUpdate(payload) {
    const message = JSON.parse(payload.body);
    console.log("Game update:", message);

    switch (message.type) {
      
      case "player.joined": //publikus csatlakozás, ugyanaz, mint a pullCard-é
        console.log("Another player joined:", message);
        setGameState(message);
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message);
            console.log("showDealerHand called");
          }
        } else {
          setNormalInfo(message.content);
        }
        break;
      case "player.left": //publikus, ugyanaz, mint a pullCard-é
        console.log("Player left");
        setGameState(message);
        if (message.leavingPlayer === "player1") {
          setPlayer1PublicHand([]);
          setPlayer1PublicActiveHand([]);
        } else if (message.leavingPlayer === "player2") {
          setPlayer2PublicHand([]);
          setPlayer2PublicActiveHand([]);
        } else if (message.leavingPlayer === "player3") {
          setPlayer3PublicHand([]);
          setPlayer3PublicActiveHand([]);
        } else if (message.leavingPlayer === "player4") {
          setPlayer4PublicHand([]);
          setPlayer4PublicActiveHand([]);
        }

        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message);
            console.log("showDealerHand called");
          }
        }
        setNormalInfo(message.content);
        break;
      case "game.firstCard": //publikus első osztás
        console.log("First card dealt:", message);
        setGameState(message);
        setDealerShownCards([]);
        setDealerShownHandValue(0);
        setOhneAceAnnounced(false);
        setDealerTurnFinished(false);
        setPlayer1PublicHand([]);
        setPlayer2PublicHand([]);
        setPlayer3PublicHand([]);
        setPlayer4PublicHand([]);
        setPlayer1PublicActiveHand([]);
        setPlayer2PublicActiveHand([]);
        setPlayer3PublicActiveHand([]);
        setPlayer4PublicActiveHand([]);
        setPlayer1PublicHandValue(0);
        setPlayer2PublicHandValue(0);
        setPlayer3PublicHandValue(0);
        setPlayer4PublicHandValue(0);
        setNormalInfo(message.content);
        setFinalInfo("");
        mounted.current = false;
        break;
      case "game.throwAce": //publikus, értesít az ász eldobásáról
        console.log("Player threw ace:", message);
        setGameState(message);
        setNormalInfo(message.content);
        break;
      case "game.pullCard": //publikus következő lap
        console.log("Next card:", message);
        setGameState(message);
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message);
            console.log("showDealerHand called");
          }
        } else {
          setPlayer1PublicHand(message.player1PublicHand.cards);
          setPlayer2PublicHand(message.player2PublicHand.cards);
          setPlayer3PublicHand(message.player3PublicHand.cards);
          setPlayer4PublicHand(message.player4PublicHand.cards);
          setPlayer1PublicHandValue(message.player1PublicHand.handValue);
          setPlayer2PublicHandValue(message.player2PublicHand.handValue);
          setPlayer3PublicHandValue(message.player3PublicHand.handValue);
          setPlayer4PublicHandValue(message.player4PublicHand.handValue);
          setNormalInfo(message.content);
        }
        break;

      case "game.throwCards": //publikus, értesít az 5 lap eldobásáról
        console.log("Player threw cards:", message);
        setGameState(message);
        setNormalInfo(message.content);
        break;
      case "game.passTurn": //publikus, ugyanaz, mint a pullCard-é
        console.log("Next turn:", message);
        setGameState(message);
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message);
            console.log("showDealerHand called");
          }
        } else {
          setNormalInfo(message.content);
        }
        break;
      case "game.raiseBet": //publikus, tétrakás után
        console.log("Raise bet: ", message);
        setGameState(message);
        setNormalInfo(message.content);
        break;
      case "game.newContent": //Infotábla változás, ohne Ásznál használom
        console.log("New content: ", message);
        setGameState(message);
        setNormalInfo(message.content);
        break;
      default:
        console.log("Unknown message type:", message.type);
    }
  }

  const sleep = (ms) => new Promise((res) => setTimeout(res, ms));

  async function showDealerHand(message) {
    console.log("Message in showDealerHand:", message);
    setDealerShownCards([]);
    setDealerShownHandValue(0);

    for (let i = 0; i < message.dealerPublicHand.cards.length; i++) {
      // Itt vársz 5750 ms-et mielőtt a lap megjelenik
      await sleep(1500);

      const card = message.dealerPublicHand.cards[i];
      setDealerShownCards((prev) => [...prev, card]);
      setDealerShownHandValue((prev) => prev + card.cardValue);
    }
    setDealerTurnFinished(true);
    await sleep(500);
    if (
      player1PublicHand.length === 0 &&
      message.player1PublicHand.cards.length > 0
    ) {
      setPlayer1PublicActiveHand(message.player1PublicHand.cards);
      setPlayer1PublicHandValue(message.player1PublicHand.handValue);

    }
    if (
      player2PublicHand.length === 0 &&
      message.player2PublicHand.cards.length > 0
    ) {
      setPlayer2PublicActiveHand(message.player2PublicHand.cards);
      setPlayer2PublicHandValue(message.player2PublicHand.handValue);
    }
    if (
      player3PublicHand.length === 0 &&
      message.player3PublicHand.cards.length > 0
    ) {
      setPlayer3PublicActiveHand(message.player3PublicHand.cards);
      setPlayer3PublicHandValue(message.player3PublicHand.handValue);
    }
    if (
      player4PublicHand.length === 0 &&
      message.player4PublicHand.cards.length > 0
    ) {
      setPlayer4PublicActiveHand(message.player4PublicHand.cards);
      setPlayer4PublicHandValue(message.player4PublicHand.handValue);
    }
    await sleep(500);
    setFinalInfo(message.content);
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
        if (ownState === "OHNE_ACE" && card.cardValue === 11) {
          return (
            <div key={index} className="inline-block mx-1">
              <img
                src={card.frontImagePath}
                alt={card}
                className="h-24 transition-transform duration-200 hover:-translate-y-1 hover:scale-105 cursor-pointer"
                onClick={throwAce}
              />
            </div>
          );
        } else {
          return (
            <div key={index} className="inline-block mx-1">
              <img src={card.frontImagePath} alt={card} className="h-24" />
            </div>
          );
        }
      }
    });
  }

  function throwAce() {
    console.log("Ace clicked to throw away");
    send("/app/game.throwAce", {
      gameId: gameState.gameId,
      turnName: player.playerName,
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
    const images = [];
    if (pot < 10) {
      for (let i = 0; i < pot / 2; i++) {
        images.push("another_two_dollars.png");
      }
      return images.map((image, index) => (
        <img key={index} src={image} alt="dollars" className="-mt-10" />
      ));
    } else {
      return <img src="many_dollars.png" alt="dollars" />;
    }
  }

  function getFirstCard() {
    //setOwnHand([]);
    //setOwnHandValue(0);

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

  function throwCards() {
    send("/app/game.throwCards", {
      gameId: gameState.gameId,
      turnName: player.playerName,
    });
  }

  function setOhneAceState() {
    send("/app/game.ohneAce", {
      gameId: gameState.gameId,
      turnName: player.playerName,
    });
    setOhneAceAnnounced(true);
  }

  function backToMenu() {
    send("/app/game.leave", {
      gameId: gameState.gameId,
      playerName: player.playerName,
    });
    navigate("/menu");
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl text-white px-6 sm:px-8">
          {/*Menügombok konténere */}
          <div className="w-full h-1/5 flex justify-around items-center relative -top-3">
            <MenuButton onClick={leaveGame} buttonText="Logout" />
            <MenuButton onClick={backToMenu} buttonText="Menu" />
            {gameState.state === "NEW" && (
              <MenuButton onClick={getFirstCard} buttonText="Deal!" />
            )}
          </div>

          {/* Játéktér konténere */}
          <div className="flex w-full h-4/5 relative -top-3">
            <div className="w-[38%] h-full flex flex-col">
              {/* Bal felső játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Kezelőgombok helye */}
                {gameState.turnName === player?.playerName && (
                  <HandlerPanel
                    onMoreCard={getMoreCard}
                    onBetButton={handleBetButton}
                    onChangeTurn={changeTurn}
                    ownState={ownState}
                    turnName={gameState.turnName}
                    playerName={player?.playerName}
                    identifier={gameState.player2}
                    cardNumber={gameState.player2CardNumber}
                    onThrowCards={throwCards}
                    gameState={gameState.state}
                    ownHandValue={ownHandValue}
                    onSetOhneAce={setOhneAceState}
                    ohneAceAnnounced={ohneAceAnnounced}
                    betButtonClicked={betButtonClicked}
                  />
                )}

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  betButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player2PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player2PublicHand.handValue}
                  playerSeat="player2"
                  playerBalance="player2Balance"
                  cardNumber="player2CardNumber"
                  location="top-9"
                  dealerTurnFinished={dealerTurnFinished}
                  playerPublicActiveHand={player2PublicActiveHand}
                  playerPublicActiveHandValue={player2PublicHandValue}
                />

                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player2Pot}
                  direction="flex-col"
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
                  playerName={player?.playerName}
                  identifier={gameState.player3}
                  cardNumber={gameState.player3CardNumber}
                  onThrowCards={throwCards}
                  gameState={gameState.state}
                  ownHandValue={ownHandValue}
                  onSetOhneAce={setOhneAceState}
                  ohneAceAnnounced={ohneAceAnnounced}
                  betButtonClicked={betButtonClicked}
                />

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  betButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player3PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player3PublicHand.handValue}
                  playerSeat="player3"
                  playerBalance="player3Balance"
                  cardNumber="player3CardNumber"
                  location="top-76"
                  dealerTurnFinished={dealerTurnFinished}
                  playerPublicActiveHand={player3PublicActiveHand}
                  playerPublicActiveHandValue={player3PublicHandValue}
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
              <RemainingCardsPlace
                gameState={gameState}
                onShowRemainingCards={showRemainingCards}
              />

              {/* Osztó terepe */}
              <DealerHandPlace
                gameState={gameState}
                onShowHand={showHand}
                onShowCardBacks={showCardBacks}
                dealerShownCards={dealerShownCards}
                dealerShownHandValue={dealerShownHandValue}
              />

              {/* Értesítések helye */}
              <MessagesPlace
                gameState={gameState}
                onDisplayInformation={displayInformation}
                finalInfo={finalInfo}
                normalInfo={normalInfo}
              />
            </div>
            <div className="w-[38%] h-full flex flex-col">
              {/* Jobb felső játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player1Pot}
                  direction="flex-col"
                />

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  betButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player1PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player1PublicHand.handValue}
                  playerSeat="player1"
                  playerBalance="player1Balance"
                  cardNumber="player1CardNumber"
                  location="top-9"
                  dealerTurnFinished={dealerTurnFinished}
                  playerPublicActiveHand={player1PublicActiveHand}
                  playerPublicActiveHandValue={player1PublicHandValue}
                />
                {/* Kezelőgombok helye */}
                <HandlerPanel
                  onMoreCard={getMoreCard}
                  onBetButton={handleBetButton}
                  onChangeTurn={changeTurn}
                  ownState={ownState}
                  turnName={gameState.turnName}
                  playerName={player?.playerName}
                  identifier={gameState.player1}
                  cardNumber={gameState.player1CardNumber}
                  onThrowCards={throwCards}
                  gameState={gameState.state}
                  ownHandValue={ownHandValue}
                  onSetOhneAce={setOhneAceState}
                  ohneAceAnnounced={ohneAceAnnounced}
                  betButtonClicked={betButtonClicked}
                />
              </div>

              {/* Jobb alsó játékos terepe */}
              <div className="w-full h-1/2 flex">
                {/* Tét helye */}
                <PotPlace
                  onShowCoins={showCoins}
                  playerPot={gameState.player4Pot}
                  direction="flex-col"
                />

                {/* Kézben lévő lapok helye */}
                <HandPlace
                  gameState={gameState}
                  betButtonClicked={betButtonClicked}
                  player={player}
                  handleBet={handleBet}
                  onBet={setBet}
                  onShowHand={showHand}
                  ownHand={ownHand}
                  playerPublicHand={player4PublicHand}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player4PublicHand.handValue}
                  playerSeat="player4"
                  playerBalance="player4Balance"
                  cardNumber="player4CardNumber"
                  location="top-76"
                  dealerTurnFinished={dealerTurnFinished}
                  playerPublicActiveHand={player4PublicActiveHand}
                  playerPublicActiveHandValue={player4PublicHandValue}
                />
                {/* Kezelőgombok helye */}
                <HandlerPanel
                  onMoreCard={getMoreCard}
                  onBetButton={handleBetButton}
                  onChangeTurn={changeTurn}
                  ownState={ownState}
                  turnName={gameState.turnName}
                  playerName={player?.playerName}
                  identifier={gameState.player4}
                  cardNumber={gameState.player4CardNumber}
                  onThrowCards={throwCards}
                  gameState={gameState.state}
                  ownHandValue={ownHandValue}
                  onSetOhneAce={setOhneAceState}
                  ohneAceAnnounced={ohneAceAnnounced}
                  betButtonClicked={betButtonClicked}
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
