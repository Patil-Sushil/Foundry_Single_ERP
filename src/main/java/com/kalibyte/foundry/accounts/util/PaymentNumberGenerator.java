package com.kalibyte.foundry.accounts.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;

@Component
public class PaymentNumberGenerator {

    public String generate(){

        return "PAY-" + Year.now().getValue() + "-" +
                UUID.randomUUID().toString().substring(0,8);
    }
}
