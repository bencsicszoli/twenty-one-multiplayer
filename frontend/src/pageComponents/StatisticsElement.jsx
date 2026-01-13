function StatisticsElement({ label, gameData }) {
  return (
    <div className="flex bg-gray-50 border border-gray-300 text-gray-900 text-xl font-bold rounded-lg focus:ring-primary-600 focus:border-primary-600 w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500 mb-3">
      <div className="w-1/2 flex justify-end pr-6">
        <p>{label}:</p>
      </div>
      <div className="w-1/2 flex justify-center">
        <p>{gameData}</p>
      </div>
    </div>
  );
}

export default StatisticsElement;
