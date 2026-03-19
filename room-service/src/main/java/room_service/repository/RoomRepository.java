package room_service.repository;

import room_service.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByCity(String city);
    List<Room> findByAvailable(boolean available);
    List<Room> findByCityAndAvailable(String city, boolean available);
}
