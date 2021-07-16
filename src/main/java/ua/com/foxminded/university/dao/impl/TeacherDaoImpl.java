package ua.com.foxminded.university.dao.impl;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.university.dao.interfaces.TeacherDao;
import ua.com.foxminded.university.domain.entity.Teacher;
import ua.com.foxminded.university.domain.entity.mapper.TeacherMapper;
import ua.com.foxminded.university.exception.DAOException;

@Slf4j
@Repository
@PropertySource("classpath:sql_query.properties")
public class TeacherDaoImpl implements TeacherDao {

    private static final String QUERY_ADD = "teacher.add";
    private static final String QUERY_GET_ALL = "teacher.getAll";
    private static final String QUERY_GET_BY_ID = "teacher.getById";
    private static final String QUERY_UPDATE = "teacher.update";
    private static final String QUERY_DELETE = "teacher.delete";
    private static final String MESSAGE_TEACHER_NOT_FOUND = "Teacher id(%d) not " +
        "found";
    private static final String MESSAGE_UPDATE_TEACHER_NOT_FOUND = "Can't " +
        "update because teacher id(%d) not found";
    private static final String MESSAGE_DELETE_TEACHER_NOT_FOUND = "Can't " +
        "delete because teacher id(%d) not found";

    private final JdbcTemplate jdbcTemplate;
    private final Environment env;

    @Autowired
    public TeacherDaoImpl(JdbcTemplate jdbcTemplate, Environment env) {
        this.jdbcTemplate = jdbcTemplate;
        this.env = env;
    }

    @Override
    public void add(Teacher teacher) {
        log.debug("Adding teacher [{} {} {}, active={}, department {}]",
            teacher.getFirstName(), teacher.getPatronymic(),
            teacher.getLastName(), teacher.isActive(),
            teacher.getDepartment().getName());
        try {
            jdbcTemplate.update(env.getRequiredProperty(QUERY_ADD),
                teacher.getFirstName(), teacher.getLastName(),
                teacher.getPatronymic(), teacher.isActive(),
                teacher.getDepartment().getId());
        } catch (DataAccessException e) {
            log.error("An error occurred while adding the {}", teacher, e);
            throw new DAOException(e.getMessage(), e);
        }
        log.info("Teacher [{} {} {}, active={}, department {}] added " +
                "successfully", teacher.getFirstName(), teacher.getPatronymic(),
            teacher.getLastName(), teacher.isActive(),
            teacher.getDepartment().getName());
    }

    @Override
    public Optional<Teacher> getById(int id) {
        log.debug("Getting teacher by id({})", id);
        Teacher result;
        try {
            result = jdbcTemplate.queryForObject(
                env.getRequiredProperty(QUERY_GET_BY_ID),
                new TeacherMapper(), id);
        } catch (DataAccessException e) {
            log.error("Teacher id({}) not found", id, e);
            throw new DAOException(String.format(MESSAGE_TEACHER_NOT_FOUND, id),
                e);
        }
        log.info("Found {}", result);
        return Optional.ofNullable(result);
    }

    @Override
    public List<Teacher> getAll() {
        log.debug("Getting all teachers");
        List<Teacher> teachers = jdbcTemplate.query(
            env.getRequiredProperty(QUERY_GET_ALL), new TeacherMapper());
        log.info("Found {} teachers", teachers.size());
        return teachers;
    }

    @Override
    public void update(Teacher teacher) {
        log.debug("Updating teacher [id={}, {} {} {}, active={}]",
            teacher.getId(), teacher.getFirstName(), teacher.getPatronymic(),
            teacher.getLastName(), teacher.isActive());
        int numberUpdatedRows = jdbcTemplate.update(
            env.getRequiredProperty(QUERY_UPDATE),
            teacher.getFirstName(), teacher.getLastName(),
            teacher.getPatronymic(), teacher.isActive(),
            teacher.getDepartment().getId(), teacher.getId());
        if (numberUpdatedRows == 0) {
            log.warn("Can't update teacher id({})", teacher.getId());
            throw new DAOException(String.format(MESSAGE_UPDATE_TEACHER_NOT_FOUND,
                teacher.getId()));
        } else {
            log.info("Update teacher id({})", teacher.getId());
        }
    }

    @Override
    public void delete(Teacher teacher) {
        log.debug("Deleting teacher [id={}, {} {} {}, active={}]",
            teacher.getId(), teacher.getFirstName(), teacher.getPatronymic(),
            teacher.getLastName(), teacher.isActive());
        int numberDeletingRows = jdbcTemplate.update(
            env.getRequiredProperty(QUERY_DELETE), teacher.getId());
        if (numberDeletingRows == 0) {
            log.warn("Can't delete teacher id({})", teacher.getId());
            throw new DAOException(String.format(MESSAGE_DELETE_TEACHER_NOT_FOUND,
                teacher.getId()));
        } else {
            log.info("Delete teacher id({})", teacher.getId());
        }
    }

}
