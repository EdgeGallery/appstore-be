package org.edgegallery.appstore.infrastructure.util;

import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.HostException;


public class IpCalculateUtil {

    private static final int PARSE_HOST_IP_INDEX = 3;

    private static final int PARSE_IP_BITS_FOUR = 4;

    private static final int PARSE_IP_RANGE = 250;

    private static final int PARSE_MASK_BITS_EIGHT = 8;

    private static final int PARSE_IP_NUMBER = 32;

    private static final int PARSE_IP_NETWORK_SEGMENT = 255;

    private static final int INT_CAPACITY = 4;

    private IpCalculateUtil() {

    }

    /**
     * Calculate the mask based on the number of mask bits.
     * @param maskIndex mask bit.
     * @return subnet mask
     */
    public static String getNetMask(String maskIndex) {
        StringBuilder mask = new StringBuilder();
        Integer inetMask = 0;
        try {
            inetMask = Integer.parseInt(maskIndex);
        } catch (NumberFormatException e) {
            throw new HostException("get internet mask error", ResponseConst.RET_GET_IP_NUMBER_ERROR);
        }
        if (inetMask > PARSE_IP_NUMBER) {
            return null;
        }
        // The subnet mask is 1 occupies a few bytes.
        int num1 = inetMask / PARSE_MASK_BITS_EIGHT;
        // The number of bits to fill in the subnet mask.
        int num2 = inetMask % PARSE_MASK_BITS_EIGHT;
        int[] array = new int[INT_CAPACITY];
        for (int i = 0; i < num1; i++) {
            array[i] = PARSE_IP_NETWORK_SEGMENT;
        }
        for (int i = num1; i < PARSE_IP_BITS_FOUR; i++) {
            array[i] = 0;
        }
        for (int i = 0; i < num2; num2--) {
            array[num1] += 1 << PARSE_MASK_BITS_EIGHT - num2;
        }
        for (int i = 0; i < PARSE_IP_BITS_FOUR; i++) {
            if (i == PARSE_HOST_IP_INDEX) {
                mask.append(array[i]);
            } else {
                mask.append(array[i] + ".");
            }
        }
        return mask.toString();
    }

    /**
     * Calculate the starting IP network segment format based on the network segment: x.x.x.x/x.
     * A network segment 0 is generally a network address, and 255 is generally a broadcast address.
     * Starting IP calculation: the IP address of the network segment and the mask plus one.
     * @param segment network segment.
     * @param range network range.
     * @return starting IP
     */
    public static String getStartIp(String segment, int range) {
        StringBuilder startIp = new StringBuilder();
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String ip = arr[0];
        String maskIndex = arr[1];
        String mask = IpCalculateUtil.getNetMask(maskIndex);
        if (PARSE_IP_BITS_FOUR != ip.split("\\.").length || mask == null) {
            return null;
        }
        int[] ipArray = new int[INT_CAPACITY];
        int[] netMaskArray = new int[INT_CAPACITY];
        for (int i = 0; i < PARSE_IP_BITS_FOUR; i++) {
            try {
                ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
                netMaskArray[i] = Integer.parseInt(mask.split("\\.")[i]);
                if (ipArray[i] > PARSE_IP_NETWORK_SEGMENT || ipArray[i] < 0
                    || netMaskArray[i] > PARSE_IP_NETWORK_SEGMENT || netMaskArray[i] < 0) {
                    return null;
                }
                ipArray[i] = ipArray[i] & netMaskArray[i];
                if (i == PARSE_HOST_IP_INDEX) {
                    startIp.append(ipArray[i] + range % PARSE_IP_RANGE + PARSE_HOST_IP_INDEX);
                } else {
                    startIp.append(ipArray[i] + ".");
                }
            } catch (NumberFormatException e) {
                throw new HostException("get start ip error", ResponseConst.RET_GET_IP_NUMBER_ERROR);
            }
        }
        return startIp.toString();
    }
}
