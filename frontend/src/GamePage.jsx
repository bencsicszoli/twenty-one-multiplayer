import { useState } from "react";

function GamePage() {
  const [computerBalance, setComputerBalance] = useState(100);
  const [remainingCards, setRemainingCards] = useState(32);
  const [player1Name, setPlayer1Name] = useState("Player 1");
  const [player1Balance, setPlayer1Balance] = useState(100);
  const [player1Bet, setPlayer1Bet] = useState(0);
  const [player1CardsNumber, setPlayer1CardsNumber] = useState(0);
  const [player1Hand, setPlayer1Hand] = useState([]);
  const [player1HandValue, setPlayer1HandValue] = useState(0);
  const [player2Name, setPlayer2Name] = useState("Player 2");
  const [player2Balance, setPlayer2Balance] = useState(100);
  const [player2Bet, setPlayer2Bet] = useState(0);
  const [player2CardsNumber, setPlayer2CardsNumber] = useState(0);
  const [player2Hand, setPlayer2Hand] = useState([]);
  const [player2HandValue, setPlayer2HandValue] = useState(0);
  const [player3Name, setPlayer3Name] = useState("Player 3");
  const [player3Balance, setPlayer3Balance] = useState(100);
  const [player3Bet, setPlayer3Bet] = useState(0);
  const [player3CardsNumber, setPlayer3CardsNumber] = useState(0);
  const [player3Hand, setPlayer3Hand] = useState([]);
  const [player3HandValue, setPlayer3HandValue] = useState(0);

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-[80rem]">
        <div className="w-full h-[42rem] bg-poker-table rounded-[70px] shadow-2xl relative text-white px-6 sm:px-8">
          <div className="h-1/3">
            <p className="text-center">Computer</p>
            <p className="text-center">{computerBalance} $</p>
            <div>{/* Computer cards */}</div>
          </div>
          <div className="h-1/3 flex items-center justify-center">
            <div className="flex">
              <div className="flex flex-col border">
                <button className="border">Pass</button>
                <button className="border">Raise bet</button>
                <button className="border">Enough</button>
              </div>
              <div>
                <p className="text-center">{player3Name}'s balance:</p>
                <p className="text-center">{player3Balance}</p>
                <div>
                  <div>{/* Player3 cards*/}</div>
                  <p className="text-center">{player3Name}</p>
                  <p className="text-center">Sum: {player3HandValue}</p>
                </div>
              </div>
              <div>
                <p className="text-center">{player3Name} bet:</p>
                <p className="text-center">{player3Bet}</p>
              </div>
            </div>
            <div>
              <div>
                <div>{/* Shuffled area */}</div>
                <p className="text-center">Remaining cards:</p>
                <p className="text-center">{remainingCards}</p>
              </div>
            </div>
            <div className="flex">
              <div>
                <p className="text-center">{player1Name} bet:</p>
                <p className="text-center">{player1Bet}</p>
              </div>
              <div>
                <p className="text-center">{player1Name}'s balance:</p>
                <p className="text-center">{player1Balance}</p>
                <div>
                  <div>{/* Player3 cards*/}</div>
                  <p className="text-center">{player1Name}</p>
                  <p className="text-center">Sum: {player1HandValue}</p>
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
              <p className="text-center">{player2Name} bet:</p>
              <p className="text-center">{player2Bet}</p>
              <div>
                <p className="text-center">{player2Name}'s balance:</p>
                <p className="text-center">{player2Balance}</p>
                <div>{/* Player2 cards*/}</div>
                <p className="text-center">{player2Name}</p>
                <p className="text-center">Sum: {player2HandValue}</p>
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
