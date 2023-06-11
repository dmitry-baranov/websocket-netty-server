package org.example;

import org.example.customhandshake.CustomWebSocketHandler;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Logger;

public class Utils {
    private static final Logger logger = Logger.getLogger(CustomWebSocketHandler.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom();

    /*
    Генератор случайного BigInteger
     */
    public static String generateUniqueBigInteger() {
        long startTime = System.nanoTime();
        try {
            byte[] first = new byte[128];
            secureRandom.nextBytes(first);

            byte[] second = UUID.randomUUID().toString().getBytes();

            byte[] third = BigInteger.valueOf(System.nanoTime()).toByteArray();

            byte[] input = new byte[first.length + second.length + third.length];
            System.arraycopy(first, 0, input, 0, first.length);
            System.arraycopy(second, 0, input, first.length, second.length);
            System.arraycopy(third, 0, input, first.length + second.length, third.length);

            BigInteger result = new BigInteger(input);
            return "{ \"result\":" + result + "}";
        } finally {
            long endTime = System.nanoTime();
            logger.info("Time taken to generate Biginteger: " + (endTime - startTime));
        }
    }
}
