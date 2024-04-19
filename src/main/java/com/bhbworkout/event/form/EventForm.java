package com.bhbworkout.event.form;

import com.bhbworkout.domain.EventType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class EventForm {

    @NotBlank // 빈문자열이면 안되고
    @Length(max = 50) // 최대 50글자
    private String title;
    private String description;
    private EventType eventType = EventType.FCFS; // 기본값으로 설정하기 위함 (선착순)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endEnrollmentDateTime; // 언제까지 접수를 받을꺼냐?
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime; // 모임일시
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime; // 종료일시

    @Min(2)
    private Integer limitOfEnrollments = 2; // 최소 몇명?

}
