function RemainingCardsPlace({ gameState, onShowRemainingCards }) {

  return (
    <div className="w-full h-1/4 flex flex-col">
      <div className="w-full h-1/5">
        <p className="text-center font-semibold">
          Remaining cards: {gameState.remainingCards}
        </p>
      </div>
      <div className="w-full h-4/5 flex place-items-center flex-wrap justify-center">
        {onShowRemainingCards(gameState.remainingCards)}
      </div>
    </div>
  );
}

export default RemainingCardsPlace;
