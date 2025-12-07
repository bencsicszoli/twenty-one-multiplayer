function HandPlace({
  gameState,
  betButtonClicked,
  player,
  handleBet,
  onBet,
  onShowHand,
  ownHand,
  playerPublicHand,
  onShowCardBacks,
  ownHandValue,
  playerPublicHandValue,
  playerSeat,
  playerBalance,
  cardNumber,
  location,
  playerPublicActiveHand,
  playerPublicActiveHandValue,
}) {
  return (
    <div className="w-2/3 h-full flex flex-col">

      <div className="w-full h-1/3 flex flex-col place-items-center justify-end">
      {gameState[playerSeat] === gameState.turnName ? (<div className="w-1/2 flex justify-center bg-[#7fce9e] text-[#2f4b3a] text-lg font-semibold rounded-md">
          <p>{gameState[playerSeat]}</p>
        </div>) : (<div className="w-1/2 flex justify-center">
          <p>{gameState[playerSeat]}</p>
        </div>)}
        
        
        {betButtonClicked &&
          player &&
          gameState[playerSeat] === player.playerName && (
            <form className={`absolute ${location}`} onSubmit={handleBet}>
              <input
                className="bg-[#d7ffe4] h-8 w-28 rounded-l-md text-[#2f4b3a] font-bold text-center text-lg"
                onChange={(e) => onBet(e.target.value)}
                type="number"
                min="1"
                max={gameState[playerBalance]}
              />
              <button
                className="bg-[#7fce9e] text-[#2f4b3a] font-bold h-8 w-9 rounded-r-md hover:scale-110 cursor-pointer relative -top-px"
                type="submit"
              >
                Bet
              </button>
            </form>
          )}
        <div className="flex">
          {gameState[playerSeat] && (
            <>
              <p>Balance: &nbsp;</p>

              <p
                key={gameState[playerBalance]}
                className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
              >
                {gameState[playerBalance]}
              </p>
            </>
          )}
        </div>
      </div>
      {player && gameState[playerSeat] === player.playerName ? (
        <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
          {onShowHand(ownHand)}
        </div>
      ) : playerPublicHand.length > 0 ? (
        <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
          {onShowHand(playerPublicHand)}
        </div>
      ) : playerPublicActiveHand.length > 0 ? (
        <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
          {onShowHand(playerPublicActiveHand)}
        </div>
      ) : (
        <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
          {onShowCardBacks(gameState[cardNumber])}
        </div>
      )}

      <div className="w-full h-1/6 flex justify-around">
        <div className="flex">
          {player && gameState[playerSeat] === player.playerName ? (
            <>
              <p>Sum: &nbsp;</p>
              <p
                key={ownHandValue}
                className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
              >
                {ownHandValue}
              </p>
            </>
          ) : playerPublicHand.length > 0 ? (
            <>
              <p>Sum: &nbsp;</p>
              <p
                key={playerPublicHandValue}
                className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
              >
                {playerPublicHandValue}
              </p>
            </>
          ) : (
            playerPublicActiveHand.length > 0 && (
              <>
                <p>Sum: &nbsp;</p>
                <p
                  key={playerPublicActiveHandValue}
                  className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
                >
                  {playerPublicActiveHandValue}
                </p>
              </>
            )
          )}
        </div>
      </div>
    </div>
  );
}

export default HandPlace;
