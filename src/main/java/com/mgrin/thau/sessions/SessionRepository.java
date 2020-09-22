package com.mgrin.thau.sessions;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    @Query("SELECT s FROM Session s WHERE s.open = true AND s.user.id = ?1")
    public Collection<Session> findOpenSessionsForUserId(long userId);
}