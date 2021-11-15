package io.kyligence.notebook.console.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class NetworkUtils {

    private static final String LOCAL_HOST = "127.0.0.1";

    public static String getIntranetIp() {
        InetAddress inetAddress = getIntranetAddress();
        return inetAddress == null ? LOCAL_HOST : inetAddress.getHostAddress();

    }

    private static InetAddress getIntranetAddress() {
        try {
            InetAddress candidateAddress = null;
            int candidateInterfaceIndex = Integer.MAX_VALUE;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }

                int interfaceIndex = networkInterface.getIndex();
                for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress() && candidateInterfaceIndex > interfaceIndex) {
                        candidateAddress = inetAddress;
                        candidateInterfaceIndex = interfaceIndex;
                        break;
                    }
                }
            }

            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            log.warn("Failed to find intranet address", e);
        }
        return null;
    }


}
