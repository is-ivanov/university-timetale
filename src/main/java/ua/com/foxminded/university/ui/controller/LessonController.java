package ua.com.foxminded.university.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.com.foxminded.university.domain.entity.*;
import ua.com.foxminded.university.domain.filter.LessonFilter;
import ua.com.foxminded.university.domain.mapper.LessonDtoMapper;
import ua.com.foxminded.university.domain.service.interfaces.*;
import ua.com.foxminded.university.ui.restcontroller.LessonRestController;
import ua.com.foxminded.university.ui.util.MappingConstants;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static ua.com.foxminded.university.ui.util.ResponseUtil.defineRedirect;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(MappingConstants.LESSONS)
public class LessonController {

    public static final String GET_ALL_TEACHERS_FOR_SELECTOR = "Get all teachers for selector";
    public static final String LESSONS = "all_lessons";

    private final LessonService lessonService;
    private final FacultyService facultyService;
    private final TeacherService teacherService;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final GroupService groupService;
    private final LessonDtoMapper lessonDtoMapper;
    private final LessonRestController lessonRestController;

    @GetMapping
    public String showLessons(Model model) {
        model.addAttribute("lessonFilter", new LessonFilter());
        log.debug("The required data is loaded into the model. Loading page");
        return LESSONS;
    }

    @GetMapping("/filter")
    public String showFilteredLessons(@RequestParam(required = false) String isShowInactiveTeachers,
                                      @RequestParam(required = false) String isShowPastLessons,
                                      @ModelAttribute LessonFilter lessonFilter,
                                      Model model) {
        log.debug("Getting data for all_lessons.html with filter");
        if (isShowInactiveTeachers != null && isShowInactiveTeachers.equals("on")) {
            model.addAttribute("isShowInactiveTeachers", true);
        }
        if (isShowPastLessons != null && isShowPastLessons.equals("on")) {
            model.addAttribute("isShowPastLessons", true);
        }
        Integer departmentId = lessonFilter.getDepartmentId();
        Integer facultyId = lessonFilter.getFacultyId();
        List<Teacher> teachers = getTeachersByFacultyOrDepartment(departmentId, facultyId);
        model.addAttribute("teachers", teachers);
        model.addAttribute("departments", getDepartmentsByFaculty(facultyId));

        log.debug("Get filtered lessons");
        Iterable<Lesson> filteredLessons = lessonService.getAllWithFilter(lessonFilter);
        model.addAttribute("lessons",
            lessonDtoMapper.toDtos(filteredLessons));
        model.addAttribute("newLesson", new Lesson());
        log.debug("The required data is loaded into the model");
        return LESSONS;
    }

    @GetMapping("/{id}/students")
    public String showLessonWithStudents(@PathVariable("id") int lessonId,
                                         Model model) {
        log.debug("Getting data for lesson.html for lesson id({})", lessonId);
        Lesson lesson = lessonService.findById(lessonId);
        model.addAttribute("lesson", lessonDtoMapper.toDto(lesson));

        LocalDateTime timeStart = lesson.getTimeStart();
        LocalDateTime timeEnd = lesson.getTimeEnd();
        Teacher teacher = lesson.getTeacher();
        Room room = lesson.getRoom();

        log.debug("Loading free teachers in model");
        List<Teacher> freeTeachers = teacherService
            .getFreeTeachersOnLessonTime(timeStart, timeEnd);
        freeTeachers.add(teacher);
        model.addAttribute("teachers", freeTeachers);

        log.debug("Loading free rooms in model");
        List<Room> freeRooms = roomService
            .getFreeRoomsOnLessonTime(timeStart, timeEnd);
        freeRooms.add(room);
        model.addAttribute("rooms", freeRooms);

        log.debug("Loading active groups in model");
        model.addAttribute("groups",
            groupService.getFreeGroupsOnLessonTime(timeStart, timeEnd));

        log.debug("The required data is loaded into the model. Loading page lesson id({})",
            lessonId);
        return "lesson";
    }

    @PostMapping(MappingConstants.ID_STUDENTS)
    public String addStudentToLesson(@PathVariable("id") int lessonId,
                                     @RequestParam int studentId,
                                     HttpServletRequest request) {
        lessonRestController.addStudentToLesson(lessonId, studentId);
        return defineRedirect(request);
    }

    @PostMapping(MappingConstants.ID_GROUPS)
    public String addStudentsFromGroupToLesson(@PathVariable("id") int lessonId,
                                               @RequestParam int groupId,
                                               HttpServletRequest request) {
        lessonRestController.addStudentsFromGroupToLesson(lessonId, groupId);
        return defineRedirect(request);
    }

    @DeleteMapping("/{id}/students")
    public String removeStudentFromLesson(@PathVariable("id") int lessonId,
                                          @RequestParam Integer[] studentIds,
                                          HttpServletRequest request) {
        lessonRestController.removeStudentFromLesson(lessonId, studentIds);
        return defineRedirect(request);
    }

    @DeleteMapping("/{id}")
    public String deleteLesson(@PathVariable("id") int lessonId,
                               HttpServletRequest request) {
        log.debug("Deleting lesson id({})", lessonId);
        lessonService.delete(lessonId);
        log.debug("Lesson id({}) is deleted", lessonId);
        return defineRedirect(request);
    }

    @ModelAttribute("faculties")
    public List<Faculty> getFaculties() {
        log.debug("Loading list faculties for selector");
        return facultyService.getAllSortedByNameAsc();
    }

    @ModelAttribute("departments")
    public List<Department> getDepartments() {
        log.debug("Loading list departments for selector");
        return departmentService.findAll();
    }

    @ModelAttribute("teachers")
    public List<Teacher> getTeachers() {
        log.debug("Loading list teachers for selector");
        return teacherService.findAll();
    }

    @ModelAttribute("courses")
    public List<Course> getCourses() {
        log.debug("Loading list courses for selector");
        return courseService.findAll();
    }

    @ModelAttribute("rooms")
    public List<Room> getRooms() {
        log.debug("Loading list rooms for selector");
        return roomService.findAll();
    }

    private List<Department> getDepartmentsByFaculty(Integer facultyId) {
        if (facultyId != null && facultyId > 0) {
            log.debug("Get departments for selector by facultyId ({})", facultyId);
            return departmentService.getAllByFaculty(facultyId);
        } else {
            log.debug("Get all departments for selector");
            return departmentService.findAll();
        }
    }

    private List<Teacher> getTeachersByFacultyOrDepartment(Integer departmentId, Integer facultyId) {
        List<Teacher> teachers;
        if (departmentId != null && departmentId > 0) {
            log.debug("Get teachers for selector by departmentId ({})",
                departmentId);
            teachers = teacherService.getAllByDepartment(departmentId);
        } else if (facultyId != null && facultyId > 0) {
            log.debug("Get teachers for selector by facultyId ({})", facultyId);
            teachers = teacherService.getAllByFaculty(facultyId);
        } else {
            log.debug(GET_ALL_TEACHERS_FOR_SELECTOR);
            teachers = teacherService.findAll();
        }
        return teachers;
    }
}