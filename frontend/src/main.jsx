import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import LoginPage from './authentication/LoginPage.jsx'
import RegisterPage from './authentication/RegisterPage.jsx'
import { PlayerProvider } from './context/PlayerContext.jsx';
import MenuPage from './MenuPage.jsx';
import StatisticsPage from './StatisticsPage.jsx';
import EditPage from './EditPage.jsx';
import GamePage from './GamePage.jsx';

export default function App() {

  const router = createBrowserRouter([
    { path: '/', element: <LoginPage /> },
    { path: '/register', element: <RegisterPage /> },
    {path: '/menu', element: <MenuPage /> },
    {path: '/statistics', element: <StatisticsPage /> },
    {path: '/editpage', element: <EditPage /> },
    {path: '/game', element: <GamePage /> },
  ]);

  return <RouterProvider router={router} />;
}

createRoot(document.getElementById('root')).render(
    <StrictMode>
      <PlayerProvider>
          <App />
      </PlayerProvider>
    </StrictMode>
);
