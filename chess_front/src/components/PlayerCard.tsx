import React from 'react';
import { PlayerResponse } from '../types';
import { User, Mail, UserPlus, UserCheck } from 'lucide-react';

interface PlayerCardProps {
    player: PlayerResponse;
    isOnline?: boolean;
    isFriend?: boolean;
    canSendFriendRequest?: boolean;
    onSendFriendRequest?: (playerEmail: string) => void;
    onViewProfile?: (playerId: number) => void;
    showActions?: boolean;
    compact?: boolean;
}

const PlayerCard: React.FC<PlayerCardProps> = ({
                                                   player,
                                                   isOnline = false,
                                                   isFriend = false,
                                                   canSendFriendRequest = false,
                                                   onSendFriendRequest,
                                                   onViewProfile,
                                                   showActions = true,
                                                   compact = false
                                               }) => {

    const handleSendFriendRequest = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (onSendFriendRequest) {
            onSendFriendRequest(player.email);
        }
    };

    const handleViewProfile = () => {
        if (onViewProfile) {
            onViewProfile(player.id);
        }
    };

    return (
        <div
            className={`glass-effect rounded-lg p-4 hover:border-chess-gold hover:border-opacity-40 transition-all duration-300 group ${
                compact ? 'space-y-2' : 'space-y-4'
            }`}
        >
            <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                    {/* Avatar with online status */}
                    <div className={`status-indicator ${isOnline ? 'online' : 'offline'}`}>
                        <div className={`${compact ? 'w-10 h-10' : 'w-12 h-12'} bg-chess-gold rounded-full flex items-center justify-center`}>
                            <User className={`${compact ? 'w-5 h-5' : 'w-6 h-6'} text-black`} />
                        </div>
                    </div>

                    {/* Player info */}
                    <div>
                        <h3 className={`${compact ? 'text-sm' : 'text-lg'} font-semibold text-white group-hover:text-chess-gold transition-colors`}>
                            {player.name}
                        </h3>
                        {!compact && (
                            <div className="flex items-center space-x-1 text-gray-400 text-sm">
                                <Mail className="w-3 h-3" />
                                <span>{player.email}</span>
                            </div>
                        )}
                    </div>
                </div>

                {/* Status and actions */}
                <div className="flex flex-col space-y-1 items-end">

                    {/* Friend status */}
                    {isFriend && (
                        <span className="text-xs px-2 py-1 rounded-full bg-chess-gold bg-opacity-20 text-chess-gold flex items-center gap-1">
                            <UserCheck className="w-3 h-3" />
                            Friend
                        </span>
                    )}
                </div>
            </div>

            {/* Actions */}
            {showActions && !compact && canSendFriendRequest && !isFriend && (
                <div className="flex pt-2 border-t border-gray-700">
                    <button
                        onClick={handleSendFriendRequest}
                        className="flex-1 px-3 py-2 bg-chess-gold hover:bg-chess-gold-light text-black text-sm rounded-md transition-all duration-200 flex items-center justify-center gap-1"
                    >
                        <UserPlus className="w-4 h-4" />
                        Add Friend
                    </button>
                </div>
            )}

            {/* Compact actions */}
            {showActions && compact && canSendFriendRequest && !isFriend && (
                <button
                    onClick={handleSendFriendRequest}
                    className="w-full px-2 py-1 bg-chess-gold hover:bg-chess-gold-light text-black text-xs rounded-md transition-all duration-200 flex items-center justify-center gap-1"
                >
                    <UserPlus className="w-3 h-3" />
                    Add
                </button>
            )}

            {/* Hover effect */}
            <div className="absolute inset-0 rounded-lg bg-chess-gold opacity-0 group-hover:opacity-5 transition-opacity duration-300 pointer-events-none"></div>
        </div>
    );
};

export default PlayerCard;