package nl.han.asd.submarine.services;

import nl.han.asd.submarine.exceptions.AliasOrUsernameAlreadyExistException;
import nl.han.asd.submarine.exceptions.CouldNotFindUserByAliasException;
import nl.han.asd.submarine.exceptions.InvalidUsernameOrPasswordException;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.controllers.dto.ChatterLoginDTO;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import nl.han.asd.submarine.repositories.ChatterRepository;
import nl.han.asd.submarine.util.FieldChecker;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ChatterServiceImpl implements ChatterService {

    @Autowired
    private ChatterRepository collection;

    @Autowired
    private ChatterAuthenticationService chatterAuthenticationService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void validateAndCreateChatter(ChatterRegistrationDTO chatterRegistrationDTO) {
        if (!FieldChecker.areFieldsNotBlankOrNull(new String[]{"username", "password", "ipAddress", "alias", "publicKey"}, chatterRegistrationDTO))
            throw new IllegalArgumentException("Not all fields are filled in. Expected:\n" +
                    "{\n" +
                    "\t\"username\": (String),\n" +
                    "\t\"password\": (String),\n" +
                    "\t\"ipAddress\": (String),\n" +
                    "\t\"alias\": (String),\n" +
                    "\t\"publicKey\": (String)\n" +
                    "}");

        Chatter chatter = modelMapper.map(chatterRegistrationDTO, Chatter.class);
        chatterAuthenticationService.updatePassword(chatter, chatterRegistrationDTO.getPassword());
        validateChatter(chatter);
        collection.save(chatter);
    }

    @Override
    public void loginChatter(ChatterLoginDTO chatterLoginDTO) {
        if (!FieldChecker.areFieldsNotBlankOrNull(new String[]{"username", "password", "ipAddress"}, chatterLoginDTO))
            throw new IllegalArgumentException("Not all fields are filled in. Expected:\n" +
                    "{\n" +
                    "\t\"username\": (String),\n" +
                    "\t\"password\": (String),\n" +
                    "\t\"ipAddress\": (String)\n" +
                    "}");

        Chatter storedChatter = collection.findDistinctByUsername(chatterLoginDTO.getUsername());

        if (!isValidIpAddress(chatterLoginDTO.getIpAddress()))
            throw new IllegalArgumentException("The given IP address is neither a valid IPv4 nor a IPv6 address");

        String plainTextPassword = chatterLoginDTO.getPassword();
        if (storedChatter == null || !chatterAuthenticationService.chatterIsAuthentic(storedChatter, plainTextPassword)) {
            throw new InvalidUsernameOrPasswordException();
        }

        if (!storedChatter.getIpAddress().equals(chatterLoginDTO.getIpAddress())) {
            storedChatter.setIpAddress(chatterLoginDTO.getIpAddress());
            collection.save(storedChatter);
        }
    }

    private boolean isValidIpAddress(String ipAdress) {
        // Check if the ip adress could even be a valid ip address, not just a random string
        InetAddressValidator validator = new InetAddressValidator();
        if (!validator.isValid(ipAdress)) {
            return false;
        }

        return validator.isValidInet4Address(ipAdress) ? isValidIpv4Address(ipAdress) : isValidIpv6Address(ipAdress);
    }

    private boolean isValidIpv4Address(String ipv4Address) {
        // Check if the ip address isn't a loopback address
        if (ipv4Address.matches("^127\\..*")) {
            return false;
        }
        // Check if the ip address isn't a private network ip address
        return !ipv4Address.matches("^(172\\.((1[6-9])|(2[0-9])|(3[0-1]))\\..*)|(192\\.168\\..*)|(10\\..*)");
    }

    private boolean isValidIpv6Address(String ipv6Adress) {
        // Check if the ip address isn't a loopback address
        if (ipv6Adress.matches("^::1")) {
            return false;
        }
        // Check if the ip address isn't a private network ip address
        return !ipv6Adress.matches("^(fc[0-9a-f][0-9a-f]:.*)");
    }

    @Override
    public String getChatterIpByAlias(String alias) {
        Chatter result = collection.findIpAddressByAlias(alias);
        if (result == null) throw new CouldNotFindUserByAliasException();
        return result.getIpAddress();
    }

    private void validateChatter(Chatter chatter) {
        if (collection.existsByAliasOrUsername(chatter.getAlias(), chatter.getUsername())) {
            throw new AliasOrUsernameAlreadyExistException();
        }
    }

}
