package app.ripple.dto;

import app.ripple.model.RippleType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RippleResponse {

    private RippleType type;
    private int score;
}
