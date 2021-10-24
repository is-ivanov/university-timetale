package ua.com.foxminded.university.ui.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.com.foxminded.university.domain.dto.StudentDto;
import ua.com.foxminded.university.domain.entity.Faculty;
import ua.com.foxminded.university.domain.entity.Group;
import ua.com.foxminded.university.domain.entity.Student;
import ua.com.foxminded.university.domain.mapper.LessonDtoMapper;
import ua.com.foxminded.university.domain.mapper.StudentDtoMapper;
import ua.com.foxminded.university.domain.service.interfaces.FacultyService;
import ua.com.foxminded.university.domain.service.interfaces.GroupService;
import ua.com.foxminded.university.domain.service.interfaces.LessonService;
import ua.com.foxminded.university.domain.service.interfaces.StudentService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.com.foxminded.university.TestObjects.*;
import static ua.com.foxminded.university.ui.controller.GroupControllerTest.ON;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    public static final String URI_STUDENTS = "/students";
    public static final String URI_STUDENTS_ID = "/students/{id}";
    public static final String IS_SHOW_INACTIVE_GROUPS = "isShowInactiveGroups";
    public static final String IS_SHOW_INACTIVE_STUDENTS = "isShowInactiveStudents";

    @Captor
    ArgumentCaptor<StudentDto> studentDtoCaptor;

    private MockMvc mockMvc;

    @Mock
    private StudentService studentServiceMock;

    @Mock
    private FacultyService facultyServiceMock;

    @Mock
    private GroupService groupServiceMock;

    @Mock
    private LessonService lessonServiceMock;

    @Mock
    private StudentDtoMapper studentDtoMapperMock;

    @Mock
    private LessonDtoMapper lessonDtoMapperMock;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(studentController)
            .build();
    }

    @Nested
    @DisplayName("test 'showStudents' method")
    class ShowStudentsTest {

        @Test
        @DisplayName("when GET request without parameters then should call expected " +
            "services and not load students in attribute of model")
        void getRequestWithoutParameters() throws Exception {
            List<Faculty> faculties = createTestFaculties();
            List<Group> groups = createTestGroups();

            when(facultyServiceMock.getAllSortedByNameAsc()).thenReturn(faculties);
            when(groupServiceMock.getAll()).thenReturn(groups);

            mockMvc.perform(get(URI_STUDENTS))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    view().name("student"),
                    model().attributeDoesNotExist(IS_SHOW_INACTIVE_GROUPS,
                        IS_SHOW_INACTIVE_STUDENTS, "students"),
                    model().attribute("faculties", faculties),
                    model().attribute("groups", groups),
                    model().attribute("facultyIdSelect", is(nullValue())),
                    model().attribute("groupIdSelect", is(nullValue()))
                );
        }

        @Test
        @DisplayName("when GET request with all parameters")
        void testWithAllParameters() throws Exception {
            int facultyId = 3;
            int groupId = 5;

            List<Student> testStudents = createTestStudents();
            List<Group> testGroups = createTestGroups();
            List<StudentDto> testStudentDtos = createTestStudentDtos(groupId);

            when(studentServiceMock.getStudentsByGroup(groupId)).thenReturn(testStudents);
            when(groupServiceMock.getAllByFacultyId(facultyId)).thenReturn(testGroups);
            when(studentDtoMapperMock.studentsToStudentDtos(testStudents))
                .thenReturn(testStudentDtos);

            mockMvc.perform(get(URI_STUDENTS)
                    .param("facultyId", String.valueOf(facultyId))
                    .param("groupId", String.valueOf(groupId))
                    .param(IS_SHOW_INACTIVE_GROUPS, ON)
                    .param(IS_SHOW_INACTIVE_STUDENTS, ON))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    view().name("student"),
                    model().attribute(IS_SHOW_INACTIVE_GROUPS, true),
                    model().attribute(IS_SHOW_INACTIVE_STUDENTS, true),
                    model().attribute("students", testStudentDtos),
                    model().attribute("groups", testGroups)
                );
            verify(studentServiceMock, never()).getStudentsByFaculty(anyInt());
        }

        @Test
        @DisplayName("when GET request with facultyId and without groupId")
        void testWithFacultyIdAndWithoutGroupId() throws Exception {
            int facultyId = 15;

            List<Student> testStudents = createTestStudents();
            List<StudentDto> testStudentDtos = createTestStudentDtos(ID1);

            when(studentServiceMock.getStudentsByFaculty(facultyId)).thenReturn(testStudents);
            when(studentDtoMapperMock.studentsToStudentDtos(testStudents))
                .thenReturn(testStudentDtos);

            mockMvc.perform(get(URI_STUDENTS)
                    .param("facultyId", String.valueOf(facultyId)))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    model().attribute("students", testStudentDtos)
                );
            verify(studentServiceMock, never()).getStudentsByGroup(anyInt());
        }
    }

    @Nested
    @DisplayName("test 'createStudent' method")
    class CreateStudentTest {
        @Test
        @DisplayName("when POST request with all required parameters then should " +
            "call studentService.add once and redirect")
        void postRequestWithParametersShouldCallStudentServiceAndRedirect() throws Exception {
            String firstName = NAME_FIRST_STUDENT;
            String patronymic = PATRONYMIC_FIRST_STUDENT;
            String lastName = LAST_NAME_FIRST_STUDENT;
            int groupId = 23;

            mockMvc.perform(post(URI_STUDENTS)
                    .param("firstName", firstName)
                    .param("patronymic", patronymic)
                    .param("lastName", lastName)
                    .param("active", ON)
                    .param("groupId", String.valueOf(groupId)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

            verify(studentDtoMapperMock, times(1))
                .studentDtoToStudent(studentDtoCaptor.capture());

            StudentDto expectedStudentDto = studentDtoCaptor.getValue();
            assertThat(expectedStudentDto.getFirstName(), is(equalTo(firstName)));
            assertThat(expectedStudentDto.getPatronymic(), is(equalTo(patronymic)));
            assertThat(expectedStudentDto.getLastName(), is(equalTo(lastName)));
            assertThat(expectedStudentDto.isActive(), is(true));
            assertThat(expectedStudentDto.getGroupId(), is(equalTo(groupId)));
        }
    }

    @Nested
    @DisplayName("test 'getStudent' method")
    class GetStudentTest {
        @Test
        @DisplayName("when GET request with PathVariable id then should return " +
            "JSON with expected studentDto in body")
        void getRequestWithPathVariableIdShouldReturnJsonWithStudentDto() throws Exception {
            Student testStudent = createTestStudent();
            StudentDto testStudentDto = createTestStudentDto();

            when(studentServiceMock.getById(STUDENT_ID1)).thenReturn(testStudent);
            when(studentDtoMapperMock.studentToStudentDto(testStudent))
                .thenReturn(testStudentDto);

            mockMvc.perform(get(URI_STUDENTS_ID, STUDENT_ID1))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id", is(STUDENT_ID1)),
                    jsonPath("$.firstName", is(NAME_FIRST_STUDENT)),
                    jsonPath("$.patronymic", is(PATRONYMIC_FIRST_STUDENT)),
                    jsonPath("$.lastName", is(LAST_NAME_FIRST_STUDENT)),
                    jsonPath("$.fullName", is(FULL_NAME_FIRST_STUDENT)),
                    jsonPath("$.active", is(true)),
                    jsonPath("$.groupId", is(GROUP_ID1)),
                    jsonPath("$.groupName", is(NAME_FIRST_GROUP))
                );
        }
    }

    @Nested
    @DisplayName("test 'updateStudent' method")
    class UpdateStudentTest {
        @Test
        @DisplayName("when PUT request with all required parameters then should " +
            "call studentDtoMapper and redirect")
        void putRequestWithParametersShouldCallStudentDtoMapper() throws Exception {
            mockMvc.perform(put(URI_STUDENTS_ID, STUDENT_ID1)
                    .param("firstName", NAME_FIRST_STUDENT)
                    .param("patronymic", PATRONYMIC_FIRST_STUDENT)
                    .param("lastName", LAST_NAME_FIRST_STUDENT)
                    .param("active", ON)
                    .param("groupId", String.valueOf(GROUP_ID1)))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

            verify(studentDtoMapperMock, times(1))
                .studentDtoToStudent(studentDtoCaptor.capture());

            StudentDto expectedStudentDto = studentDtoCaptor.getValue();

            assertThat(expectedStudentDto.getId(), is(STUDENT_ID1));
            assertThat(expectedStudentDto.getFirstName(), is(NAME_FIRST_STUDENT));
            assertThat(expectedStudentDto.getPatronymic(), is(PATRONYMIC_FIRST_STUDENT));
            assertThat(expectedStudentDto.getLastName(), is(LAST_NAME_FIRST_STUDENT));
            assertThat(expectedStudentDto.isActive(), is(true));
            assertThat(expectedStudentDto.getGroupId(), is(GROUP_ID1));
        }
    }

}