import SockJS from 'sockjs-client';
import { Client, IMessage, StompSubscription, ActivationState } from '@stomp/stompjs';
import { ChessMoveRequest, GameStateResponse, MatchFoundResponse, OnlineStatus } from '../types';

class WebSocketService {
    private client: Client | null = null;
    private subscriptions: Map<string, StompSubscription> = new Map();
    private isConnected = false;
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 5;
    private reconnectDelay = 3000;

    private matchmakingCallback: ((response: MatchFoundResponse) => void) | null = null;

    setMatchmakingCallback(callback: (response: MatchFoundResponse) => void): void {
        this.matchmakingCallback = callback;
    }

    connect(userId: number): Promise<void> {
        return new Promise((resolve, reject) => {
            try {
                const socket = new SockJS('http://localhost:8080/ws-chess');

                this.client = new Client({
                    webSocketFactory: () => socket,
                    connectHeaders: {
                        Authorization: `Bearer ${localStorage.getItem('authToken') || ''}`,
                        user: userId.toString()
                    },
                    debug: (str) => {
                        console.log('STOMP Debug:', str);
                    },
                    reconnectDelay: this.reconnectDelay,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000,
                    onConnect: (frame) => {
                        console.log('WebSocket connected:', frame);
                        this.isConnected = true;
                        this.reconnectAttempts = 0;

                        this.subscribeToMatchmaking(userId, (response) => {
                            console.log('Match found:', response);
                            if (this.matchmakingCallback) {
                                this.matchmakingCallback(response);
                            }
                        });

                        resolve();
                    },
                    onStompError: (frame) => {
                        console.error('STOMP error:', frame);
                        this.isConnected = false;
                        reject(new Error(`STOMP error: ${frame.headers['message']}`));
                    },
                    onWebSocketClose: (event) => {
                        console.log('WebSocket closed:', event);
                        this.isConnected = false;
                        this.handleReconnect(userId);
                    },
                    onWebSocketError: (error) => {
                        console.error('WebSocket error:', error);
                        this.isConnected = false;
                        reject(error);
                    }
                });

                this.client.activate();
            } catch (error) {
                reject(error);
            }
        });
    }

    private handleReconnect(userId: number): void {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                this.connect(userId).catch((error) => {
                    console.error('Reconnection failed:', error);
                });
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    disconnect(): void {
        this.subscriptions.forEach((subscription) => {
            subscription.unsubscribe();
        });
        this.subscriptions.clear();

        if (this.client) {
            this.client.deactivate();
        }
        this.isConnected = false;
    }

    subscribeToGameMoves(gameId: number, callback: (move: ChessMoveRequest) => void): void {
        if (!this.isConnected || !this.client) {
            console.error('WebSocket не подключен');
            return;
        }
        const destination = `/topic/game/${gameId}/move`;
        console.log(`Подписка на ${destination} для игры ${gameId}`);
        const subscription = this.client.subscribe(destination, (message: IMessage) => {
            console.log(`Получено сообщение для игры ${gameId}:`, message.body);
            try {
                const move: ChessMoveRequest = JSON.parse(message.body);
                callback(move);
            } catch (error) {
                console.error('Ошибка парсинга хода:', error);
            }
        });
        this.subscriptions.set(`game-${gameId}`, subscription);
    }

    subscribeToGameState(gameId: number, callback: (state: GameStateResponse) => void): void {
        if (!this.isConnected || !this.client) {
            console.error('WebSocket не подключен');
            return;
        }
        const destination = `/topic/game/${gameId}/state`;
        console.log(`Подписка на ${destination} для игры ${gameId}`);
        const subscription = this.client.subscribe(destination, (message: IMessage) => {
            console.log(`Получено состояние игры ${gameId}:`, message.body);
            try {
                const state: GameStateResponse = JSON.parse(message.body);
                callback(state);
            } catch (error) {
                console.error('Ошибка парсинга состояния игры:', error);
            }
        });
        this.subscriptions.set(`state-${gameId}`, subscription);
    }

    sendChessMove(gameId: number, move: ChessMoveRequest): void {
        if (!this.isConnected || !this.client) {
            console.error('WebSocket не подключен');
            return;
        }
        const destination = `/app/game/${gameId}/move`;
        console.log(`Отправка хода на ${destination}:`, move);
        this.client.publish({
            destination,
            body: JSON.stringify(move)
        });
    }

    joinMatchmakingPool(playerId: number): void {
        if (!this.isConnected || !this.client) {
            console.error('WebSocket not connected');
            return;
        }

        const destination = `/app/${playerId}/game/add-in-game-pool`;
        this.client.publish({
            destination,
            body: JSON.stringify({ playerId })
        });

        console.log('Joined matchmaking pool for player:', playerId);
    }

    subscribeToMatchmaking(playerId: number, callback: (response: MatchFoundResponse) => void): void {
        if (!this.isConnected || !this.client) {
            console.error('WebSocket not connected');
            return;
        }

        const userDestination = `/topic/${playerId}/matchmaking`;
        this.unsubscribe(`matchmaking-user-${playerId}`);

        const subscription = this.client.subscribe(userDestination, (message: IMessage) => {
            try {
                const matchResponse: MatchFoundResponse = JSON.parse(message.body);
                callback(matchResponse);
            } catch (error) {
                console.error('Error parsing match found response:', error);
            }
        });

        this.subscriptions.set(`matchmaking-user-${playerId}`, subscription);
        console.log('Subscribed to matchmaking for player:', playerId);
    }

    subscribeToOnlineStatus(callback: (status: OnlineStatus) => void): void {
        if (!this.isConnected || !this.client) {
            console.error('WebSocket not connected');
            return;
        }

        const destination = '/topic/onlineStatus';
        const subscription = this.client.subscribe(destination, (message: IMessage) => {
            try {
                const status: OnlineStatus = JSON.parse(message.body);
                callback(status);
            } catch (error) {
                console.error('Error parsing online status:', error);
            }
        });

        this.subscriptions.set('online-status', subscription);
        console.log('Subscribed to online status updates');
    }

    unsubscribe(key: string): void {
        const subscription = this.subscriptions.get(key);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(key);
            console.log('Unsubscribed from:', key);
        }
    }

    sendHeartbeat(): void {
        if (this.isConnected && this.client) {
            this.client.publish({
                destination: '/app/heartbeat',
                body: JSON.stringify({ timestamp: Date.now() })
            });
        }
    }

    isWebSocketConnected(): boolean {
        return this.isConnected && this.client?.connected === true;
    }

    getConnectionState(): ActivationState {
        if (!this.client) return ActivationState.INACTIVE;
        return this.client.state;
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;