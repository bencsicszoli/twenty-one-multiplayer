function PotPlace({onShowCoins, playerPot, direction}) {
  
  return (
    <div className={`w-1/6 h-full flex ${direction}`}>
      <div className="w-full h-2/5 flex flex-col place-items-center">
        {playerPot > 0 && (
          <>
            {onShowCoins(playerPot)}
            <p>{playerPot} $</p>
          </>
        )}
      </div>
    </div>
  );
}

export default PotPlace;
