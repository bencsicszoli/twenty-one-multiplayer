function MessagesPlace({ gameState, onDisplayInformation, normalInfo, finalInfo }) {

  function displayMessage() {
    
    if (finalInfo) {
      return (
        <p className="text-center text-sm font-semibold animate-jump-in pb-2 2xl:text-base 2xl:font-bold" 
           key={finalInfo}>
          {onDisplayInformation(finalInfo)}</p>)
    } else {
      return (
        <p className="text-center text-base font-semibold animate-jump-in 2xl:text-lg 2xl:font-bold"
           key={normalInfo}>
          {onDisplayInformation(normalInfo)}</p>)
    }
  }
  
  return (
    <div className="w-full h-1/4 flex flex-col bg-[#d7ffe4] text-[#2f4b3a] rounded-xl">
      <div className="w-full h-1/3 flex justify-around place-items-center">
        <p
          className="text-center text-2xl font-bold whitespace-pre animate-jump-in"
          key={gameState.turnName}
        >
          Turn: {`${gameState.turnName.toUpperCase()}`}
        </p>
      </div>
      <div className="w-full h-2/3 flex justify-around place-items-center">
        {displayMessage()}
      </div>
    </div>
  );
}

export default MessagesPlace;
