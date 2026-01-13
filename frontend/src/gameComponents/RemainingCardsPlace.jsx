function RemainingCardsPlace({
  onShowRemainingCards,
  remainingCards,
  finalRemainingCards,
}) {
  
  function displayRemainingCards() {
    if (finalRemainingCards) {
      return finalRemainingCards;
    } else {
      return remainingCards;
    }
  }

  function displayRemainingsCardsCount() {
    if (finalRemainingCards) {
      return finalRemainingCards;
    } else {
      return remainingCards;
    }
  }

  return (
    <div className="w-full h-1/4 flex flex-col text-xl">
      <div className="w-full h-1/5 flex justify-center">
        <p className="text-center font-semibold">Remaining cards: &nbsp;</p>
        <p
          key={displayRemainingsCardsCount()}
          className="font-bold animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
        >
          {displayRemainingsCardsCount()}
        </p>
      </div>
      <div className="w-full h-4/5 flex place-items-center flex-wrap justify-center">
        {onShowRemainingCards(displayRemainingCards())}
      </div>
    </div>
  );
}

export default RemainingCardsPlace;
