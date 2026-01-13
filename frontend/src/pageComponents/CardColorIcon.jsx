function CardColorIcon({source, altText, verticalPosition, horizontalPosition, rotation}) {
  
  return (
    <img
      src={source}
      alt={altText}
      className={`absolute ${verticalPosition} ${horizontalPosition} w-20 md:w-28 opacity-80 ${rotation} pointer-events-none`}
    />
  );
}

export default CardColorIcon;
