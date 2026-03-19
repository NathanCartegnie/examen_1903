package room_service.service;

import room_service.dto.RoomRequest;
import room_service.entity.Room;
import room_service.kafka.RoomKafkaProducer;
import room_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomKafkaProducer kafkaProducer;

    public Room createRoom(RoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setCity(request.getCity());
        room.setCapacity(request.getCapacity());
        room.setType(request.getType());
        room.setHourlyRate(request.getHourlyRate());
        room.setAvailable(true);
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle introuvable avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    public List<Room> getRoomsByCity(String city) {
        return roomRepository.findByCity(city);
    }

    @Transactional(readOnly = true)
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailable(true);
    }

    public Room updateRoom(Long id, RoomRequest request) {
        Room room = getRoomById(id);
        room.setName(request.getName());
        room.setCity(request.getCity());
        room.setCapacity(request.getCapacity());
        room.setType(request.getType());
        room.setHourlyRate(request.getHourlyRate());
        return roomRepository.save(room);
    }

    /**
     * Met à jour la disponibilité d'une salle.
     * Appelé par le consumer Kafka quand une réservation est créée ou terminée.
     */
    public void updateAvailability(Long roomId, boolean available) {
        Room room = getRoomById(roomId);
        room.setAvailable(available);
        roomRepository.save(room);
        log.info("Salle {} disponibilité mise à jour : {}", roomId, available);
    }

    /**
     * Supprime une salle et publie un événement Kafka pour annuler
     * toutes les réservations CONFIRMED associées.
     */
    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
        kafkaProducer.sendRoomDeletedEvent(id);
        log.info("Salle {} supprimée, événement Kafka publié", id);
    }
}
