package nl.han.asd.submarine.controllers;

import nl.han.asd.submarine.controllers.dto.ChatterAddressDTO;
import nl.han.asd.submarine.controllers.dto.ChatterLoginDTO;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import nl.han.asd.submarine.services.ChatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/chatter")
public class ChatterController {
    private static final Logger LOG = Logger.getLogger(ChatterController.class.getName());

    @Autowired
    private ChatterService chatterServiceImpl;


    public ChatterController() {
        LOG.setLevel(Level.INFO);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void validateAndCreateChatter(@RequestBody ChatterRegistrationDTO chatterRegistrationDTO) {
        LOG.log(Level.INFO, "Received request on /chatter/create");
        chatterServiceImpl.validateAndCreateChatter(chatterRegistrationDTO);
    }

    @PutMapping("/login")
    public void loginChatter(@RequestBody ChatterLoginDTO chatterLoginDTO) {
        LOG.log(Level.INFO, "Received request on /chatter/login");
        chatterServiceImpl.loginChatter(chatterLoginDTO);
    }

    @GetMapping("/get-ip/{alias}")
    public ChatterAddressDTO getChatterIP(@PathVariable("alias") String alias) {
        LOG.log(Level.INFO, "Received request on /chatter/get-ip/");
        return new ChatterAddressDTO(chatterServiceImpl.getChatterIpByAlias(alias));
    }

}
