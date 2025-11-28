package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.requests.model.RequestsCount;

import java.util.List;
import java.util.Set;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("""
            SELECT pr.eventId as id, COUNT(pr) as count
            FROM ParticipationRequest pr
            WHERE pr.eventId IN :ids AND pr.status = 'CONFIRMED'
            GROUP BY pr.eventId""")
    List<RequestsCount> countConfirmedRequestsForEvents(@Param("ids") Set<Long> ids);
}