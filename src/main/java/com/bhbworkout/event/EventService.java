package com.bhbworkout.event;

import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Event;
import com.bhbworkout.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    @Transactional
    public Event createEvent(Event event, Study study, Account account) {
        event.setCreateBy(account); // 누가 만드는거냐?
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }
}
