function EditButton({onHandle, type, buttonText}) {
  return (
    <button
      onClick={onHandle}
      type={type}
      className="w-full text-gray-50 bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-bold rounded-lg text-md px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
    >
      {buttonText}
    </button>
  );
}

export default EditButton;
