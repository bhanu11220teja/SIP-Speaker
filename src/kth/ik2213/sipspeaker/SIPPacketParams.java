/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

/**
 *
 * @author btkotte,debopam
 */

public class SIPPacketParams {

    private String requestType;
    private String recipient;//Example: sip:test@192.168.0.5
    private String recipientIP;//Example: 192.168.0.5
    private String recipientUserName;//Example: test
    private String via;//Example: Via: SIP/2.0/UDP 192.168.0.5:5060;rport;branch=z9hG4bK240049629
    private String branch;//Example: z9hG4bK240049629
    private String sender;//Example: sip:sipuser@192.168.0.5
    private String senderUserName;//Example: sipuser
    private String senderIP;//Example: 192.168.0.5
    private String senderTag;//Example: From: <sip:sipuser@192.168.0.5>;tag=1584168284
    private String toField;//Example: To: <sip:test@192.168.0.5:5064>
    private String callID;//Example: Call-ID: 1658840397
    private String cSeq1;//Example: CSeq: 20 INVITE -> 20
    private String cSeq2;//Example: CSeq: 20 INVITE -> INVITE
    private String contact;//Example: Contact: <sip:sipuser@192.168.0.5>
    private String contentType;//Example: Content-Type: application/sdp
    private String allow;//Example: Allow: INVITE, ACK, CANCEL, OPTIONS, BYE, REFER, NOTIFY, MESSAGE, SUBSCRIBE, INFO
    private String maxForwards;//Example: Max-Forwards: 70
    private String userAgent;//User-Agent: Linphone/3.6.1 (eXosip2/4.0.0)
    private String subject;//Example: Subject: Phone call
    private String contentLength;//Example: Content-Length:   437
    private String session;//Example: o=sipuser 3063 1869 IN IP4 192.168.0.5 -> 3063 is the ID
    private String media;//Example: m=audio 7078 RTP/AVP 124 111 110 0 8 101
    private String time;//Example: t=0 0

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRecipientIP() {
        return recipientIP;
    }

    public void setRecipientIP(String recipientIP) {
        this.recipientIP = recipientIP;
    }

    public String getRecipientUserName() {
        return recipientUserName;
    }

    public void setRecipientUserName(String recipientUserName) {
        this.recipientUserName = recipientUserName;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
    }

    public String getSenderIP() {
        return senderIP;
    }

    public void setSenderIP(String senderIP) {
        this.senderIP = senderIP;
    }

    public String getSenderTag() {
        return senderTag;
    }

    public void setSenderTag(String senderTag) {
        this.senderTag = senderTag;
    }

    public String getToField() {
        return toField;
    }

    public void setToField(String toField) {
        this.toField = toField;
    }

    public String getCallID() {
        return callID;
    }

    public void setCallID(String callID) {
        this.callID = callID;
    }

    public String getcSeq1() {
        return cSeq1;
    }

    public void setcSeq1(String cSeq1) {
        this.cSeq1 = cSeq1;
    }

    public String getcSeq2() {
        return cSeq2;
    }

    public void setcSeq2(String cSeq2) {
        this.cSeq2 = cSeq2;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getAllow() {
        return allow;
    }

    public void setAllow(String allow) {
        this.allow = allow;
    }

    public String getMaxForwards() {
        return maxForwards;
    }

    public void setMaxForwards(String maxForwards) {
        this.maxForwards = maxForwards;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

}
