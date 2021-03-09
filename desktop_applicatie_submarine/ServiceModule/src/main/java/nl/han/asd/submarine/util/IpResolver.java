package nl.han.asd.submarine.util;

import nl.han.asd.submarine.exception.IpResolveException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class IpResolver {

    private IpResolver() {
    }

    public static String getHostIp() {
        try (BufferedReader sc = new BufferedReader(new InputStreamReader(new URL("http://bot.whatismyipaddress.com").openStream()))) {
            return sc.readLine().trim();
        } catch (Exception e) {
            throw new IpResolveException(e.getMessage(), e);
        }
    }
}
