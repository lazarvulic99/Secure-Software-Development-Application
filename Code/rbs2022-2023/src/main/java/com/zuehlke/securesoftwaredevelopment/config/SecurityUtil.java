package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class SecurityUtil {

    private SecurityUtil() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtil.class);

    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            if(permission != null){
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                if(authorities != null){
                    boolean authorized = authorities.contains(new SimpleGrantedAuthority(permission));
                    return authorized;
                }else{
                    LOG.error("Nemate ovlascenja!");
                    return false;
                }
            }else{
                LOG.error("Nemate permisiju!");
                return false;
            }
        }else{
            LOG.error("Niste autentifikovani!");
            return false;
        }
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if(principal == null){
                LOG.warn("Principal nije dohvacen!");
                return null;
            }else{
                return (User)principal;
            }
        }else {
            LOG.warn("Autentifikacija neuspesna!");
            return null;
        }
    }
}
