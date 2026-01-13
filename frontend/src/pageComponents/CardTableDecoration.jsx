import CardColorIcon from "./CardColorIcon";
import bellSvg from "../assets/bell.svg";
import heartSvg from "../assets/heart.svg";
import acornSvg from "../assets/acorn.svg";
import leafSvg from "../assets/leaf.svg";

function CardTableDecoration() {
  return (
    <>
      <CardColorIcon
        source={heartSvg}
        altText="Heart"
        verticalPosition="top-6"
        horizontalPosition="left-8"
        rotation="-rotate-6"
      />
      <CardColorIcon
        source={acornSvg}
        altText="Acorn"
        verticalPosition="top-6"
        horizontalPosition="right-8"
        rotation="rotate-12"
      />
      <CardColorIcon
        source={bellSvg}
        altText="Bell"
        verticalPosition="bottom-8"
        horizontalPosition="left-8"
        rotation="-rotate-12"
      />
      <CardColorIcon
        source={leafSvg}
        altText="Leaf"
        verticalPosition="bottom-8"
        horizontalPosition="right-8"
        rotation="rotate-6"
      />
    </>
  );
}

export default CardTableDecoration;
