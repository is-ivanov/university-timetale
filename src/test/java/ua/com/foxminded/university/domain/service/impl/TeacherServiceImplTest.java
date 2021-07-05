package ua.com.foxminded.university.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.com.foxminded.university.dao.interfaces.TeacherDao;
import ua.com.foxminded.university.domain.entity.*;
import ua.com.foxminded.university.exception.DAOException;
import ua.com.foxminded.university.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    public static final String FIRST_NAME = "FirstName";
    public static final String LAST_NAME = "LastName";

    private TeacherServiceImpl teacherService;

    @Mock
    private TeacherDao teacherDaoMock;

    @BeforeEach
    void setUp() {
        teacherService = new TeacherServiceImpl(teacherDaoMock);
    }

    @Test
    @DisplayName("test 'add' when call add method then should call Dao once")
    void testAdd_CallDaoOnce() {
        Teacher teacher = new Teacher();
        teacherService.add(teacher);
        verify(teacherDaoMock).add(teacher);
    }

    @Nested
    @DisplayName("test 'getById' method")
    class getByIdTest {

        @Test
        @DisplayName("when Dao return Optional with Teacher then method " +
            "should return this Teacher")
        void testReturnExpectedTeacher() throws Exception {
            Teacher expectedTeacher = new Teacher();
            expectedTeacher.setId(1);
            expectedTeacher.setFirstName(FIRST_NAME);
            expectedTeacher.setLastName(LAST_NAME);
            expectedTeacher.setActive(true);
            expectedTeacher.setDepartment(new Department());
            when(teacherDaoMock.getById(1)).thenReturn(Optional.of(expectedTeacher));
            assertEquals(expectedTeacher, teacherService.getById(1));
        }

        @Test
        @DisplayName("when Dao return empty Optional then method should " +
            "return empty Teacher")
        void testReturnEmptyTeacher() throws Exception {
            Optional<Teacher> optional = Optional.empty();
            when(teacherDaoMock.getById(anyInt())).thenReturn(optional);
            assertEquals(new Teacher(), teacherService.getById(anyInt()));
        }

        @Test
        @DisplayName("when Dao throw DAOException then method should throw " +
            "ServiceException")
        void testThrowException() throws Exception {
            when(teacherDaoMock.getById(anyInt())).thenThrow(DAOException.class);
            assertThrows(ServiceException.class,
                () -> teacherService.getById(anyInt()));
        }
    }

    @Test
    @DisplayName("test 'getAll' when Dao return List teachers then method " +
        "should return this List")
    void testGetAll_ReturnListTeachers() {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        Department department = new Department();
        department.setId(1);
        department.setFaculty(faculty);
        Teacher teacher1 = new Teacher();
        teacher1.setId(1);
        teacher1.setActive(true);
        teacher1.setFirstName(FIRST_NAME);
        teacher1.setDepartment(department);
        List<Teacher> expectedTeachers = new ArrayList<>();
        expectedTeachers.add(teacher1);
        Teacher teacher2 = new Teacher();
        teacher2.setId(2);
        teacher2.setActive(false);
        teacher2.setDepartment(department);
        expectedTeachers.add(teacher2);

        when(teacherDaoMock.getAll()).thenReturn(expectedTeachers);
        assertEquals(expectedTeachers, teacherService.getAll());
    }

    @Test
    @DisplayName("test 'update' when call update method then should call " +
        "teacherDao once")
    void testUpdate_CallDaoOnce() {
        Teacher teacher = new Teacher();
        teacherService.update(teacher);
        verify(teacherDaoMock).update(teacher);
    }

    @Test
    @DisplayName("test 'delete' when call delete method then should call " +
        "teacherDao once")
    void testDelete_CallDaoOnce() {
        Teacher teacher = new Teacher();
        teacherService.delete(teacher);
        verify(teacherDaoMock).delete(teacher);
    }

    @Nested
    @DisplayName("test 'deactivateTeacher' method")
    class deactivateStudentTest {

        @Test
        @DisplayName("should call teacherDao.update once")
        void testCallTeacherDaoOnce() {
            Teacher teacher = new Teacher();
            teacherService.deactivateTeacher(teacher);
            verify(teacherDaoMock).update(teacher);
        }

        @Test
        @DisplayName("should update teacher with active = false")
        void testSetTeacherActiveFalse() {
            Teacher teacher = new Teacher();
            teacherService.deactivateTeacher(teacher);
            ArgumentCaptor<Teacher> captor =
                ArgumentCaptor.forClass(Teacher.class);
            verify(teacherDaoMock).update(captor.capture());
            assertFalse(captor.getValue().isActive());
        }
    }

    @Nested
    @DisplayName("test 'activateTeacher' method")
    class activateTeacherTest {

        @Test
        @DisplayName("should call teacherDao.update once")
        void testCallTeacherDaoOnce() {
            Teacher teacher = new Teacher();
            teacherService.deactivateTeacher(teacher);
            verify(teacherDaoMock).update(teacher);
        }

        @Test
        @DisplayName("should update teacher with active = true")
        void testSetTeacherActiveTrue() {
            teacherService.activateTeacher(new Teacher());
            ArgumentCaptor<Teacher> captor =
                ArgumentCaptor.forClass(Teacher.class);
            verify(teacherDaoMock).update(captor.capture());
            assertTrue(captor.getValue().isActive());
        }
    }

    @Nested
    @DisplayName("test 'transferTeacherToDepartment' method")
    class transferTeacherToDepartmentTest {

        @Test
        @DisplayName("should call teacherDao.update once")
        void testCallTeacherDaoOnce() {
            Teacher teacher = new Teacher();
            teacherService.transferTeacherToDepartment(teacher,
                new Department());
            verify(teacherDaoMock).update(teacher);
        }

        @Test
        @DisplayName("should update Teacher with Departments from parameter")
        void testSetTeacherDepartmentEqualsExpectedDepartment(){
            Department expectedDepartment = new Department();
            expectedDepartment.setId(1);
            expectedDepartment.setName("Test department name");
            Teacher teacher = new Teacher();
            teacherService.transferTeacherToDepartment(teacher, expectedDepartment);
            ArgumentCaptor<Teacher> captor =
                ArgumentCaptor.forClass(Teacher.class);
            verify(teacherDaoMock).update(captor.capture());
            assertEquals(expectedDepartment, captor.getValue().getDepartment());
        }
    }
}