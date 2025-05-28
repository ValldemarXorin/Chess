import axios, { AxiosResponse } from 'axios';
import {
    PlayerRequest,
    PlayerResponse,
    GameInfoRequest,
    GameInfoResponse,
    PlayerFilterRequest,
    FriendRequest,
    ExceptionResponse,
    PaginatedResponse
} from '../types';

const API_BASE_URL = 'http://localhost:8080';
const EMPTY_ENDPOINT = 'http://localhost:8080/empty-endpoint';

// Create axios instance with default config
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle responses and errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('currentUser');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Authentication API
export const authAPI = {
    register: async (userData: PlayerRequest): Promise<PlayerResponse> => {
        const response = await api.post('/players', userData);
        return response.data;
    },

    login: async (credentials: { email: string; password: string }): Promise<PlayerResponse> => {
        const response = await api.post('/players/login', credentials);
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
    }
};

// Players API
export const playersAPI = {
    getPlayer: async (id: number): Promise<PlayerResponse> => {
        const response = await api.get(`/players/${id}`);
        return response.data;
    },

    getAllPlayers: async (): Promise<PlayerResponse[]> => {
        const response = await api.get('/players');
        return response.data;
    },

    updatePlayer: async (id: number, userData: Partial<PlayerRequest>): Promise<PlayerResponse> => {
        const response = await api.put(`/players/${id}`, userData);
        return response.data;
    },

    deletePlayer: async (id: number): Promise<void> => {
        await api.delete(`/players/${id}`);
    },

    searchPlayers: async (params: { name?: string; email?: string }): Promise<PlayerResponse[]> => {
        const response = await api.get('/players', { params });
        return response.data;
    },

    filterPlayers: async (filterData: PlayerFilterRequest): Promise<PaginatedResponse<PlayerResponse>> => {
        const response = await api.get('/players/filter', { params: filterData });
        return response.data;
    },

    // Friends management
    getFriends: async (playerId: number): Promise<PlayerResponse[]> => {
        const response = await api.get(`/players/${playerId}/friends`);
        return response.data;
    },

    sendFriendRequest: async (playerId: number, friendEmail: string): Promise<void> => {
        await api.post(`/players/${playerId}/send_friend_request`, friendEmail);
    },

    getFriendRequests: async (playerId: number): Promise<PlayerResponse[]> => {
        const response = await api.get(`/players/${playerId}/get_friend_request`);
        return response.data;
    },

    approveFriendRequest: async (senderId: number, recipientEmail: string): Promise<void> => {
        await api.post(`/players/${senderId}/aproove_request?recipientEmail=${encodeURIComponent(recipientEmail)}`);
    },

    rejectFriendRequest: async (requestId: number): Promise<void> => {
        // Using placeholder endpoint as specified
        await api.post(EMPTY_ENDPOINT, { requestId, action: 'reject' });
    },

    removeFriend: async (playerId: number, friendEmail: string): Promise<void> => {
        await api.delete(`/players/${playerId}/remove_friend`, {
            data: friendEmail // Отправляем email как строку
        });
    },

    bulkFriendRequests: async (senderId: number, friendIds: number[]): Promise<void> => {
        await api.post(`/players/${senderId}/bulk-friend-requests`, { friendIds });
    },

    // Game history
    getPlayerGames: async (playerId: number): Promise<GameInfoResponse[]> => {
        const response = await api.get(`/players/${playerId}/gamesInfo`);
        console.log(response.data);
        return response.data;
    }
};

// Games API
export const gamesAPI = {
    createGame: async (gameData: GameInfoRequest): Promise<GameInfoResponse> => {
        const response = await api.post('/games', gameData);
        return response.data;
    },

    getGame: async (gameId: number): Promise<GameInfoResponse> => {
        const response = await api.get(`/games/${gameId}`);
        return response.data;
    },

    getAllGames: async (): Promise<GameInfoResponse[]> => {
        const response = await api.get('/games');
        return response.data;
    },

    updateGame: async (gameId: number, gameData: Partial<GameInfoRequest>): Promise<GameInfoResponse> => {
        const response = await api.put(`/games/${gameId}`, gameData);
        return response.data;
    },

    deleteGame: async (gameId: number): Promise<void> => {
        await api.delete(`/games/${gameId}`);
    }
};

// Visitor counter API
export const visitorAPI = {
    getCounter: async (): Promise<number> => {
        const response = await api.get('/counter');
        return response.data;
    },

    resetCounter: async (): Promise<void> => {
        await api.put('/counter/reset');
    }
};

// Logs API
export const logsAPI = {
    generateLogs: async (): Promise<void> => {
        await api.post('/logs/generate');
    },

    getLogsStatus: async (): Promise<string> => {
        const response = await api.get('/logs/status');
        return response.data;
    },

    downloadLogs: async (): Promise<Blob> => {
        const response = await api.get('/logs/download', { responseType: 'blob' });
        return response.data;
    }
};

// Online status (placeholder since WebSocket will handle this)
export const onlineStatusAPI = {
    getOnlineStatus: async (playerId: number): Promise<boolean> => {
        try {
            // Using placeholder endpoint for now
            const response = await api.get(EMPTY_ENDPOINT, { params: { playerId, action: 'status' } });
            return response.data?.isOnline || false;
        } catch {
            return false;
        }
    },

    updateOnlineStatus: async (playerId: number, isOnline: boolean): Promise<void> => {
        // Using placeholder endpoint
        await api.post(EMPTY_ENDPOINT, { playerId, isOnline, action: 'updateStatus' });
    }
};

// Matchmaking (placeholder for cancellation)
export const matchmakingAPI = {
    cancelMatchmaking: async (playerId: number): Promise<void> => {
        // Using placeholder endpoint as specified
        await api.post(EMPTY_ENDPOINT, { playerId, action: 'cancelMatchmaking' });
    }
};

export default api;