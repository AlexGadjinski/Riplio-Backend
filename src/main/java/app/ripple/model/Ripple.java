package app.ripple.model;

import java.time.LocalDateTime;

public interface Ripple {

    RippleType getType();

    void setType(RippleType type);

    void setUpdatedOn(LocalDateTime updatedOn);
}
