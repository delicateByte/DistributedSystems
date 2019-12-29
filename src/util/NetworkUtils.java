package util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {
	
	public static String getIP() {
        InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
}
