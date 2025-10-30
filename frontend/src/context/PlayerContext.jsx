import { createContext, useContext, useState } from 'react';

const PlayerContext = createContext();

export function PlayerProvider({ children }) {
  const [player, setPlayer] = useState(null);
  const logout = () => setPlayer(null);

  return <PlayerContext.Provider value={{ player, setPlayer, logout }}>{children}</PlayerContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function usePlayer() {
  return useContext(PlayerContext);
}