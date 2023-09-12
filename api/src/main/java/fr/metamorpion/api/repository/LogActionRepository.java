package fr.metamorpion.api.repository;

import fr.metamorpion.api.model.LogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long> {
    
    List<LogAction> findByGameUUIDAndTimestampAfterOrderByTimestampDesc(String roomCode, LocalDateTime timestamp);

    List<LogAction> findByGameUUIDOrderByTimestampDesc(String roomCode);

    void deleteAllByGameUUID(String roomCode);

}
