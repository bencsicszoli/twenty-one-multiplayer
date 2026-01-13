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
    <div className="w-full h-1/2 text-xl font-semibold">
      <div className="w-full h-full flex flex-col">
        <div className="w-full h-1/3 flex flex-col place-items-center justify-end">
          {gameState.turnName === "Dealer" ? (
            <div className="w-1/2 flex justify-center bg-[#7fce9e] text-[#2f4b3a] text-3xl font-bold rounded-md">
              <p>Dealer</p>
            </div>
          ) : (
            <div className="w-1/2 flex justify-center font-semibold">
              <p>Dealer</p>
            </div>
          )}
          <div className="flex text-lg font-normal">
            <p>Balance: &nbsp;</p>
            {dealerFinalBalance ? (
              <p
                key={dealerFinalBalance}
                className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
              >
                {dealerFinalBalance}
              </p>
            ) : (
              <p
                key={dealerBalance}
                className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
              >
                {dealerBalance}
              </p>
            )}
          </div>
        </div>
        {gameState.turnName === "Dealer" ? (
          <div className="w-full h-1/2 flex flex-nowrap place-items-center justify-center">
            {onShowHand(dealerShownCards)}
          </div>
        ) : (
          <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
            {onShowCardBacks(gameState.dealerCardNumber)}
          </div>
        )}

        <div className="w-full h-1/6 flex justify-around">
          <div className="flex font-normal">
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
