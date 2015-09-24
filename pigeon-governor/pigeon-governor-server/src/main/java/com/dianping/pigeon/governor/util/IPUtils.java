package com.dianping.pigeon.governor.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class IPUtils {

	public static String getFirstNoLoopbackIP4Address() {
        Collection<String> allNoLoopbackIP4Addresses = getNoLoopbackIP4Addresses();
        if (allNoLoopbackIP4Addresses.isEmpty()) {
            return null;
        }
        return allNoLoopbackIP4Addresses.iterator().next();
    }

    public static Collection<String> getNoLoopbackIP4Addresses() {
        Collection<String> noLoopbackIP4Addresses = new ArrayList<String>();
        Collection<InetAddress> allInetAddresses = getAllHostAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()
                    && !Inet6Address.class.isInstance(address)) {
                noLoopbackIP4Addresses.add(address.getHostAddress());
            }
        }
        if (noLoopbackIP4Addresses.isEmpty()) {
            // 降低过滤标准，将site local address纳入结果
            for (InetAddress address : allInetAddresses) {
                if (!address.isLoopbackAddress() && !Inet6Address.class.isInstance(address)) {
                    noLoopbackIP4Addresses.add(address.getHostAddress());
                }
            }
        }
        return noLoopbackIP4Addresses;
    }
    
    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
	
    public static String getUserIP(final HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }
        // 获取cdn-src-ip中的源IP
        String ip = request.getHeader("Cdn-Src-Ip");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return (ip == null || "".equals(ip)) ? "0.0.0.0" : ip;
    }
    
    public static String addHost(String hosts, String ip, String port) {
	    if(!checkIpAddress(ip)) {
	        throw new RuntimeException("Invalid ip " + ip);
	    }
	    if(!checkNumber(port, 1, 65535)) {
	        throw new RuntimeException("Invalid port " + port);
	    }
	    String host = ip + ":" + port;
	    if(hosts != null && hosts.indexOf(host) != -1) {
	        // if already exists, just return	        
	        return hosts;
	    }
	    hosts = (hosts==null ? "" : hosts.trim());
	    StringBuilder sb = new StringBuilder(hosts);
	    if(hosts.length()>0 && !hosts.endsWith(","))
	        sb.append(',');
	    sb.append(host);
	    return sb.toString();
	}
    
    private static boolean checkIpAddress(String ip) {
	    if(null == ip)
	        return false;
	    return ip.indexOf('.') != -1;
	}
	
	private static boolean checkNumber(String number, int min, int max) {
	    try {
	        int n = Integer.parseInt(number);
	        return (n>=min && n<=max);
	    } catch(NumberFormatException e) {
	        return false;
	    }
	}

	public static String removeHost(String hosts, String ip, String port) {
	    if(!checkIpAddress(ip)) {
            throw new RuntimeException("Invalid ip " + ip);
        }
        if(!checkNumber(port, 1, 65535)) {
            throw new RuntimeException("Invalid port " + port);
        }
	    String host = ip + ":" + port;
	    int idx = -1;
	    if(hosts==null || (idx = hosts.indexOf(host)) == -1) {
	        // if not exist, ignore
	        return hosts;
	    }
	    int idx2 = hosts.indexOf(',', idx);
	    String newHosts = hosts.substring(0, idx) + 
	            ((idx2==-1 || idx2==hosts.length()-1) ? "" : hosts.substring(idx2 + 1));
	    return newHosts;
	}

}
