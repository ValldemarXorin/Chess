import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient: Client | null = null;
let cleanupBeforeUnload: (() => void) | null = null;

export const connect = (
    onConnect: () => void,
    onError: (error: any) => void,
    playerId: string,
    setConnected: (connected: boolean) => void
) => {
    console.log('Попытка подключения к WebSocket для playerId:', playerId);

    if (stompClient) {
        console.log('WebSocket уже существует, деактивируем перед повторным подключением');
        stompClient.deactivate();
    }

    const socket = new SockJS('http://localhost:8080/ws-chess');
    stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: { user: playerId },
        debug: (str: string) => console.log('STOMP Debug:', str),
        reconnectDelay: 1000,
        heartbeatIncoming: 5000,
        heartbeatOutgoing: 5000,
        onConnect: (frame: any) => {
            console.log('WebSocket подключён:', frame);
            setConnected(true);
            onConnect();
        },
        onWebSocketClose: (event: any) => {
            console.log('WebSocket закрыт, причина:', event);
            setConnected(false);
            if (stompClient) {
                stompClient.activate();
            }
        },
        onStompError: (frame: any) => {
            console.error('STOMP Error:', frame.body);
            onError(frame);
            setConnected(false);
        },
    });

    stompClient.activate();
    console.log('WebSocket активация инициирована');

    const handleBeforeUnload = () => {
        console.log('Закрытие WebSocket перед выходом');
        if (stompClient) {
            stompClient.deactivate();
            setConnected(false);
        }
    };

    if (!cleanupBeforeUnload) {
        window.addEventListener('beforeunload', handleBeforeUnload);
        cleanupBeforeUnload = () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
            console.log('Очистка слушателя beforeunload');
        };
    }

    return () => {
        if (cleanupBeforeUnload) {
            cleanupBeforeUnload();
            cleanupBeforeUnload = null;
        }
        if (stompClient) {
            console.log('Очистка WebSocket при размонтировании');
            stompClient.deactivate();
            stompClient = null;
        }
    };
};

export const disconnect = () => {
    if (stompClient && stompClient.connected) {
        console.log('WebSocket отключён');
        stompClient.deactivate();
        stompClient = null;
    }
};

export const subscribe = (destination: string, callback: (message: any) => void): string => {
    if (stompClient && stompClient.connected) {
        console.log('Подписка на destination:', destination);
        const subscription = stompClient.subscribe(destination, (message: any) => {
            console.log(`Получено сообщение на ${destination} (сырые данные):`, message);
            console.log('Тело сообщения (сырое):', message.body);
            callback(message);
        });
        console.log(`Подписка на ${destination} успешно создана с ID: ${subscription.id}`);
        return subscription.id;
    } else {
        console.log('STOMP-клиент не подключён, подписка не выполнена');
        return '';
    }
};

export const unsubscribe = (subscriptionId: string) => {
    if (stompClient && stompClient.connected && subscriptionId) {
        stompClient.unsubscribe(subscriptionId);
        console.log(`Отписка от подписки с ID: ${subscriptionId}`);
    }
};

export const sendMessage = (destination: string, body: any = {}) => {
    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination,
            body: JSON.stringify(body),
        });
        console.log(`Сообщение отправлено на ${destination}:`, body);
    } else {
        console.log('STOMP-клиент не подключён, сообщение не отправлено');
    }
};