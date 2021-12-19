package ua.com.foxminded.university.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.com.foxminded.university.domain.entity.Teacher;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    List<Teacher> findAllByDepartmentId(int departmentId);

    List<Teacher> findByDepartment_Faculty_IdIs(int facultyId);


//    @Query("SELECT t FROM Teacher t " +
//        "WHERE t.active = TRUE " +
//        "AND t.id NOT IN " +
//        "( " +
//        "SELECT t2.id " +
//        "FROM Teacher t2 " +
//        "LEFT JOIN Lesson l ON t2 = l.teacher " +
//        "WHERE l.timeEnd >= :startTime " +
//        "AND l.timeStart <= :endTime " +
//        ") " +
//        "ORDER BY t.lastName, t.firstName")
//    List<Teacher> findFreeTeachersOnLessonTime(LocalDateTime startTime,
//                                               LocalDateTime endTime);

    @Query("SELECT t " +
        "FROM Teacher t " +
        "LEFT JOIN Lesson l ON t = l.teacher " +
        "WHERE l.timeEnd >= :from " +
        "AND l.timeStart <= :to ")
    List<Teacher> findBusyTeachersOnTime(LocalDateTime from, LocalDateTime to);

    List<Teacher> findByActiveIsTrueAndIdNotInOrderByLastNameAscFirstNameAsc(Collection<Integer> ids);


}
