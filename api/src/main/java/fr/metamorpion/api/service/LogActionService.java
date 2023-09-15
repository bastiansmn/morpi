package fr.metamorpion.api.service;

import fr.metamorpion.api.model.LogAction;
import fr.metamorpion.api.repository.LogActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class LogActionService {

    private final LogActionRepository logActionRepository;

    public Collection<LogAction> getLogsAfter(LocalDateTime timestamp, String roomCode) {
        return logActionRepository.findByGameUUIDAndTimestampAfterOrderByTimestampDesc(roomCode, timestamp);
    }

    public Collection<LogAction> getLogsOfRoom(String roomCode) {
        return logActionRepository.findByGameUUIDOrderByTimestampDesc(roomCode);
    }

    public void deleteAllByGameUUID(String roomCode) {
        logActionRepository.deleteAllByGameUUID(roomCode);
    }

    public LogAction saveLogAction(String playerUUID, String roomCode, Long i, Long j) {
        var logAction = new LogAction();
        logAction.setPlayerUUID(playerUUID);
        logAction.setGameUUID(roomCode);
        logAction.setX(j);
        logAction.setY(i);
        return logActionRepository.save(logAction);
    }

}

