SIPSpeaker
==========

Done by:
| Bhanu Teja Kotte | btkotte@kth.se | 
| Debopam Bhattacherjee | debopam@kth.se | 

SIP Speaker is a SIP User Agent (UA) that uses SIP signaling. A SIP User Agent can be a softphone used for Voice over IP. In this project we have designed and implemented a SIP UA that waits for incoming calls and answers them when received. When the call is answered, SIP Speaker plays a message. When the message ends the call terminates. SIP Speaker speaks (sends audio data) but doesn't listen. We have also integrated a web server that can be used to change the current message to be played.

Protocols implemented: SIP, SDP, Basic HTTP server
Programming Language: Java (Oracle JDK 1.7)
Text-to-speech library: Fret’s 1.2
External library used for RTP/RTCP: Java Media Framework (JMF)
SIP Client (for testing): Linphone 3.6.1
