package ua.com.foxminded.university.ui.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import ua.com.foxminded.university.domain.entity.Faculty;
import ua.com.foxminded.university.domain.entity.Group;
import ua.com.foxminded.university.domain.entity.Student;
import ua.com.foxminded.university.domain.service.interfaces.FacultyService;
import ua.com.foxminded.university.domain.service.interfaces.GroupService;
import ua.com.foxminded.university.domain.service.interfaces.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private static final int ID1 = 1;
    private static final int ID2 = 2;
    public static final String NAME_FIRST_FACULTY = "faculty1";
    public static final String NAME_SECOND_FACULTY = "faculty2";
    public static final String NAME_FIRST_GROUP = "group1";
    public static final String NAME_SECOND_GROUP = "group2";
    public static final String URL_TEMPLATE = "/student?facultyId=1&groupId=2&isShowInactiveGroups=on&isShowInactiveStudents=on";

    private MockMvc mockMvc;

    @Mock
    private StudentService studentServiceMock;

    @Mock
    private FacultyService facultyServiceMock;

    @Mock
    private GroupService groupServiceMock;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(studentController)
            .setViewResolvers(viewResolver)
            .build();
    }

    @Test
    @DisplayName("Test showStudents without parameters")
    void testShowStudentsWithoutParameters() throws Exception {
        Faculty faculty1 = new Faculty(ID1, NAME_FIRST_FACULTY);
        Faculty faculty2 = new Faculty(ID2, NAME_SECOND_FACULTY);
        List<Faculty> faculties = Arrays.asList(faculty1, faculty2);

        Group group1 = new Group(ID1, NAME_FIRST_GROUP, faculty1, true);
        Group group2 = new Group(ID2, NAME_SECOND_GROUP, faculty2, true);
        List<Group> groups = Arrays.asList(group1, group2);

        when(facultyServiceMock.getAllSortedByNameAsc()).thenReturn(faculties);
        when(groupServiceMock.getAll()).thenReturn(groups);

        mockMvc.perform(get("/student"))
            .andDo(print())
            .andExpect(matchAll(
                status().isOk(),
                view().name("student"),
                model().attributeDoesNotExist("isShowInactiveGroups",
                    "isShowInactiveStudents", "students"),
                model().attribute("faculties", faculties),
                model().attribute("groups", groups),
                model().attribute("facultyIdSelect", is(nullValue())),
                model().attribute("groupIdSelect", is(nullValue()))
            ));
    }

    @Test
    @DisplayName("Test showStudents with all parameters")
    void testShowStudentsWithAllParameters() throws Exception {
        Student student1 = new Student();
        student1.setId(ID1);
        student1.setFirstName("student1 name");
        List<Student> students = Collections.singletonList(student1);
        Group group1 = new Group();
        group1.setId(ID1);
        List<Group> groups = Collections.singletonList(group1);

        when(studentServiceMock.getStudentsByGroup(ID2)).thenReturn(students);
        when(groupServiceMock.getAllByFacultyId(ID1)).thenReturn(groups);

        mockMvc.perform(get(URL_TEMPLATE))
            .andDo(print())
            .andExpect(matchAll(
               status().isOk(),
                view().name("student"),
                model().attribute("isShowInactiveGroups", true),
                model().attribute("isShowInactiveStudents", true),
                model().attribute("students", students),
                model().attribute("groups", groups)
            ));
    }
}