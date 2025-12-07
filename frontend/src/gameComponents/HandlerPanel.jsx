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
  ownHandValue,
  onSetOhneAce,
  ohneAceAnnounced,
  betButtonClicked,

}) {
  return (
    <>
      {turnName === identifier &&
        turnName === playerName &&
        gameState === "IN_PROGRESS" && (
          <>
            <div className="w-1/6 h-full flex flex-col justify-center gap-1.5">
              {!(ownState === "OHNE_ACE" && ownHandValue === 22 || betButtonClicked) && (
                <button
                  onClick={onMoreCard}
                  className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl hover:scale-105 cursor-pointer"
                >
                  Hit
                </button>
              )}

              <button
                onClick={onBetButton}
                className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl hover:scale-105 cursor-pointer"
              >
                Bet
              </button>
              {ownState === "COULD_STOP" && turnName === playerName && !betButtonClicked && (
                <button
                  onClick={onChangeTurn}
                  className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl hover:scale-105 cursor-pointer"
                >
                  Stand
                </button>
              )}
              {cardNumber > 1 && ownHandValue === 11 && !ohneAceAnnounced && !betButtonClicked && (
                <button
                  onClick={onSetOhneAce}
                  className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl hover:scale-105 cursor-pointer"
                >
                  Ohne ace
                </button>
              )}

              {cardNumber === 5 && !betButtonClicked && (
                <button
                  onClick={onThrowCards}
                  className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl hover:scale-105 cursor-pointer"
                >
                  Discard
                </button>
              )}
            </div>
          </>
        )}
    </>
  );
}

export default HandlerPanel;
