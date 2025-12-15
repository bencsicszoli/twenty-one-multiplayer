function MessagesPlace({ gameState, onDisplayInformation, normalInfo, finalInfo }) {
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
        {finalInfo ? (<p
          className="text-center text-base font-bold animate-jump-in pb-2"
          key={finalInfo}
        >
          {onDisplayInformation(finalInfo)}
        </p>) : (<p
          className="text-center text-base font-bold animate-jump-in"
          key={normalInfo}
        >
          {onDisplayInformation(normalInfo)}
        </p>)}
      </div>
    </div>
  );
}

export default MessagesPlace;
