package com.kalibyte.foundry.furnace.furnace_heats.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MetalBalanceValidator.class)
@Documented
public @interface ValidMetalBalance {
    String message() default "Metal balance validation failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
