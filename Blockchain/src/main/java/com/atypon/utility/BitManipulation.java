package com.atypon.utility;

/**
 * A utility class to help deal with bits and strings operations.
 */
public final class BitManipulation {
    private final static char[] INT_TO_HEX =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Convert the byte into unsigned by ANDing it with '...00 1111 1111'.
     *
     * @param b The byte to convert.
     * @return The byte in an unsigned form of integer.
     */
    private static int toUnsignedByte(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert the byte into a hexadecimal string of 2 characters.
     *
     * @param b The byte to convert.
     * @return The byte in a hexadecimal string form.
     */
    private static String byteToString(byte b) {
        int unsignedByte = toUnsignedByte(b);
        return INT_TO_HEX[unsignedByte >> 4] + "" + INT_TO_HEX[unsignedByte % 16];
    }

    /**
     * Convert a byte array into a hexadecimal (lowercase) string of 2 characters for each byte.
     *
     * @param byteArray The byte array to convert.
     * @return The byte array in a hexadecimal string form.
     */
    public static String byteArrayToString(byte[] byteArray) {
        StringBuilder byteString = new StringBuilder();
        for (byte thisByte : byteArray)
            byteString.append(byteToString(thisByte));
        return byteString.toString();
    }

    /**
     * Convert a hexadecimal (lowercase) string into a byte array
     *
     * @param byteString The string to convert.
     * @return The string as a byte array.
     * @throws IndexOutOfBoundsException if the string length is odd.
     */
    public static byte[] stringToByteArray(String byteString) {
        byte[] byteArray = new byte[byteString.length() >> 1];
        for (int i = 0; i < byteString.length(); i += 2) {
            byteArray[i >> 1] = (byte) ((Character.digit(byteString.charAt(i), 16) << 4)
                    + Character.digit(byteString.charAt(i + 1), 16));
        }
        return byteArray;
    }

    /**
     * Gets the first 'n' bits in some hexadecimal string.
     *
     * @param n      The number of bits.
     * @param string The string to process.
     * @return The first 'n' bits of the string.
     */
    public static String getFirstBits(int n, String string) {
        StringBuilder firstBits = new StringBuilder();

        for (int i = 0; firstBits.length() < n && i < string.length(); ++i) {
            // Convert the character to a byte
            int thisByte = Character.digit(string.charAt(i), 16);
            // Append each bit one by one
            firstBits.append((thisByte >> 3) & 1 + '0');
            firstBits.append((thisByte >> 2) & 1 + '0');
            firstBits.append((thisByte >> 1) & 1 + '0');
            firstBits.append(thisByte & 1 + '0');
        }
        firstBits.setLength(n);
        return firstBits.toString();
    }

    /**
     * Generate a string of length 'n' consisting of zeros.
     * Works only if n <= 256.
     *
     * @param n The number of bits.
     * @return A string of length 'n' consisting of zeros.
     */
    public static String getLeadingZeros(int n) {
        return BitManipulation.getFirstBits(n,
                "0000000000000000000000000000000000000000000000000000000000000000");
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private BitManipulation() {
    }
}
