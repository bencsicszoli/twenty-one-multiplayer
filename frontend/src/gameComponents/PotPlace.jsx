function PotPlace({ onShowCoins, playerPot, direction }) {
  return (
    <div className={`w-1/6 h-full flex ${direction} justify-center`}>
      {playerPot > 0 && (
        <>
          {onShowCoins(playerPot)}
          <div className="flex justify-center">
            <p
              key={playerPot}
              className="animate-ping animate-once animate-duration-500 animate-delay-100 animate-ease-in-out"
            >
              {playerPot}
            </p>
            <p>&nbsp; $</p>
          </div>
        </>
      )}
    </div>
  );
}

export default PotPlace;
