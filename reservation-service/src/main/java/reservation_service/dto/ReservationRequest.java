package reservation_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRequest {

    @NotNull(message = "L'ID de la salle est obligatoire")
    private Long roomId;

    @NotNull(message = "L'ID du membre est obligatoire")
    private Long memberId;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDateTime;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDateTime;
}
