import HandlerButton from "../pageComponents/HandlerButton";

function HandlerPanel({
  onMoreCard,
  onBetButton,
  onChangeTurn,
  ownState,
  turnName,
  playerName,
  identifier,
  cardNumber,
  onThrowCards,
  gameState,
  ownHandValue,
  onSetOhneAce,
  ohneAceAnnounced,
  betButtonClicked,
}) {
  return (
    <>
      {turnName === identifier &&
        turnName === playerName &&
        gameState === "IN_PROGRESS" && (
          <>
            <div className="w-1/6 h-full flex flex-col justify-center gap-1.5">
              {!(
                (ownState === "OHNE_ACE" && ownHandValue === 22) ||
                betButtonClicked
              ) && <HandlerButton onAction={onMoreCard} buttonText="Hit" />}

              <HandlerButton onAction={onBetButton} buttonText="Bet" />
              {ownState === "COULD_STOP" &&
                turnName === playerName &&
                !betButtonClicked && (
                  <HandlerButton onAction={onChangeTurn} buttonText="Stand" />
                )}
              {cardNumber > 1 &&
                ownHandValue === 11 &&
                !ohneAceAnnounced &&
                !betButtonClicked && (
                  <HandlerButton
                    onAction={onSetOhneAce}
                    buttonText="Ohne Ace"
                  />
                )}

              {cardNumber === 5 && !betButtonClicked && (
                <HandlerButton onAction={onThrowCards} buttonText="Discard" />
              )}
            </div>
          </>
        )}
    </>
  );
}

export default HandlerPanel;
