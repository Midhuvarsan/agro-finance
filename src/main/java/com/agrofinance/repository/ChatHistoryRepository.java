package com.agrofinance.repository;
 
import com.agrofinance.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
 
    /** Recent context for the assistant — newest first, service reverses. */
    List<ChatHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
 
}
 






























