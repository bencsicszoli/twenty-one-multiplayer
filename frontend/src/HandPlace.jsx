function HandPlace({
  gameState,
  onBetButtonClicked,
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
  location
}) {
  
  return (
    <div className="w-2/3 h-full flex flex-col">
      <div className="w-full h-1/3 flex flex-col place-items-center justify-end">
        <p>{gameState[playerSeat]}</p>
        {onBetButtonClicked &&
          player &&
          gameState[playerSeat] === player.playerName && (
            <form className={`absolute ${location}`} onSubmit={handleBet}>
              <input
                className="bg-gray-400"
                onChange={(e) => onBet(e.target.value)}
                type="number"
                min="1"
                max={gameState[playerBalance]}              />
              <button className="bg-green-400 h-7 w-9" type="submit">
                Bet
              </button>
            </form>
          )}
        {gameState[playerSeat] && (
          <p>Balance: {gameState[playerBalance]}</p>
        )}
        
      </div>
      {player && gameState[playerSeat] === player.playerName ? (
        <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
          {onShowHand(ownHand)}
        </div>
      ) : (
        <>
          {playerPublicHand.length > 0 ? (
            <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
              {onShowHand(playerPublicHand)}
            </div>
          ) : (
            <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
              {onShowCardBacks(gameState[cardNumber])}
            </div>
          )}
        </>
      )}

      <div className="w-full h-1/6 flex justify-around">
        {player && gameState[playerSeat] === player.playerName ? (
          <p>Sum: {ownHandValue}</p>
        ) : (
          <>
            {playerPublicHand.length > 0 && (
              <p>Sum: {playerPublicHandValue}</p>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default HandPlace;
