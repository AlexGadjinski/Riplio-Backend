package app.ripple.dto;

import app.ripple.model.RippleType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RippleRequest {

    @NotNull(message = "Type is required.")
    private RippleType type;
}
