function PotPlace({onShowCoins, playerPot, direction}) {
  
  return (
    <div className={`w-1/6 h-full flex ${direction} justify-center`}>
      
        {playerPot > 0 && (
          <>
            {onShowCoins(playerPot)}
            <p>{playerPot} $</p>
          </>
        )}
      
    </div>
  );
}

export default PotPlace;
