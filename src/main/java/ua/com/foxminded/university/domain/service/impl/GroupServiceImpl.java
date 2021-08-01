package ua.com.foxminded.university.domain.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.foxminded.university.dao.interfaces.GroupDao;
import ua.com.foxminded.university.dao.interfaces.StudentDao;
import ua.com.foxminded.university.domain.entity.Faculty;
import ua.com.foxminded.university.domain.entity.Group;
import ua.com.foxminded.university.domain.service.interfaces.GroupService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    private final GroupDao groupDao;
    private final StudentDao studentDao;

    @Autowired
    public GroupServiceImpl(GroupDao groupDao, StudentDao studentDao) {
        this.groupDao = groupDao;
        this.studentDao = studentDao;
    }

    @Override
    public void add(Group group) {
        log.debug("Adding {}", group);
        groupDao.add(group);
        log.info("{} added successfully", group);
    }

    @Override
    public Group getById(int id) {
        log.debug("Getting group by id({})", id);
        Group group = groupDao.getById(id).orElse(new Group());
        log.info("Found {}", group);
        return group;
    }

    @Override
    public List<Group> getAll() {
        log.debug("Getting all groups");
        List<Group> groups = groupDao.getAll();
        log.info("Found {} groups", groups.size());
        return groups;
    }

    @Override
    public void update(Group group) {
        log.debug("Updating {}", group);
        groupDao.update(group);
        log.info("Update {}", group);
    }

    @Override
    public void delete(Group group) {
        log.debug("Deleting {}", group);
        groupDao.delete(group);
        log.info("Delete {}", group);
    }

    @Override
    public void deactivateGroup(Group group) {
        log.debug("Deactivating {}", group);
        group.setActive(false);
        groupDao.update(group);
        log.info("Deactivate {}", group);
    }

    @Override
    public Group joinGroups(List<Group> groups, String nameNewGroup,
                            Faculty facultyNewGroup) {
        String groupsId = Arrays.toString(groups.stream()
            .map(Group::getId)
            .toArray());
        log.debug("Joining groups with id{}", groupsId);
        Group newGroup = new Group();
        newGroup.setName(nameNewGroup);
        newGroup.setFaculty(facultyNewGroup);
        newGroup.setActive(true);
        log.debug("Saving new {} in DB", newGroup);
        groupDao.add(newGroup);
        log.debug("Replace all students from groups id ({}) into new {}",
            groupsId, newGroup);
        groups.forEach(group -> {
            studentDao.getStudentsByGroup(group).forEach(student -> {
                student.setGroup(newGroup);
                studentDao.update(student);
            });
            log.debug("Deactivating former {}", group);
            deactivateGroup(group);
        });
        log.info("Create new {}", newGroup);
        return newGroup;
    }

    @Override //TODO sort groups
    public List<Group> getAllByFacultyId(int facultyId) {
        log.debug("Getting all groups by faculty id({})", facultyId);
        List<Group> groups = groupDao.getAllByFacultyId(facultyId);
        log.info("Found {} groups", groups.size());
        return groups;
    }
}