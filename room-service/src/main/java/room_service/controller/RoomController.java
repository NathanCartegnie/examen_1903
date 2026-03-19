package room_service.controller;

import room_service.dto.RoomRequest;
import room_service.entity.Room;
import room_service.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "Gestion des salles de coworking")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle salle")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les salles")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une salle par ID")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/available")
    @Operation(summary = "Lister les salles disponibles")
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Lister les salles par ville")
    public ResponseEntity<List<Room>> getRoomsByCity(@PathVariable String city) {
        return ResponseEntity.ok(roomService.getRoomsByCity(city));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une salle")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une salle (annule les réservations via Kafka)")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint interne appelé par le Reservation Service pour vérifier la disponibilité.
     */
    @GetMapping("/{id}/available")
    @Operation(summary = "Vérifier si une salle est disponible")
    public ResponseEntity<Boolean> isRoomAvailable(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room.isAvailable());
    }
}
