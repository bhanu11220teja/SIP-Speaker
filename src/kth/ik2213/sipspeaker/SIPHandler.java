/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author btkotte,debopam
 */
public class SIPHandler extends Thread {

    private DatagramPacket receivedPacket;
    private DatagramSocket sipServerSocket;
    private SIPParams sipParams;
    private SIPPacketParams packetParams = null;
    private String localTag = null;
    private String key = "";

    public DatagramPacket getReceivedPacket() {
        return receivedPacket;
    }

    public void setReceivedPacket(DatagramPacket receivedPacket) {
        this.receivedPacket = receivedPacket;
    }

    public DatagramSocket getSipServerSocket() {
        return sipServerSocket;
    }

    public void setSipServerSocket(DatagramSocket sipServerSocket) {
        this.sipServerSocket = sipServerSocket;
    }

    public SIPParams getSipParams() {
        return sipParams;
    }

    public void setSipParams(SIPParams sipParams) {
        this.sipParams = sipParams;
    }

    public SIPPacketParams getPacketParams() {
        return packetParams;
    }

    public void setPacketParams(SIPPacketParams packetParams) {
        this.packetParams = packetParams;
    }

    public String getLocalTag() {
        return localTag;
    }

    public void setLocalTag(String localTag) {
        this.localTag = localTag;
    }

    public SIPHandler(DatagramPacket receivedPacket, DatagramSocket sipServerSocket, SIPParams sipParams) {
        this.receivedPacket = receivedPacket;
        this.sipServerSocket = sipServerSocket;
        this.sipParams = sipParams;
    }

    @Override
    public void run() {
        String receivedData = new String(receivedPacket.getData());
        System.out.println("*********Received SIP data***********\n" + receivedData);
        try {
            String requestData[] = receivedData.split("\r\n|\r|\n");
            setPacketParams(new SIPPacketParams());
            for (String requestData1 : requestData) {
                if (requestData1 != null && !"".equals(requestData1.trim())) {
                    if (requestData1.startsWith("INVITE ")
                            || requestData1.startsWith("CANCEL ")
                            || requestData1.startsWith("BYE ")) {
                        String[] spaceSeparatedTokens = requestData1.split(" ");
                        getPacketParams().setRequestType(spaceSeparatedTokens[0]);
                        String[] colonSeparatedTokens = spaceSeparatedTokens[1].split(":");
                        getPacketParams().setRecipient(colonSeparatedTokens[0] + ":" + colonSeparatedTokens[1]);
                        getPacketParams().setRecipientUserName(colonSeparatedTokens[1].split("@")[0]);
                        getPacketParams().setRecipientIP(Utility.getIPAddress(sipParams.getSipInterface()));
                    } else if (requestData1.startsWith("Via:")) {
                        String senderUri = requestData1.split(";")[0].split(" ")[2];
                        getPacketParams().setVia(requestData1);
                        //getPacketParams().setSenderIP(Utility.getIPAddress(senderUri));
                        for (String viaPart : requestData1.split(";")) {
                            if (viaPart.startsWith("branch")) {
                                getPacketParams().setBranch(viaPart.split("=")[1]);
                                break;
                            }
                        }

                        //rport: response-port
                        /*String modifiedVia = requestData1.replace("rport", "rport=" + getSipParams().getSipPort());
                         getPacketParams().setVia(modifiedVia);
                         getPacketParams().setBranch(modifiedVia.split(";")[2].split("=")[1]);*/
                    } else if (requestData1.startsWith("From:")) {
                        if (requestData1.contains("\"")) {
                            getPacketParams().setSenderUserName(requestData1.split("\"")[1]);
                        }
                        String[] spaceSeparatedTokens = requestData1.split(" ");
                        String[] semicolonSeparatedTokens = spaceSeparatedTokens[1].split(";");
                        getPacketParams().setSenderTag(semicolonSeparatedTokens[1]);
                        String sender = semicolonSeparatedTokens[0];
                        getPacketParams().setSender(sender);
                        sender = sender.replace("<", "").replace(">", "");
                        //getPacketParams().setSenderUserName(sender.split("<")[0].replace("\"", ""));
                        String[] colonSeparatedTokens = sender.split(":");
                        String[] atSeparatedTokens = colonSeparatedTokens[1].split("@");
                        if (atSeparatedTokens.length > 1) {
                            getPacketParams().setSenderUserName(atSeparatedTokens[0]);
                            getPacketParams().setSenderIP(Utility.getIPAddress(atSeparatedTokens[1]));
                        } else {
                            getPacketParams().setSenderIP(Utility.getIPAddress(colonSeparatedTokens[1]));
                        }
                    } else if (requestData1.startsWith("To:")) {
                        getPacketParams().setToField(requestData1.split(" ")[1]);
                    } else if (requestData1.startsWith("Call-ID:")) {
                        getPacketParams().setCallID(requestData1.split(" ")[1]);
                    } else if (requestData1.startsWith("CSeq:")) {
                        String[] cSeq = requestData1.split(" ");
                        getPacketParams().setcSeq1(cSeq[1]);
                        getPacketParams().setcSeq2(cSeq[2]);
                    } else if (requestData1.startsWith("Content-Type:")) {
                        getPacketParams().setContentType(requestData1.split(" ")[1]);
                    } else if (requestData1.startsWith("Allow:")) {
                        getPacketParams().setAllow(requestData1.split(":")[1].trim());
                    } else if (requestData1.startsWith("Max-Forwards:")) {
                        getPacketParams().setMaxForwards(requestData1.split(" ")[1]);
                    } else if (requestData1.startsWith("User-Agent:")) {
                        String[] userAgent = requestData1.split(":");
                        getPacketParams().setUserAgent(userAgent[1].trim());
                    } else if (requestData1.startsWith("Subject:")) {
                        getPacketParams().setSubject(requestData1.split(":")[1]);
                    } else if (requestData1.startsWith("Content-Length:")) {
                        getPacketParams().setContentLength(requestData1);
                    } else if (requestData1.startsWith("o=")) {
                        String[] spaceSeparatedTokens = requestData1.split(" ");
                        getPacketParams().setSession(spaceSeparatedTokens[1]);
                    } else if (requestData1.startsWith("m=")) {
                        getPacketParams().setMedia(requestData1.split("=")[1]);
                    } else if (requestData1.startsWith("t=")) {
                        getPacketParams().setTime(requestData1.split("=")[1]);
                    }
                }
            }
            key = getPacketParams().getSenderTag() + "_" + getPacketParams().getCallID();
            if ("INVITE".equals(getPacketParams().getRequestType())
                    && (getSipParams().getSessionWiseTagPairs() == null
                    || getSipParams().getSessionWiseTagPairs().isEmpty()
                    || !getSipParams().getSessionWiseTagPairs().containsKey(key))) {
                if (getSipParams().getSessionWiseTagPairs() == null) {
                    getSipParams().setSessionWiseTagPairs(new HashMap<String, String>());
                }
                setLocalTag(getRadomString());
                String currentValue = getLocalTag() + "_" + getPacketParams().getBranch();
                getSipParams().getSessionWiseTagPairs().put(key, currentValue);
            } else {
                if (getSipParams().getSessionWiseTagPairs() != null
                        && !getSipParams().getSessionWiseTagPairs().isEmpty()
                        && getSipParams().getSessionWiseTagPairs().containsKey(key)) {
                    setLocalTag(getSipParams().getSessionWiseTagPairs().get(key).split("_")[0]);
                    String currentValue = getLocalTag() + "_" + getPacketParams().getBranch();
                    getSipParams().getSessionWiseTagPairs().remove(key);
                    getSipParams().getSessionWiseTagPairs().put(key, currentValue);
                }
            }

            DatagramPacket packet2bSent;
            if ("INVITE".equals(getPacketParams().getRequestType())) {
                if (getPacketParams().getRecipientUserName().equals(getSipParams().getSipUser())) {
                    String ringingMessage = getRingingMessage();
                    System.out.println("Ringing message generated***********************\r\n" + ringingMessage);
                    packet2bSent = new DatagramPacket(ringingMessage.getBytes(), ringingMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                    sipServerSocket.send(packet2bSent);
                    Thread.sleep(3000);
                    if (getSipParams().getSessionWiseTagPairs() == null
                            || getSipParams().getSessionWiseTagPairs().isEmpty()
                            || !getSipParams().getSessionWiseTagPairs().containsKey(key)) {
                        //SIP Session already cancelled
                    } else {
                        String okMessage = getOKMessage();
                        System.out.println("OK message generated***********************\r\n" + okMessage);
                        packet2bSent = new DatagramPacket(okMessage.getBytes(), okMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                        sipServerSocket.send(packet2bSent);
                        int remotePort = Integer.parseInt(getPacketParams().getMedia().split(" ")[1]);

                        //SoundSender mediaSender = new SoundSender(getPacketParams().getSenderIP(), remotePort, getSipParams());
                        //mediaSender.run(key);
                        //System.out.println("Media Stream Stopped");
                        RTPHandler rtpHandler = new RTPHandler(getPacketParams().getSenderIP(), remotePort, sipParams.getSipInterface(), sipParams, key);
                        rtpHandler.start();
                        rtpHandler.stop();
                        //

                        if (getSipParams().getSessionWiseTagPairs() == null
                                || getSipParams().getSessionWiseTagPairs().isEmpty()
                                || !getSipParams().getSessionWiseTagPairs().containsKey(key)) {
                            //SIP Session already cancelled
                        } else {
                            String byeMessage = getByeMessage();
                            System.out.println("Bye message generated***********************\r\n" + byeMessage);
                            packet2bSent = new DatagramPacket(byeMessage.getBytes(), byeMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                            sipServerSocket.send(packet2bSent);
                        }
                    }
                } else {
                    String notFoundMessage = getNotFoundMessage();
                    System.out.println("Not Found message generated***********************\r\n" + notFoundMessage);
                    packet2bSent = new DatagramPacket(notFoundMessage.getBytes(), notFoundMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                    sipServerSocket.send(packet2bSent);
                    if (getSipParams().getSessionWiseTagPairs() != null
                            && !getSipParams().getSessionWiseTagPairs().isEmpty()
                            && getSipParams().getSessionWiseTagPairs().containsKey(key)) {
                        getSipParams().getSessionWiseTagPairs().remove(key);
                    }
                }

            } else if ("CANCEL".equals(getPacketParams().getRequestType())) {
                String byeMessage = getCancelOKMessage();
                System.out.println("Cancel OK message generated***********************\r\n" + byeMessage);
                packet2bSent = new DatagramPacket(byeMessage.getBytes(), byeMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                sipServerSocket.send(packet2bSent);
                byeMessage = getRequestCancelledMessage();
                System.out.println("Request Cancelled message generated***********************\r\n" + byeMessage);
                packet2bSent = new DatagramPacket(byeMessage.getBytes(), byeMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                sipServerSocket.send(packet2bSent);
                //If the normal flow thread (if condition above) wakes up after this - Need to handle!
                if (getSipParams().getSessionWiseTagPairs() != null
                        && !getSipParams().getSessionWiseTagPairs().isEmpty()
                        && getSipParams().getSessionWiseTagPairs().containsKey(key)) {
                    getSipParams().getSessionWiseTagPairs().remove(key);
                }

            } else if ("BYE".equals(getPacketParams().getRequestType())) {
                String okMessage = getByeOKMessage();
                System.out.println("Bye OK message generated***********************\r\n" + okMessage);
                packet2bSent = new DatagramPacket(okMessage.getBytes(), okMessage.length(), receivedPacket.getAddress(), receivedPacket.getPort());
                sipServerSocket.send(packet2bSent);
                //If the normal flow thread (if condition above) wakes up after this - Need to handle!
                if (getSipParams().getSessionWiseTagPairs() != null
                        && !getSipParams().getSessionWiseTagPairs().isEmpty()
                        && getSipParams().getSessionWiseTagPairs().containsKey(key)) {
                    getSipParams().getSessionWiseTagPairs().remove(key);
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String getRingingMessage() {
        StringBuilder ringingMsg = new StringBuilder("SIP/2.0 180 Ringing\r\n");
        ringingMsg
                .append(getPacketParams().getVia()).append("\r\n")
                .append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: ").append(getPacketParams().getcSeq1()).append(" ").append(getPacketParams().getcSeq2()).append("\r\n")
                .append("Contact: <").append(getPacketParams().getRecipient()).append(":").append(getSipParams().getSipPort()).append(">\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Content-Length: 0\r\n\r\n");
        return ringingMsg.toString();

    }

    public String getOKMessage() {

        String[] mediaVals = getPacketParams().getMedia().split(" ");
        StringBuilder sdp = new StringBuilder("v=0\r\n");
        sdp
                .append("o=").append(getPacketParams().getRecipientUserName())
                .append(" ").append(getPacketParams().getSession())
                .append(" ").append(getPacketParams().getSession()).append(" IN IP4 ")
                .append(getPacketParams().getRecipientIP()).append("\r\n")
                .append("s=Talk\r\n")
                .append("c=IN IP4 ").append(getPacketParams().getRecipientIP()).append("\r\n")
                .append("t=").append(getPacketParams().getTime()).append("\r\n")
                .append("m=").append(mediaVals[0]).append(" ").append(mediaVals[1]).append(" ").append(mediaVals[2]).append(" ")
                .append("0 101").append("\r\n")
                //.append("11 10 0 8 101").append("\r\n")
                .append("a=rtpmap:0 PCMU/8000\r\n")
                //.append("a=rtpmap:8 PCMA/8000\r\n")
                //.append("a=rtpmap:11 L16/44100\r\n")
                //.append("a=rtpmap:10 L16/44100/2\r\n")
                .append("a=rtpmap:101 telephone-event/8000\r\n")
                .append("a=fmtp:101 0-11\r\n");

        String sdpString = sdp.toString();
        StringBuilder okMsg = new StringBuilder("SIP/2.0 200 OK\r\n");
        okMsg
                .append(getPacketParams().getVia()).append("\r\n")
                .append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: ").append(getPacketParams().getcSeq1()).append(" ").append(getPacketParams().getcSeq2()).append("\r\n")
                .append("Contact: <").append(getPacketParams().getRecipient()).append(":").append(getSipParams().getSipPort()).append(">\r\n")
                .append("Content-Type: ").append(getPacketParams().getContentType()).append("\r\n")
                .append("Max-Forwards: ").append(getPacketParams().getMaxForwards()).append("\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Content-Length: ").append(sdpString.length()).append("\r\n\r\n")
                .append(sdp.toString());
        return okMsg.toString();
    }

    public String getByeOKMessage() {
        StringBuilder okMsg = new StringBuilder("SIP/2.0 200 OK\r\n");
        okMsg
                .append(getPacketParams().getVia()).append("\r\n")
                .append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: ").append(getPacketParams().getcSeq1()).append(" ").append(getPacketParams().getcSeq2()).append("\r\n")
                .append("Contact: <").append(getPacketParams().getRecipient()).append(":").append(getSipParams().getSipPort()).append(">\r\n")
                .append("Max-Forwards: ").append(getPacketParams().getMaxForwards()).append("\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Content-Length: 0\r\n\r\n");
        return okMsg.toString();
    }

    public String getCancelOKMessage() {
        StringBuilder cancelOKMsg = new StringBuilder("SIP/2.0 200 OK\r\n");
        cancelOKMsg
                .append(getPacketParams().getVia()).append("\r\n")
                .append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: ").append(getPacketParams().getcSeq1()).append(" ").append(getPacketParams().getcSeq2()).append("\r\n")
                .append("Contact: <").append(getPacketParams().getRecipient()).append(":").append(getSipParams().getSipPort()).append(">\r\n")
                .append("Max-Forwards: ").append(getPacketParams().getMaxForwards()).append("\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Content-Length: 0\r\n\r\n");
        return cancelOKMsg.toString();
    }

    public String getRequestCancelledMessage() {
        StringBuilder requestCancelledMsg = new StringBuilder("SIP/2.0 487 Request Cancelled\r\n");
        requestCancelledMsg
                .append(getPacketParams().getVia()).append("\r\n")
                .append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: ").append(getPacketParams().getcSeq1()).append(" INVITE\r\n")
                .append("Max-Forwards: ").append(getPacketParams().getMaxForwards()).append("\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Content-Length: 0\r\n\r\n");
        return requestCancelledMsg.toString();
    }

    public String getByeMessage() {
        String[] viaParts = getPacketParams().getVia().split(";");
        StringBuilder byeMsg = new StringBuilder("BYE ");
        byeMsg
                .append("sip:").append(getPacketParams().getSenderUserName()).append("@").append(getPacketParams().getSenderIP()).append(" SIP/2.0\r\n")
                .append(viaParts[0]).append(";").append(viaParts[1]).append(";branch=")
                .append(getSipParams().getSessionWiseTagPairs().get(key).split("_")[1]).append("\r\n")
                //.append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                //.append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("From: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("To: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: 1 BYE\r\n")
                .append("Contact: <").append(getPacketParams().getRecipient()).append(":").append(getSipParams().getSipPort()).append(">\r\n")
                .append("Max-Forwards: ").append(getPacketParams().getMaxForwards()).append("\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Content-Length: 0\r\n\r\n");
        return byeMsg.toString();
    }

    public String getNotFoundMessage() {
        StringBuilder notFoundMsg = new StringBuilder("SIP/2.0 404 Not Found Call processing released\r\n");
        notFoundMsg
                .append(getPacketParams().getVia()).append("\r\n")
                .append("From: ").append(getPacketParams().getSender()).append(";").append(getPacketParams().getSenderTag()).append("\r\n")
                .append("To: <").append(getPacketParams().getRecipient()).append(">;tag=").append(getLocalTag()).append("\r\n")
                .append("Call-ID: ").append(getPacketParams().getCallID()).append("\r\n")
                .append("CSeq: ").append(getPacketParams().getcSeq1()).append(" ").append(getPacketParams().getcSeq2()).append("\r\n")
                .append("Contact: <").append(getPacketParams().getRecipient()).append(":").append(getSipParams().getSipPort()).append(">\r\n")
                .append("User-Agent: ").append(getPacketParams().getUserAgent()).append("\r\n")
                .append("Reason: Q.850 ;cause=1 ; text=\"Unallocated (unassigned) number\" ")
                .append("Content-Length: 0\r\n\r\n");
        return notFoundMsg.toString();

    }

    public String getRadomString() {
        StringBuilder randomValue = new StringBuilder("");
        for (int i = 0; i < 10; i++) {
            Random rand = new Random();
            int n = rand.nextInt(10);
            randomValue.append(n);
        }
        return randomValue.toString();
    }
}
