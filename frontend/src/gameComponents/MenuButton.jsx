function MenuButton({ onClick, buttonText }) {
  return (
    <button
      onClick={onClick}
      className="text-[#2f4b3a] bg-[#7fce9e] font-bold text-3xl border-2 h-1/2 w-1/8 rounded-xl hover:scale-105 cursor-pointer"
    >
      {buttonText}
    </button>
  );
}

export default MenuButton;
