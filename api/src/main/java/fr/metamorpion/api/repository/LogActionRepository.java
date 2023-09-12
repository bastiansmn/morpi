package fr.metamorpion.api.repository;

import fr.metamorpion.api.model.LogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogActionSer extends JpaRepository<LogAction, Long> {

    List<LogAction> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);

    List<LogAction> findByGameUUID(String roomCode);

    void deleteAllByGameUUID(String roomCode);

}
