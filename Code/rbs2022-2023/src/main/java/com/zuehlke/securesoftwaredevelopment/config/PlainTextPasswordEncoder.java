package com.zuehlke.securesoftwaredevelopment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PlainTextPasswordEncoder implements PasswordEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtil.class);

    @Override
    public String encode(CharSequence charSequence) {
        if(charSequence == null){
            LOG.error("String za enkodovanje je null!");
            return null;
        }else{
            return charSequence.toString();
        }
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        if(charSequence == null || s == null){
            if(charSequence == null){
                LOG.error("CharSequence za uporedjivanje je null!");
                return false;
            }else{
                LOG.error("String za uporedjivanje je null!");
                return false;
            }
        }else{
            return charSequence.toString().equals(s);
        }
    }
}
