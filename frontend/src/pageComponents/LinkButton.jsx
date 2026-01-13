import { Link } from "react-router-dom";

function LinkButton({ whereToLink, buttonText, onHandleClick, fontStyle }) {
  return (
    <div>
      <Link to={whereToLink}>
        <button
          onClick={onHandleClick}
          className={`w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 ${fontStyle} rounded-lg px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800 ${fontStyle}`}
        >
          {buttonText}
        </button>
      </Link>
    </div>
  );
}

export default LinkButton;
