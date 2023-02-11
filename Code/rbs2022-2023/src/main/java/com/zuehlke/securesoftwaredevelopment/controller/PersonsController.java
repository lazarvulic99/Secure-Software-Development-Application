package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.CsrfHttpSessionListener;
import com.zuehlke.securesoftwaredevelopment.config.DatabaseAuthenticationProvider;
import com.zuehlke.securesoftwaredevelopment.config.SecurityUtil;
import com.zuehlke.securesoftwaredevelopment.domain.Permission;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.Role;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.PersonRepository;
import com.zuehlke.securesoftwaredevelopment.repository.RoleRepository;
import com.zuehlke.securesoftwaredevelopment.repository.UserRepository;
import com.zuehlke.securesoftwaredevelopment.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.UsesSunMisc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.List;

@Controller

public class PersonsController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonsController.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(PersonRepository.class);
    private final PersonRepository personRepository;
    private final UserRepository userRepository;


    public PersonsController(PersonRepository personRepository, UserRepository userRepository) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/persons/{id}")
    @PreAuthorize("hasAuthority('VIEW_PERSON') or hasAuthority('VIEW_SELF_PERSONS_PAGE')")
    public String person(@PathVariable int id, Model model, HttpSession session) {
        // Korisnik sa Id = 1 sme da vidi persons/1, ali ne i persons/2, persons/3... dok Admin sme da vidi sve ostale korisnike
        // Dodao sam linije koda 52-54 i or deo u PreAuthorize na 48
        if(SecurityUtil.hasPermission("VIEW_SELF_PERSONS_PAGE") && id != SecurityUtil.getCurrentUser().getId()){
            throw new AccessDeniedException("Mozes da vidis samo svoju persons/Id stranicu!");
        }
        User user = SecurityUtil.getCurrentUser();
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        model.addAttribute("CSRF_TOKEN", session.getAttribute("CSRF_TOKEN"));
        model.addAttribute("person", personRepository.get("" + id));
        return "person";
    }

    /*@GetMapping("/personsOne/{id}")
    @PreAuthorize("hasAuthority('VIEW_PERSON_SELF')")
    public String personOne(@PathVariable int id, Model model, HttpSession session) {
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfManually)){
            LOG.error("CSRF tokeni se ne poklapaju!");
            throw new AccessDeniedException("Forbidden!");
        }
        model.addAttribute("CSRF_TOKEN", session.getAttribute("CSRF_TOKEN"));
        model.addAttribute("person", personRepository.get("" + id));
        return "person";
    }*/

    @GetMapping("/myprofile")
    @PreAuthorize("hasAuthority('VIEW_MY_PROFILE')")
    public String self(Model model, Authentication authentication, HttpSession session) {
        //LOG.info("Glecam svoj licni profil!");
        User user = (User) authentication.getPrincipal();
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        model.addAttribute("CSRF_TOKEN", session.getAttribute("CSRF_TOKEN"));
        model.addAttribute("person", personRepository.get("" + user.getId()));
        return "person";
    }

    @DeleteMapping("/persons/{id}")
    public String person(@PathVariable int id, HttpSession session, @RequestParam("csrfToken") String csrfToken) {
        //Prvo sam stavio: @PreAuthorize("hasAuthority('DELETE_PERSON')")
        //Al ne moze tako jer onda samo Admin moze da brise, a treba i svako sebe da moze da brise
        boolean adminYesNo = SecurityUtil.hasPermission("DELETE_PERSON");
        if(adminYesNo == false){
            //Provera da li korisnik brise samog sebe
            int currId = SecurityUtil.getCurrentUser().getId();
            if(currId != id){
                throw new AccessDeniedException("Nedozvoljeno brisanje korisnika koji niste vi!");
            }
        }
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfToken)){
            LOG.error("Ne poklapaju se tokeni pri brisanju osobe!");
            throw new AccessDeniedException("Forbidden!");
        }else{
            LOG.info("Poklopili su se CSRF tokeni pri brisanju osobe!");
        }

        personRepository.delete(id);
        userRepository.delete(id);

        boolean briseSebe = SecurityUtil.getCurrentUser().getId() == id ? true : false;
        if (briseSebe != true) {
            return "redirect:/persons";
        } else {
            return "redirect:/logout";
        }
    }

    @PostMapping("/update-person")
    public String updatePerson(Person person, HttpSession session, @RequestParam("csrfToken") String csrfToken) throws AccessDeniedException{
        //Prvo sam stavio: @PreAuthorize("hasAuthority('UPDATE_PERSON')")
        //Al ne moze tako jer onda samo Admin moze da azurira licne podatke, a treba i svako sebe da moze da azurira
        User loggedUser = (User) SecurityUtil.getCurrentUser();
        int id = loggedUser.getId();
        boolean adminYesNo = SecurityUtil.hasPermission("UPDATE_PERSON");
        if(adminYesNo == false){
            //Provera da li korisnik azurira samog sebe
            int currId = Integer.parseInt(person.getId());
            if(currId != id){
                throw new AccessDeniedException("Nedozvoljeno brisanje korisnika koji niste vi!");
            }
        }
        String csrf = session.getAttribute("CSRF_TOKEN").toString();
        if(!csrf.equals(csrfToken)){
            LOG.error("CSRF tokeni se ne poklapaju!");
            throw new AccessDeniedException("Forbidden!");
        }else{
            LOG.info("CSRF tokeni u updatePerson se poklapaju!");
        }
        personRepository.update(person);
        //return "redirect:/personsOne/" + person.getId();
        boolean imaPravoNaPregledDetaljaOsobe = SecurityUtil.hasPermission("VIEW_PERSON") == true ? true : false;
        if (imaPravoNaPregledDetaljaOsobe == true) {
            return "redirect:/persons/" + person.getId();
        } else {
            return "redirect:/myprofile";
        }
    }

    @GetMapping("/persons")
    @PreAuthorize("hasAuthority('VIEW_PERSONS_LIST')")
    public String persons(Model model) {
        model.addAttribute("persons", personRepository.getAll());
        return "persons";
    }

    @GetMapping(value = "/persons/search", produces = "application/json")
    @ResponseBody
    @PreAuthorize("hasAuthority('VIEW_PERSONS_LIST')")
    public List<Person> searchPersons(@RequestParam String searchTerm) throws SQLException {
        return personRepository.search(searchTerm);
    }
}
