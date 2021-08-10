package org.edgegallery.appstore.infrastructure.util;

import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.HostException;


public class IpCalculateUtil {

    private static final int PARSE_SUBNETWORK_INDEX = 2;

    private static final int PARSE_HOST_IP_INDEX = 3;

    private static final int PARSE_IP_BITS_FOUR = 4;

    private static final int PARSE_IP_RANGE = 250;

    private static final int PARSE_MASK_BITS_EIGHT = 8;

    private static final int PARSE_IP_NUMBER = 32;

    private static final int PARSE_IP_NETWORK_SEGMENT = 255;

    private static final int PARSE_IP_SUBNETWORK_SEGMENT = 256;

    private static final int INT_CAPACITY = 4;

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
            throw new HostException("get ip number error", ResponseConst.RET_GET_IP_NUMBER_ERROR);
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
        StringBuffer startIp = new StringBuffer();
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
                throw new HostException("get ip number error", ResponseConst.RET_GET_IP_NUMBER_ERROR);
            }
        }
        return startIp.toString();
    }

    /**
     * It can be seen that the network segment calculation ends IP.
     * @param segment segment.
     * @return end IP
     */
    public static String getEndIp(String segment) {
        StringBuffer endIp = new StringBuffer();
        String startIp = getStartIp(segment,0);
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String maskIndex = arr[1];
        //Number of IPs actually needed.
        int hostNumber = 0;
        int[] startIpArray = new int[INT_CAPACITY];
        try {
            hostNumber = 1 << PARSE_IP_NUMBER - (Integer.parseInt(maskIndex));
            for (int i = 0; i < PARSE_IP_BITS_FOUR; i++) {
                startIpArray[i] = Integer.parseInt(startIp.split("\\.")[i]);
                if (i == PARSE_HOST_IP_INDEX) {
                    startIpArray[i] = startIpArray[i] - 1;
                    break;
                }
            }
            startIpArray[PARSE_HOST_IP_INDEX] = startIpArray[PARSE_HOST_IP_INDEX] + (hostNumber - 1);
        } catch (NumberFormatException e) {
            throw new HostException("get ip number error", ResponseConst.RET_GET_IP_NUMBER_ERROR);
        }

        if (startIpArray[PARSE_HOST_IP_INDEX] > PARSE_IP_NETWORK_SEGMENT) {
            int k = startIpArray[PARSE_HOST_IP_INDEX] / PARSE_IP_SUBNETWORK_SEGMENT;
            startIpArray[PARSE_HOST_IP_INDEX] = startIpArray[PARSE_HOST_IP_INDEX] % PARSE_IP_SUBNETWORK_SEGMENT;
            startIpArray[PARSE_SUBNETWORK_INDEX] = startIpArray[2] + k;
        }
        if (startIpArray[PARSE_SUBNETWORK_INDEX] > PARSE_IP_NETWORK_SEGMENT) {
            int j = startIpArray[PARSE_SUBNETWORK_INDEX] / PARSE_IP_SUBNETWORK_SEGMENT;
            startIpArray[PARSE_SUBNETWORK_INDEX] = startIpArray[PARSE_SUBNETWORK_INDEX] % PARSE_IP_SUBNETWORK_SEGMENT;
            startIpArray[1] = startIpArray[1] + j;
            if (startIpArray[1] > PARSE_IP_NETWORK_SEGMENT) {
                int k = startIpArray[1] / PARSE_IP_SUBNETWORK_SEGMENT;
                startIpArray[1] = startIpArray[1] % PARSE_IP_SUBNETWORK_SEGMENT;
                startIpArray[0] = startIpArray[0] + k;
            }
        }
        for (int i = 0; i < PARSE_IP_BITS_FOUR; i++) {
            if (i == PARSE_HOST_IP_INDEX) {
                startIpArray[i] = startIpArray[i] - 1;
            }
            if ("".equals(endIp.toString()) || endIp.length() == 0) {
                endIp.append(startIpArray[i]);
            } else {
                endIp.append("." + startIpArray[i]);
            }
        }
        return endIp.toString();
    }

}
