package com.diploma.server.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    private final CodeVerifier verifier;

    public TotpService() {
        this.verifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(), new SystemTimeProvider());
    }

    public boolean verify(String secret, String code) {
        try { return verifier.isValidCode(secret, code); }
        catch (Exception e) { return false; }
    }
}
