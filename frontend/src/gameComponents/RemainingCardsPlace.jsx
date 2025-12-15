function RemainingCardsPlace({ gameState, onShowRemainingCards, remainingCards, finalRemainingCards }) {
  return (
    <div className="w-full h-1/4 flex flex-col">
      <div className="w-full h-1/5 flex justify-center">
        <p className="text-center font-semibold">Remaining cards: &nbsp;</p>
        {finalRemainingCards ? (<p
          key={finalRemainingCards}
          className="font-bold animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
        >
          {finalRemainingCards}
        </p>) : (<p
          key={remainingCards}
          className="font-bold animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
        >
          {remainingCards}
        </p>)}
        
      </div>
      {finalRemainingCards ? (<div className="w-full h-4/5 flex place-items-center flex-wrap justify-center">
        {onShowRemainingCards(finalRemainingCards)}
      </div>) : (<div className="w-full h-4/5 flex place-items-center flex-wrap justify-center">
        {onShowRemainingCards(remainingCards)}
      </div>)}
      
    </div>
  );
}

export default RemainingCardsPlace;
