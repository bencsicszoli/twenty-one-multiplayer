function HandlerPanel({
  onMoreCard,
  onBetButton,
  onChangeTurn,
  ownState,
  turnName,
  playerName,
  identifier
}) {
  console.log("HandlerPanel ownState:", ownState);
  console.log("HandlerPanel turnName:", turnName);
  console.log("HandlerPanel playerName:", playerName);

  return (<>
    {turnName === identifier && turnName === playerName && (<>
      <div className="w-1/6 h-full flex flex-col justify-center">
      
          <button
            onClick={onMoreCard}
            className="w-full h-1/4 bg-green-200 text-blue-500 font-bold border border-blue-500"
          >
            Hit
          </button>
        
      {/*<button
                    onClick={onMoreCard}
                    className="w-full h-1/4 bg-green-200 text-blue-500 font-bold border border-blue-500"
                  >
                    Hit
                  </button>*/}

      <button
        onClick={onBetButton}
        className="w-full h-1/4 bg-green-200 text-blue-500 font-bold border border-blue-500"
      >
        Bet
      </button>
      {ownState === "COULD_STOP" && turnName === playerName && (
        <button
        onClick={onChangeTurn}
        className="w-full h-1/4 bg-green-200 text-blue-500 font-bold border border-blue-500"
      >
        Stand
      </button>
      )}
      
      <button className="w-full h-1/4 bg-green-200 text-blue-500 font-bold border border-blue-500 hidden">
        Ohne ace
      </button>
      {/*
                  <button className="w-full h-14 bg-green-200 text-blue-500 border border-blue-500">5 card</button>
                  */}
    </div>
    </>)}
    
  </>);
}

export default HandlerPanel;
