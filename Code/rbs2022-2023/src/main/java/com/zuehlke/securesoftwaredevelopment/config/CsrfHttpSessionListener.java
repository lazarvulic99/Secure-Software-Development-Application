package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.controller.PersonsController;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@WebListener
public class CsrfHttpSessionListener implements HttpSessionListener {

    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CsrfHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String token = createToken();
        se.getSession().setAttribute("CSRF_TOKEN", token);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        auditLogger.auditDetailedDescription("Unistena sesija. Korisnik je izlogovan.");
    }

    private static String createToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        byte[] base64token = Base64.encodeBase64(token);
        return new String(base64token, StandardCharsets.UTF_8);
    }
}
