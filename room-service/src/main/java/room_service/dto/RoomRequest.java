package room_service.dto;

import room_service.entity.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer capacity;

    @NotNull(message = "Le type est obligatoire")
    private RoomType type;

    @NotNull(message = "Le tarif horaire est obligatoire")
    @Min(value = 0, message = "Le tarif doit être positif")
    private BigDecimal hourlyRate;
}
