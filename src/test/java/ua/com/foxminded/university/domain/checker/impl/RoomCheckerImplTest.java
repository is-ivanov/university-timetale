package ua.com.foxminded.university.domain.checker.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.com.foxminded.university.dao.interfaces.LessonDao;
import ua.com.foxminded.university.domain.entity.Lesson;
import ua.com.foxminded.university.domain.entity.Room;
import ua.com.foxminded.university.exception.ServiceException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static ua.com.foxminded.university.TestObjects.*;

@ExtendWith(MockitoExtension.class)
class RoomCheckerImplTest {

    private Room room;
    private Lesson lesson;

    @Mock
    private LessonDao lessonDaoMock;

    @Mock
    private AvailableLessonChecker availableLessonCheckerMock;

    @InjectMocks
    private RoomCheckerImpl roomChecker;

    @BeforeEach
    void setUp() {
        room = createTestRoom();
        lesson = createTestLesson(LESSON_ID1);
    }

    @Nested
    @DisplayName("test 'check' method")
    class CheckTest {
        @Test
        @DisplayName("call LessonDao and AvailableLessonChecker with expected parameters")
        void callLessonDaoAndAvailableLessonCheckerWithExpectedParameters() {
            List<Lesson> testLessons = createTestLessons();
            int roomId = room.getId();

            when(lessonDaoMock.getAllForRoom(roomId)).thenReturn(testLessons);

            roomChecker.check(room, lesson);

            verify(lessonDaoMock, times(1)).getAllForRoom(roomId);
            verify(availableLessonCheckerMock, times(1))
                .checkAvailableLesson(lesson, testLessons);
        }

        @Test
        @DisplayName("when availableLessonChecker throw ServiceException then should throw Exception")
        void whenAvailableLessonCheckerThrowServiceExceptionShouldThrowException() {
            List<Lesson> testLessons = createTestLessons();
            int roomId = room.getId();

            when(lessonDaoMock.getAllForRoom(roomId)).thenReturn(testLessons);
            doThrow(ServiceException.class).when(availableLessonCheckerMock)
                .checkAvailableLesson(lesson, testLessons);

            ServiceException e = assertThrows(ServiceException.class,
                () -> roomChecker.check(room, lesson));
            String expectedMessage = "Room id(" + roomId + ") is not available";
            assertThat(e.getMessage(), is(equalTo(expectedMessage)));
        }
    }
}