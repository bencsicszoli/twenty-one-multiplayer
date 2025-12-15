function DealerHandPlace({
  gameState,
  onShowHand,
  dealerShownCards,
  onShowCardBacks,
  dealerShownHandValue,
  dealerBalance,
  dealerFinalBalance,
}) {
  return (
    <div className="w-full h-1/2">
      <div className="w-full h-full flex flex-col">
        <div className="w-full h-1/3 flex flex-col place-items-center justify-end">
          <p>Dealer</p>
          <div className="flex">
            <p>Balance: &nbsp;</p>
            {dealerFinalBalance ? (<p
              key={dealerFinalBalance}
              className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
            >
              {dealerFinalBalance}
            </p>) : (<p
              key={dealerBalance}
              className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
            >
              {dealerBalance}
            </p>)}
            
          </div>
        </div>
        {gameState.turnName === "Dealer" ? (
          <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
            {onShowHand(dealerShownCards)}
          </div>
        ) : (
          <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
            {onShowCardBacks(gameState.dealerCardNumber)}
          </div>
        )}

        <div className="w-full h-1/6 flex justify-around">
          <div className="flex">
            {gameState.turnName === "Dealer" && (
              <>
                <p>Sum: &nbsp;</p>
                <p
                  key={dealerShownHandValue}
                  className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
                >
                  {dealerShownHandValue}
                </p>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default DealerHandPlace;
