/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Properties;

/**
 *
 * @author btkotte,debopam
 */
public class SIPSpeaker {

    private static String configFileName = "";
    private static String sipUri = "";
    private static String httpBindAddress = "";
    private static SIPParams sipParams;
    private static HTTPParams httpParams;

    public static String getConfigFileName() {
        return configFileName;
    }

    public static void setConfigFileName(String configFileName) {
        SIPSpeaker.configFileName = configFileName;
    }

    public static String getSipUri() {
        return sipUri;
    }

    public static void setSipUri(String sipUri) {
        SIPSpeaker.sipUri = sipUri;
    }

    public static String getHttpBindAddress() {
        return httpBindAddress;
    }

    public static void setHttpBindAddress(String httpBindAddress) {
        SIPSpeaker.httpBindAddress = httpBindAddress;
    }

    public static SIPParams getSipParams() {
        return sipParams;
    }

    public static void setSipParams(SIPParams sipParams) {
        SIPSpeaker.sipParams = sipParams;
    }

    public static HTTPParams getHttpParams() {
        return httpParams;
    }

    public static void setHttpParams(HTTPParams httpParams) {
        SIPSpeaker.httpParams = httpParams;
    }

    public static void main(String[] args) {
        boolean isConfigProvided = false;
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-c")) {//Configuration File Name
                    if (i < args.length && !args[i + 1].startsWith("-")) {
                        setConfigFileName(args[i + 1]);
                        isConfigProvided = true;
                        System.out.println("Name of Configuration File provided: " + getConfigFileName());
                    }
                } else if (args[i].equalsIgnoreCase("-user")) {//SIP URI
                    if (i < args.length && !args[i + 1].startsWith("-")) {
                        setSipUri(args[i + 1]);
                        System.out.println("SIP URI provided: " + getSipUri());
                    }
                } else if (args[i].equalsIgnoreCase("-http")) {//HTTP Bind Address
                    if (i < args.length && !args[i + 1].startsWith("-")) {
                        setHttpBindAddress(args[i + 1]);
                        System.out.println("HTTP Bind Address provided: " + getHttpBindAddress());
                    }
                }
            }
            System.out.println("Trying to use the information and configure self...");
        }
        if (isConfigProvided) {
            //Read from user provided configuration file first.
            //In case of exception fall back to default configuration file
            try {
                loadConfigurationFile(getConfigFileName());
                System.out.println("Specified configuration file loaded successfully!");
            } catch (Exception ex) {
                System.out.println("Unable to load specified configuration file!");
                System.out.println("Exception: " + ex.getMessage());
                System.out.println("Loading default configuration file");
                try {
                    loadConfigurationFile(Constants.DEFAULT_CONFIG_FILENAME);
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            //Directly read from default configuration file
            System.out.println("Loading default configuration file");
            try {
                loadConfigurationFile(Constants.DEFAULT_CONFIG_FILENAME);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        if (!"".equals(getSipUri())) {
            //Parse user provided SIP URI: [-user user@host[:port]]
            //In case of exception fall back to default SIP URI
            try {
                String[] sipUriParts = getSipUri().split("@");
                getSipParams().setSipUser(sipUriParts[0]);
                String[] sipHostParts;
                if (sipUriParts[1].contains(":")) {
                    sipHostParts = sipUriParts[1].split(":");
                    getSipParams().setSipInterface(Utility.getIPAddress(sipHostParts[0]));
                    getSipParams().setSipPort(Integer.parseInt(sipHostParts[1]));
                } else {
                    getSipParams().setSipInterface(sipUriParts[1]);
                    getSipParams().setSipPort(Constants.DEFAULT_SIP_PORT);
                }
                getSipParams().setSipUri(getSipParams().getSipUser() + "@" + getSipParams().getSipInterface() + ":" + getSipParams().getSipPort());
                System.out.println("Specified SIP URI configured: " + getSipParams().getSipUri());
            } catch (Exception ex) {
                System.out.println("Unable to configure specified SIP URI!");
                System.out.println("Exception: " + ex.getMessage());
                getSipParams().setSipUser(Constants.DEFAULT_SIP_USER);
                getSipParams().setSipInterface(Constants.DEFAULT_SIP_INTERFACE);
                getSipParams().setSipPort(Constants.DEFAULT_SIP_PORT);
                getSipParams().setSipUri(getSipParams().getSipUser() + "@" + getSipParams().getSipInterface() + ":" + getSipParams().getSipPort());
                System.out.println("Default SIP URI configured: " + getSipParams().getSipUri());
            }
        } else {
            getSipParams().setSipUser(Constants.DEFAULT_SIP_USER);
            getSipParams().setSipInterface(Constants.DEFAULT_SIP_INTERFACE);
            getSipParams().setSipPort(Constants.DEFAULT_SIP_PORT);
            getSipParams().setSipUri(getSipParams().getSipUser() + "@" + getSipParams().getSipInterface() + ":" + getSipParams().getSipPort());
            System.out.println("Default SIP URI configured: " + getSipParams().getSipUri());
        }
        if (!"".equals(getHttpBindAddress())) {
            //Parse user provided HTTP Bind Address
            //In case of exception fall back to default HTTP Bind Address
            try {
                try {
                    int port = Integer.parseInt(getHttpBindAddress());
                    getHttpParams().setHttpInterface(Constants.DEFAULT_HTTP_INTERFACE);
                    getHttpParams().setHttpPort(port);
                } catch (NumberFormatException e) {
                    if (getHttpBindAddress().contains(":")) {
                        String[] httpParts = getHttpBindAddress().split(":");
                        getHttpParams().setHttpInterface(Utility.getIPAddress(httpParts[0]));
                        getHttpParams().setHttpPort(Integer.parseInt(httpParts[1]));
                    } else {
                        getHttpParams().setHttpInterface(Utility.getIPAddress(getHttpBindAddress()));
                        getHttpParams().setHttpPort(Constants.DEFAULT_HTTP_PORT);
                    }
                }

                System.out.println("Specified HTTP Bind Address configured: " + getHttpParams().getHttpInterface() + ":" + getHttpParams().getHttpPort());
            } catch (Exception ex) {
                System.out.println("Unable to configure specified HTTP Bind Address!");
                System.out.println("Exception: " + ex.getMessage());
                getHttpParams().setHttpInterface(Constants.DEFAULT_HTTP_INTERFACE);
                getHttpParams().setHttpPort(Constants.DEFAULT_HTTP_PORT);
                System.out.println("Default HTTP Bind Address configured: " + getHttpParams().getHttpInterface() + ":" + getHttpParams().getHttpPort());
            }

        } else {
            //Set Default HTTP Bind Address
            getHttpParams().setHttpInterface(Constants.DEFAULT_HTTP_INTERFACE);
            getHttpParams().setHttpPort(Constants.DEFAULT_HTTP_PORT);
            System.out.println("Default HTTP Bind Address configured: " + getHttpParams().getHttpInterface() + ":" + getHttpParams().getHttpPort());
        }
        System.out.println("Configuration phase complete");
        System.out.println("SIP Parameters: " + getSipParams().toString());
        System.out.println("HTTP Parameters: " + getHttpParams().toString());
        //Now the Web server and SIP server 
        //will try to start with the configured parameters
        HTTPHandler httpHandler = new HTTPHandler(getSipParams(), getHttpParams());
        httpHandler.start();

        DatagramSocket sipServerSocket = null;
        try {
            sipServerSocket = new DatagramSocket(getSipParams().getSipPort());
        } catch (SocketException ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        byte[] receivedData = new byte[2048];
        while (true) {
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                sipServerSocket.receive(receivedPacket);
            } catch (IOException ex) {
                System.out.println("Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            SIPHandler sipHandler = new SIPHandler(receivedPacket, sipServerSocket, sipParams);
            sipHandler.start();
        }
    }

    private static void loadConfigurationFile(String configurationFileName)
            throws FileNotFoundException, IOException, Exception {
        Properties configurationProperties = new Properties();
        configurationProperties.load(new FileInputStream(configurationFileName));

        setSipParams(new SIPParams());
        getSipParams().setConfigurationFileName(configurationFileName);
        getSipParams().setDefaultMessageWav(configurationProperties.getProperty("default_message_wav"));
        getSipParams().setDefaultMessageText(configurationProperties.getProperty("default_message_text"));
        Utility.createVoiceFile(getSipParams().getDefaultMessageText(), getSipParams().getDefaultMessageWav());
        getSipParams().setMessageText(configurationProperties.getProperty("message_text"));
        getSipParams().setMessageWav(configurationProperties.getProperty("message_wav"));
        if (getSipParams().getMessageText() == null || "".equals(getSipParams().getMessageText().trim())) {
            getSipParams().setMessageText(getSipParams().getDefaultMessageText());
            Utility.copyFile(new File(getSipParams().getDefaultMessageWav()), new File(getSipParams().getMessageWav()));
        } else {
            Utility.createVoiceFile(getSipParams().getMessageText(), getSipParams().getMessageWav());
        }
        getSipParams().setSipInterface(Utility.getIPAddress(configurationProperties.getProperty("sip_interface")));
        getSipParams().setSipPort(Integer.parseInt(configurationProperties.getProperty("sip_port").trim()));
        getSipParams().setSipUser(configurationProperties.getProperty("sip_user"));
        setHttpParams(new HTTPParams());
        getHttpParams().setHttpInterface(Utility.getIPAddress(configurationProperties.getProperty("http_interface")));
        getHttpParams().setHttpPort(Integer.parseInt(configurationProperties.getProperty("http_port")));
    }

}
