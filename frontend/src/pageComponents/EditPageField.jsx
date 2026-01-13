function EditPageField({htmlFor, labelText, inputType, inputName, placeholder, value, onChangeHandler, autoComplete, required}) {
  return (
    <div className="flex items-center">
      <label
        htmlFor={htmlFor}
        className="pr-8 w-1/2 text-lg text-gray-900 dark:text-gray-50 text-end"
      >
        {labelText}:
      </label>
      <input
        type={inputType}
        name={inputName}
        id={htmlFor}
        className="placeholder:text-center text-center w-1/2 bg-gray-50 border border-gray-300 text-gray-900 text-lg rounded-lg focus:ring-primary-600 focus:border-primary-600 p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-gray-50 dark:focus:ring-blue-500 dark:focus:border-blue-500"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChangeHandler(e.target.value)}
        autoComplete={autoComplete}
        required={required}
      />
    </div>
  );
}

export default EditPageField;
