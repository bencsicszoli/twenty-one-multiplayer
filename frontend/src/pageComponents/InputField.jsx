function InputField({
  htmlFor,
  labelText,
  inputType,
  inputName,
  inputId,
  placeholderText,
  inputValue,
  onInputValue,
  autoComplete,
}) {
  
  return (
    <div>
      <label
        htmlFor={htmlFor}
        className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
      >
        {labelText}
      </label>
      <input
        type={inputType}
        name={inputName}
        id={inputId}
        className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
        placeholder={placeholderText}
        value={inputValue}
        onChange={(e) => onInputValue(e.target.value)}
        required
        autoComplete={autoComplete}
      />
    </div>
  );
}

export default InputField;
