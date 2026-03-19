package ru.shaxowskiy.javaspeakerclub.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.Talks;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.TalksRecord;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TalkRepository {

    private final DSLContext dsl;

    public List<TalksRecord> findBySpeakerId(Long speakerId) {
        return dsl.selectFrom(Talks.TALKS)
                .where(Talks.TALKS.SPEAKER_ID.eq(speakerId))
                .orderBy(Talks.TALKS.TALK_DATE.desc())
                .fetch();
    }
}

