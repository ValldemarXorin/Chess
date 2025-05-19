import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, useNavigate } from 'react-router-dom';
import { connect, disconnect } from './services/socket';
import Login from './components/Login';
import Lobby from './components/Lobby';
import Game from './components/Game';
import './index.css';

interface Player {
  id: number;
  email: string;
  name: string | null;
}

const AppContent: React.FC = () => {
  const [player, setPlayer] = useState<Player | null>(null);
  const [connected, setConnected] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;
    let cleanupSocket: (() => void) | undefined;

    if (player && !connected) {
      console.log('Подключение WebSocket для player:', player);
      cleanupSocket = connect(
          () => {
            if (isMounted) {
              console.log('WebSocket успешно подключён');
              setConnected(true);
            }
          },
          (error) => {
            if (isMounted) console.error('Connection error:', error);
          },
          player.id.toString(),
          setConnected
      );
    }

    return () => {
      isMounted = false;
      if (player && cleanupSocket) {
        console.log('Очистка WebSocket при размонтировании');
        cleanupSocket();
        disconnect();
      }
    };
  }, [player]);

  const handleLogin = (playerData: Player) => {
    console.log('Логин успешен, playerData:', playerData);
    setPlayer(playerData);
    console.log('Авторизация успешна, переход в Lobby'); ////////////////////
    navigate('/lobby');                                     //////////////////////////////
  };

  const handleMatchFound = (gameId: number, color: string) => {
    console.log('Матч найден:', { gameId, color });
    // Навигация теперь выполняется в Lobby.tsx
  };

  console.log('Текущее состояние App:', { player, connected });

  return (
      <Routes>
        <Route
            path="/"
            element={player ? <Lobby playerId={player.id} playerName={player.name || 'Игрок'} connected={connected} onMatchFound={handleMatchFound} /> : <Login onLogin={handleLogin} />}
        />
        <Route
            path="/lobby"
            element={player ? <Lobby playerId={player.id} playerName={player.name || 'Игрок'} connected={connected} onMatchFound={handleMatchFound} /> : <Login onLogin={handleLogin} />}
        />
        <Route path="/game/:gameId" element={<Game />} />
        <Route path="*" element={<Login onLogin={handleLogin} />} />
      </Routes>
  );
};

const App: React.FC = () => (
    <Router>
      <AppContent />
    </Router>
);

export default App;