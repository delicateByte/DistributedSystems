package util;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {
	private static String OS = System.getProperty("os.name").toLowerCase();
	public static String getIP() {
		
		
		if (isWindows()) {
			System.out.println("This is Windows");
	        InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getLocalHost();
				return inetAddress.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			return null;
		
		} else if (isUnix()) {
			System.out.println("This is Unix or Linux");
			try(final DatagramSocket socket = new DatagramSocket()){
				  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				  String ip = socket.getLocalAddress().getHostAddress();
				  System.out.println(ip);
				  return ip;
				}catch(Exception e){
					System.out.println(e);
				}
			return null;
		}else {
			return null;
		}
		

	}
	public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isUnix() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
		
	}

	public static boolean isSolaris() {

		return (OS.indexOf("sunos") >= 0);

	}
}
