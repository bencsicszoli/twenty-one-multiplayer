function RemainingCardsPlace({ gameState, onShowRemainingCards }) {
  return (
    <div className="w-full h-1/4 flex flex-col">
      <div className="w-full h-1/5 flex justify-center">
        <p className="text-center font-semibold">Remaining cards: &nbsp;</p>
        <p
          key={gameState.remainingCards}
          className="font-bold animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
        >
          {gameState.remainingCards}
        </p>
      </div>
      <div className="w-full h-4/5 flex place-items-center flex-wrap justify-center">
        {onShowRemainingCards(gameState.remainingCards)}
      </div>
    </div>
  );
}

export default RemainingCardsPlace;
