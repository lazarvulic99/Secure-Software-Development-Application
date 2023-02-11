package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditLogger {
    public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    private final Logger LOG;
    private Integer id;

    public static AuditLogger getAuditLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new AuditLogger(logger);
    }

    private AuditLogger(Logger log) {
        this.LOG = log;
    }

    public void audit(String description) {
        Integer id = getId();
        LOG.info(AUDIT, "userId={} - {}", id, description);
    }

    public void auditDetailedDescriptionWithId(int userId, String opis){
        LOG.info(AUDIT, "userId={} - {}", userId, opis);
    }

    public void auditDetailedDescription(String opis){
        Integer id = DatabaseAuthenticationProvider.idUser;
        LOG.info(AUDIT, "userId={} - {}", id, opis);
    }

    public void auditChange(Entity entity) {
        audit("Change " + entity.toString());
    }

    private Integer getId() {
        if (id == null) {
            User loggedUser = SecurityUtil.getCurrentUser();
            if (loggedUser != null) {
                id = loggedUser.getId();
            }else{
                LOG.info("Ne postoji ulogovani korisnik!");
            }
        }
        return id;
    }
}
