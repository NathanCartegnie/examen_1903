package reservation_service.controller;

import reservation_service.dto.ReservationRequest;
import reservation_service.entity.Reservation;
import reservation_service.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Gestion des réservations de salles")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Créer une réservation (vérifie dispo salle + quota membre)")
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les réservations")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une réservation par ID")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Lister les réservations d'un membre")
    public ResponseEntity<List<Reservation>> getReservationsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(reservationService.getReservationsByMember(memberId));
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Lister les réservations d'une salle")
    public ResponseEntity<List<Reservation>> getReservationsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(reservationService.getReservationsByRoom(roomId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une réservation (State Pattern : CONFIRMED → CANCELLED)")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marquer une réservation comme complétée (State Pattern : CONFIRMED → COMPLETED)")
    public ResponseEntity<Reservation> completeReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.completeReservation(id));
    }
}
