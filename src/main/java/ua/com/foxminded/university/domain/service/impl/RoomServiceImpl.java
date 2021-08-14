package ua.com.foxminded.university.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.foxminded.university.dao.interfaces.RoomDao;
import ua.com.foxminded.university.domain.entity.Room;
import ua.com.foxminded.university.domain.service.interfaces.RoomService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomDao roomDao;

    @Override
    public void add(Room room) {
        log.debug("Adding {}", room);
        roomDao.add(room);
        log.info("{} added successfully", room);
    }

    @Override
    public Room getById(int id) {
        log.debug("Getting room by id({})", id);
        Room room = roomDao.getById(id).orElse(new Room());
        log.info("Found {}", room);
        return room;
    }

    @Override
    public List<Room> getAll() {
        log.debug("Getting all rooms");
        List<Room> rooms = roomDao.getAll();
        log.info("Found {} rooms", rooms.size());
        return rooms;
    }

    @Override
    public void update(Room room) {
        log.debug("Updating {}", room);
        roomDao.update(room);
        log.info("Update {}", room);
    }

    @Override
    public void delete(Room room) {
        log.debug("Deleting {}", room);
        roomDao.delete(room);
        log.info("Delete {}", room);
    }

}