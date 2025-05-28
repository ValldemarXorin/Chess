package com.example.chess.service.implementation;

import com.example.chess.dto.response.OnlineStatus;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.StatusService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatusServiceImpl implements StatusService {
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerRepository playerRepository;

    public void updateUserStatus(Long playerId, String status) {
        playerRepository.findById(playerId).ifPresent(user -> {
            messagingTemplate.convertAndSend("/topic/onlineStatus", new OnlineStatus(playerId, status));
        });
    }
}
