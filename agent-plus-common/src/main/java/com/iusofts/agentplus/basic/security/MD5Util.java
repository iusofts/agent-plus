package com.iusofts.agentplus.basic.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Random;

public class MD5Util {

    /**
     * Used by the hash method.
     */
    private static MessageDigest MD5_DIGEST;

    static {
        try {
            MD5_DIGEST = MessageDigest.getInstance(MD5StringUtils.MD5);
        }
        catch (NoSuchAlgorithmException e) {
            // Smack wont be able to function normally if this exception is thrown, wrap it into
            // an ISE and make the user aware of the problem.
            throw new IllegalStateException(e);
        }
    }

    public static synchronized byte[] bytes(byte[] bytes) {
        return MD5_DIGEST.digest(bytes);
    }

    public static byte[] bytes(String string) {
        return bytes(MD5StringUtils.toBytes(string));
    }

    public static String hex(byte[] bytes) {
        return MD5StringUtils.encodeHex(bytes(bytes));
    }

    public static String hex(String string) {
        return hex(MD5StringUtils.toBytes(string));
    }

    public static class MD5StringUtils {
        public static final String MD5 = "MD5";
        public static final String SHA1 = "SHA-1";
        public static final String UTF8 = "UTF-8";

        public static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

        /**
         * Encodes an array of bytes as String representation of hexadecimal.
         *
         * @param bytes an array of bytes to convert to a hex string.
         * @return generated hex string.
         */
        public static String encodeHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for ( int j = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_CHARS[v >>> 4];
                hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
            }
            return new String(hexChars);
        }

        public static byte[] toBytes(String string) {
            try {
                return string.getBytes(MD5StringUtils.UTF8);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 encoding not supported by platform", e);
            }
        }

        /**
         * Pseudo-random number generator object for use with randomString().
         * The Random class is not considered to be cryptographically secure, so
         * only use these random Strings for low to medium security applications.
         */
        private static Random randGen = new Random();

        /**
         * Array of numbers and letters of mixed case. Numbers appear in the list
         * twice so that there is a more equal chance that a number will be picked.
         * We can use the array to get a random number or letter by picking a random
         * array index.
         */
        private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" +
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();

        /**
         * Returns a random String of numbers and letters (lower and upper case)
         * of the specified length. The method uses the Random class that is
         * built-in to Java which is suitable for low to medium grade security uses.
         * This means that the output is only pseudo random, i.e., each number is
         * mathematically generated so is not truly random.<p>
         *
         * The specified length must be at least one. If not, the method will return
         * null.
         *
         * @param length the desired length of the random String to return.
         * @return a random String of numbers and letters of the specified length.
         */
        public static String randomString(int length) {
            if (length < 1) {
                return null;
            }
            // Create a char buffer to put random letters and numbers in.
            char [] randBuffer = new char[length];
            for (int i=0; i<randBuffer.length; i++) {
                randBuffer[i] = numbersAndLetters[randGen.nextInt(numbersAndLetters.length)];
            }
            return new String(randBuffer);
        }

        /**
         * Returns true if CharSequence is not null and is not empty, false otherwise
         * Examples:
         *    isNotEmpty(null) - false
         *    isNotEmpty("") - false
         *    isNotEmpty(" ") - true
         *    isNotEmpty("empty") - true
         *
         * @param cs checked CharSequence
         * @return true if string is not null and is not empty, false otherwise
         */
        public static boolean isNotEmpty(CharSequence cs) {
            return !isNullOrEmpty(cs);
        }

        /**
         * Returns true if the given CharSequence is null or empty.
         *
         * @param cs
         * @return true if the given CharSequence is null or empty
         */
        public static boolean isNullOrEmpty(CharSequence cs) {
            return cs == null || isEmpty(cs);
        }

        /**
         * Returns true if the given CharSequence is empty
         *
         * @param cs
         * @return true if the given CharSequence is empty
         */
        public static boolean isEmpty(CharSequence cs) {
            return (cs == null || cs.length() == 0);
        }

        public static String collectionToString(Collection<String> collection) {
            StringBuilder sb = new StringBuilder();
            for (String s : collection) {
                sb.append(s);
                sb.append(" ");
            }
            String res = sb.toString();
            // Remove the trailing whitespace
            res = res.substring(0, res.length() - 1);
            return res;
        }
    }

    public static void main(String[] args) {
        System.err.println(hex("123456"));
    }

}
