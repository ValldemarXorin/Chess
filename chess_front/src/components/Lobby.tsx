import React, { useState, useEffect } from 'react';
import { FaChessKnight } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { sendMessage, subscribe, unsubscribe } from '../services/socket';
import '../index.css';

interface LobbyProps {
    playerId: number;
    playerName: string;
    connected: boolean;
    onMatchFound: (gameId: number, color: string) => void;
}

const Lobby: React.FC<LobbyProps> = ({ playerId, playerName, connected, onMatchFound }) => {
    const [isSearching, setIsSearching] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        console.log('Lobby mounted, connected:', connected, 'playerId:', playerId);

        if (!connected || !playerId) {
            console.log('Не подключён или playerId отсутствует:', { connected, playerId });
            navigate('/');
            return;
        }

        let subscriptionId: string | null = null;
        const destination = `/user/${playerId}/queue/matchmaking`;
        subscriptionId = subscribe(destination, (message: any) => {
            console.log('Сообщение получено в Lobby (сырые данные):', message);
            console.log('Тело сообщения (сырое):', message.body);
            try {
                const matchData = JSON.parse(message.body);
                console.log('Обработанные данные матча:', matchData);
                onMatchFound(matchData.gameId, matchData.color);
            } catch (e) {
                console.error('Ошибка парсинга сообщения:', e);
            }
        });

        return () => {
            if (subscriptionId) {
                unsubscribe(subscriptionId);
                console.log('Отписка выполнена при размонтировании Lobby');
            }
        };
    }, [connected, playerId, navigate, onMatchFound]);

    const handleMatchSearch = () => {
        if (!connected) {
            console.log('WebSocket не подключён, поиск матча невозможен');
            return;
        }

        setIsSearching(true);
        console.log('Отправка запроса на поиск матча для playerId:', playerId);
        sendMessage('/app/matchmaking', { playerId });
    };

    return (
        <div className="lobby-container">
            <div className="lobby-card">
                <div className="lobby-header">
                    <FaChessKnight className="lobby-icon" />
                    <h2>Лобби</h2>
                </div>
                <div className="lobby-content">
                    <div className="player-info">
                        <p>Игрок: {playerName}</p>
                        <p>ID: {playerId}</p>
                    </div>
                    <div className="connection-status">
                        Статус подключения:{' '}
                        <span className={connected ? 'status-connected' : 'status-disconnected'}>
              {connected ? 'Подключён' : 'Отключён'}
            </span>
                    </div>
                    <button
                        className={`match-button ${isSearching ? 'loading' : ''}`}
                        onClick={handleMatchSearch}
                        disabled={isSearching || !connected}
                    >
                        {isSearching ? <span>Поиск...</span> : 'Найти матч'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Lobby;