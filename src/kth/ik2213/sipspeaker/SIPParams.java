/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import java.util.Map;

/**
 *
 * @author btkotte,debopam
 */
public class SIPParams {

    private String defaultMessageWav;
    private String defaultMessageText;
    private String messageText;
    private String messageWav;
    private String sipInterface;
    private int sipPort;
    private String sipUser;
    private String sipUri;
    private Map<String, String> sessionWiseTagPairs;
    private String configurationFileName;

    public String getDefaultMessageWav() {
        return defaultMessageWav;
    }

    public void setDefaultMessageWav(String defaultMessageWav) {
        this.defaultMessageWav = defaultMessageWav;
    }

    public String getDefaultMessageText() {
        return defaultMessageText;
    }

    public void setDefaultMessageText(String defaultMessageText) {
        this.defaultMessageText = defaultMessageText;
    }

    public String getMessageWav() {
        return messageWav;
    }

    public void setMessageWav(String messageWav) {
        this.messageWav = messageWav;
    }

    public String getSipInterface() {
        return sipInterface;
    }

    public void setSipInterface(String sipInterface) {
        this.sipInterface = sipInterface;
    }

    public int getSipPort() {
        return sipPort;
    }

    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
    }

    public String getSipUser() {
        return sipUser;
    }

    public void setSipUser(String sipUser) {
        this.sipUser = sipUser;
    }

    public String getSipUri() {
        return sipUri;
    }

    public void setSipUri(String sipUri) {
        this.sipUri = sipUri;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Map<String, String> getSessionWiseTagPairs() {
        return sessionWiseTagPairs;
    }

    public void setSessionWiseTagPairs(Map<String, String> sessionWiseTagPairs) {
        this.sessionWiseTagPairs = sessionWiseTagPairs;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public void setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName()).append(" Object {");
        result.append("\ndefaultMessageWav: ").append(getDefaultMessageWav());
        result.append("\ndefaultMessageText: ").append(getDefaultMessageText());
        result.append("\nmessageWav: ").append(getMessageWav());
        result.append("\nsipInterface: ").append(getSipInterface());
        result.append("\nsipPort: ").append(getSipPort());
        result.append("\nsipUser: ").append(getSipUser());
        result.append("\nsipUri: ").append(getSipUri());
        result.append("\n}");
        return result.toString();
    }

}
