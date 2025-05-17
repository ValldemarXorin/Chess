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
  const [gameId, setGameId] = useState<number | null>(null);
  const [playerColor, setPlayerColor] = useState<string | null>(null);
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

  useEffect(() => {
    if (player) {
      console.log('Авторизация успешна, переход в Lobby');
      navigate('/lobby');
    }
  }, [player, navigate]);

  const handleLogin = (playerData: Player) => {
    console.log('Логин успешен, playerData:', playerData);
    setPlayer(playerData);
  };

  const handleMatchFound = (gameId: number, color: string) => {
    console.log('Матч найден:', { gameId, color });
    setGameId(gameId);
    setPlayerColor(color);
  };

  console.log('Текущее состояние App:', { player, gameId, playerColor, connected });

  return (
      <Routes>
        {!player ? (
            <Route path="/" element={<Login onLogin={handleLogin} />} />
        ) : !gameId || !playerColor ? (
            <Route
                path="/lobby"
                element={
                  <Lobby
                      playerId={player.id}
                      playerName={player.name || 'Игрок'}
                      connected={connected}
                      onMatchFound={handleMatchFound}
                  />
                }
            />
        ) : (
            <Route
                path="/game"
                element={<Game gameId={gameId} playerId={player.id} playerColor={playerColor!} />}
            />
        )}
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