package fr.metamorpion.api.service;

import fr.metamorpion.api.model.LogAction;
import fr.metamorpion.api.repository.LogActionSer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class LogActionService {

    private final LogActionSer logActionRepository;

    public Collection<LogAction> getLogsAfter(LocalDateTime timestamp) {
        return logActionRepository.findByTimestampAfterOrderByTimestampDesc(timestamp);
    }

    public Collection<LogAction> getLogsOfRoom(String roomCode) {
        return logActionRepository.findByGameUUID(roomCode);
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

