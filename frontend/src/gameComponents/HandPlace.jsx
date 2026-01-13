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
  playerBalanceValue,
  playerFinalBalance,
}) {

  function displayPlayerName() {
    if (gameState[playerSeat] === gameState.turnName) {
      return (
        <div className="w-1/2 flex justify-center bg-[#7fce9e] text-[#2f4b3a] text-3xl font-bold rounded-md">
          <p>{gameState[playerSeat]}</p>
        </div>
      );
    } else {
      return (
        <div className="w-1/2 flex justify-center font-semibold">
          <p>{gameState[playerSeat]}</p>
        </div>
      );
    }
  }
  function displayBetInput() {
    if (
      betButtonClicked &&
      player &&
      gameState[playerSeat] === player.playerName
    ) {
      return (
        <form
          className={`absolute ${location} flex justify-center`}
          onSubmit={handleBet}
        >
          <input
            className="bg-[#d7ffe4] h-9 w-30 rounded-l-md text-[#2f4b3a] font-bold text-center text-lg"
            onChange={(e) => onBet(e.target.value)}
            type="number"
            min="1"
            max={gameState[playerBalance]}
          />
          <button
            className="bg-[#7fce9e] text-[#2f4b3a] font-bold h-9 w-11 rounded-r-md hover:scale-110 cursor-pointer relative -top-px"
            type="submit"
          >
            Bet
          </button>
        </form>
      );
    }
  }

  function displayPlayerBalance() {
    if (!gameState[playerSeat]) {
      return null;
    }
    const balance = playerFinalBalance ?? playerBalanceValue;
    return (
      <>
        <p>Balance:&nbsp;</p>
        <p
          key={balance}
          className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
        >
          {balance}
        </p>
      </>
    );
  }

  function displayHandContent() {
    if (player && gameState[playerSeat] === player.playerName) {
      return onShowHand(ownHand);
    }
    if (playerPublicHand.length > 0) {
      return onShowHand(playerPublicHand);
    }
    if (playerPublicActiveHand.length > 0) {
      return onShowHand(playerPublicActiveHand);
    }
    return onShowCardBacks(gameState[cardNumber]);
  }

  function displayPlayerHandValue() {
    if (!(player && gameState[playerSeat] === player.playerName) &&
        playerPublicHand.length < 1 && 
        playerPublicActiveHand.length < 1) {
      return null;
    }
    let handValue = 0;
    if (player && gameState[playerSeat] === player.playerName) {
      handValue = ownHandValue;
    } else if (playerPublicHand.length > 0) {
      handValue = playerPublicHandValue;
    } else if (playerPublicActiveHand.length > 0) {
      handValue = playerPublicActiveHandValue;
    }
    return (
      <>
        <p>Sum: &nbsp;</p>
        <p
          key={handValue}
          className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
        >
          {handValue}
        </p>
      </>
    )
  }

  return (
    <div className="w-2/3 h-full flex flex-col">
      <div className="w-full h-1/3 flex flex-col place-items-center justify-end text-2xl">
        {displayPlayerName()}

        {displayBetInput()}
        <div className="flex text-lg">
          {displayPlayerBalance()}
        </div>
      </div>
      <div className="w-full h-1/2 flex flex-wrap place-items-center justify-center">
        {displayHandContent()}
      </div>

      <div className="w-full h-1/6 flex justify-around">
        <div className="flex">
          {displayPlayerHandValue()}
        </div>
      </div>
    </div>
  );
}

export default HandPlace;
