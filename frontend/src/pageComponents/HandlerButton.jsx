

function HandlerButton({onAction, buttonText}) {

  return (
    <button
      onClick={onAction}
      className="w-full h-1/4 bg-[#7fce9e] text-[#2f4b3a] font-bold rounded-xl hover:scale-105 cursor-pointer"
    >
      {buttonText}
    </button>
  );
}

export default HandlerButton;
