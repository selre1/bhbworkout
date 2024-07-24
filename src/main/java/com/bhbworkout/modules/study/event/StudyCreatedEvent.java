package com.bhbworkout.modules.study.event;

import com.bhbworkout.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent{
    private final Study study;
}
