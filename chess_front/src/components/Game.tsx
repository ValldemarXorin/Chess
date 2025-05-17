import React, { useState, useEffect } from 'react';
import '../index.css';

interface GameProps {
    gameId: number;
    playerId: number;
    playerColor: string;
}

const Game: React.FC<GameProps> = ({ gameId, playerId, playerColor }) => {
    const [chatMessage, setChatMessage] = useState('');
    const [messages, setMessages] = useState<string[]>([]);

    const handleChatSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (chatMessage.trim()) {
            setMessages([...messages, `${playerColor}: ${chatMessage}`]);
            setChatMessage('');
        }
    };

    useEffect(() => {
        console.log('Game mounted with gameId:', gameId, 'playerId:', playerId, 'playerColor:', playerColor);
    }, [gameId, playerId, playerColor]);

    return (
        <div className="game-container">
            <h2>Игра #{gameId}</h2>
            <p>Вы играете за {playerColor}</p>
            <div className="chat-section">
                <h3>Чат</h3>
                <div className="chat-messages">
                    {messages.map((msg, index) => (
                        <p key={index}>{msg}</p>
                    ))}
                </div>
                <form onSubmit={handleChatSubmit}>
                    <input
                        type="text"
                        id="chat-message"
                        name="chat-message"
                        autoComplete="off" /* Чат не должен автозаполняться */
                        placeholder="Введите сообщение..."
                        value={chatMessage}
                        onChange={(e) => setChatMessage(e.target.value)}
                    />
                    <button type="submit">Отправить</button>
                </form>
            </div>
        </div>
    );
};

export default Game;