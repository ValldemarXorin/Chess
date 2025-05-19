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

        const destination = `/user/${playerId}/queue/matchmaking`;
        const subscriptionId = subscribe(destination, (message: any) => {
            console.log('Сообщение получено в Lobby (сырые данные, персональный маршрут):', message);
            console.log('Тело сообщения (сырое, персональный маршрут):', message.body);
            try {
                const matchData = JSON.parse(message.body);
                console.log('Обработанные данные матча (персональный маршрут):', matchData);
                if (matchData && matchData.gameId && matchData.color) {
                    console.log('Игра найдена, gameId:', matchData.gameId, 'color:', matchData.color);
                    onMatchFound(matchData.gameId, matchData.color);
                    navigate(`/game/${matchData.gameId}`, {
                        state: { color: matchData.color, playerId: playerId },
                    });
                } else {
                    console.warn('Некорректные данные матча:', matchData);
                }
            } catch (e) {
                console.error('Ошибка парсинга сообщения (персональный маршрут):', e);
            }
        });

        return () => {
            if (subscriptionId) {
                unsubscribe(subscriptionId);
                console.log('Отписка выполнена при размонтировании Lobby (персональный маршрут)');
            }
        };
    }, [playerId, navigate, connected, onMatchFound]);

    const handleMatchSearch = () => {
        if (!connected) {
            console.log('WebSocket не подключён, поиск матча невозможен');
            return;
        }

        setIsSearching(true);

        const commonDestination = '/queue/matchmaking';
        const commonSubscriptionId = subscribe(commonDestination, (message: any) => {
            console.log('Сообщение получено в Lobby (сырые данные, общий маршрут):', message);
            console.log('Тело сообщения (сырое, общий маршрут):', message.body);
            try {
                const matchData = JSON.parse(message.body);
                console.log('Обработанные данные матча (общий маршрут):', matchData);
                if (matchData.gameId && matchData.color) {
                    console.log('Игра найдена, gameId:', matchData.gameId, 'color:', matchData.color);
                    onMatchFound(matchData.gameId, matchData.color);
                    navigate(`/game/${matchData.gameId}`, {
                        state: { color: matchData.color, playerId: playerId },
                    });
                } else {
                    console.log('Сообщение не содержит данных игры:', matchData);
                }
            } catch (e) {
                console.error('Ошибка парсинга сообщения (общий маршрут):', e);
            }
        });

        console.log('Отправка запроса на поиск матча для playerId:', playerId);
        sendMessage(`/app/${playerId}/game/add-in-game-pool`, { playerId });

        return () => {
            if (commonSubscriptionId) {
                unsubscribe(commonSubscriptionId);
                console.log('Отписка выполнена от общего маршрута:', commonDestination);
            }
        };
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