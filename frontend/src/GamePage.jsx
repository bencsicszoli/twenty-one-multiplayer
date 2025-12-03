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
  const [dealerShownCards, setDealerShownCards] = useState([]);
  const [dealerShownHandValue, setDealerShownHandValue] = useState(0);
  const [dealerHandValue, setDealerHandValue] = useState(0);
  const [dealerTurnFinished, setDealerTurnFinished] = useState(false);

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
      case "game.joined": //privát csatlakozás, ugyanaz, mint a publikus pullCard-é
        console.log("Joined game:", message);
        setGameState(message);
        setDealerShownCards(message.dealerPublicHand.cards);
        setDealerShownHandValue(message.dealerPublicHand.handValue); //setOwnState???
        /*
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message.dealerPublicHand.cards);
            console.log("showDealerHand called");
          }
        }
          */
        break;
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
      case "hand.withOhneAce":                  //privát kéz frissítés ohne ace állapotban
        console.log("Hand ohne ace:", message);
        setOwnHand(message.cards);
        setOwnHandValue(message.handValue);
        setOwnState(message.playerState);
        break;
      case "playerState.update":                        //playerState változtatás, ohne ásznál használom
        console.log("PlayerState update: ", message);
        setOwnState(message.playerState);
        break;
      case "game.throwAce":                          //privát ász eldobás
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
            showDealerHand(message.dealerPublicHand.cards);
            console.log("showDealerHand called");
          }
        }
        break;
      case "player.leaved":           //publikus, ugyanaz, mint a pullCard-é
        console.log("Player leaved");
        setGameState(message);
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message.dealerPublicHand.cards);
            console.log("showDealerHand called");
          }
        }
        break;
      case "game.firstCard": //publikus első osztás
        console.log("First card dealt:", message);
        setGameState(message);
        setDealerShownCards([]);
        setDealerShownHandValue(0);
        setOhneAceAnnounced(false);
        setDealerTurnFinished(false);
        mounted.current = false;
        break;
      case "game.throwAce":                       //publikus, értesít az ász eldobásáról
        console.log("Player threw ace:", message);
        setGameState(message);
        break;
      case "game.pullCard": //publikus következő lap
        console.log("Next card:", message);
        setGameState(message);
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message.dealerPublicHand.cards);
            console.log("showDealerHand called");
          }
        }
        break;

      case "game.throwCards":                        //publikus, értesít az 5 lap eldobásáról
        console.log("Player threw cards:", message);
        setGameState(message);
        break;
      case "game.passTurn":                 //publikus, ugyanaz, mint a pullCard-é
        console.log("Next turn:", message);
        setGameState(message);
        if (message.dealerPublicHand !== null) {
          if (!mounted.current) {
            mounted.current = true;
            showDealerHand(message.dealerPublicHand.cards);
            console.log("showDealerHand called");
          }
        }
        break;

      case "game.dealerTurn":
        console.log("Dealer's turn:", message);
        setGameState(message);
        break;
      /*
      case "dealerHand.update":
        /*
        console.log("Dealer hand update:", message);
        setDealerShownCards([]);
        if (!dealerShownCardsIsFull) {
          showDealerHand(message.cards);
          setDealerShownCardsIsFull(true);
        }
          
        if (!mounted.current) {
          mounted.current = true;
          showDealerHand(message.dealerPublicHand.cards);
        }

        console.log("showDealerHand called");

        //setDealerHand(message.cards);
        //setDealerHandValue(message.handValue);
        break;
        */
      case "game.raiseBet":                   //publikus, tétrakás után
        console.log("Raise bet: ", message);
        setGameState(message);
        break;
      case "game.newContent":                      //Infotábla változás, ohne Ásznél használom
        console.log("New content: ", message);
        setGameState(message);
        break;
      default:
        console.log("Unknown message type:", message.type);
    }
  }

  function showDealerHand(dealerHand) {
    setDealerShownCards([]);
    dealerHand.forEach((card, index) => {
      setTimeout(() => {
        setDealerShownCards((prev) => [...prev, card]);
      }, 750 * index);
      setTimeout(() => {
        setDealerShownHandValue((prev) => prev + card.cardValue);
      }, 750 * index);
    });
    setDealerTurnFinished(true);
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
                {gameState.turnName === player.playerName && (
                  <HandlerPanel
                    onMoreCard={getMoreCard}
                    onBetButton={handleBetButton}
                    onChangeTurn={changeTurn}
                    ownState={ownState}
                    turnName={gameState.turnName}
                    playerName={player.playerName}
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
                  playerPublicHand={gameState.player2PublicHand.cards}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player2PublicHand.handValue}
                  playerSeat="player2"
                  playerBalance="player2Balance"
                  cardNumber="player2CardNumber"
                  location="top-9"
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
                  playerName={player.playerName}
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
                  playerPublicHand={gameState.player3PublicHand.cards}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player3PublicHand.handValue}
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
                  playerPublicHand={gameState.player1PublicHand.cards}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player1PublicHand.handValue}
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
                  playerPublicHand={gameState.player4PublicHand.cards}
                  onShowCardBacks={showCardBacks}
                  ownHandValue={ownHandValue}
                  playerPublicHandValue={gameState.player4PublicHand.handValue}
                  playerSeat="player4"
                  playerBalance="player4Balance"
                  cardNumber="player4CardNumber"
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
