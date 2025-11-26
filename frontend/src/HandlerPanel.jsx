function HandlerPanel({
  onMoreCard,
  onBetButton,
  onChangeTurn,
  ownState,
  turnName,
  playerName,
  identifier,
  cardNumber,
  onThrowCards,
  gameState,

}) {
  console.log("HandlerPanel ownState:", ownState);
  console.log("HandlerPanel turnName:", turnName);
  console.log("HandlerPanel playerName:", playerName);

  return (
    <>
      {turnName === identifier && turnName === playerName && gameState === 'IN_PROGRESS' && (
        <>
          <div className="w-1/6 h-full flex flex-col justify-center gap-1.5">
            <button
              onClick={onMoreCard}
              className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl"
            >
              Hit
            </button>
            <button
              onClick={onBetButton}
              className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl"
            >
              Bet
            </button>
            {ownState === "COULD_STOP" && turnName === playerName && (
              <button
                onClick={onChangeTurn}
                className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl"
              >
                Stand
              </button>
            )}

            <button className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold hidden rounded-xl">
              Ohne ace
            </button>
            {cardNumber === 5 && (
              <button onClick={onThrowCards} className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl">
                Throw cards
              </button>
            )}
          </div>
        </>
      )}
    </>
  );
}

export default HandlerPanel;
