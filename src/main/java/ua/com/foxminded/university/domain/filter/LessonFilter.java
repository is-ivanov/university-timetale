package ua.com.foxminded.university.domain.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static ua.com.foxminded.university.ui.Util.DATE_TIME_PATTERN;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonFilter {

    private Integer facultyId;
    private Integer departmentId;
    private Integer teacherId;
    private Integer courseId;
    private Integer roomId;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime dateFrom;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime dateTo;

}
