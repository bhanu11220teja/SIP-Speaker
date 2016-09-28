/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import java.io.File;
import java.io.IOException;
//import javax.media.format.AudioFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.net.DatagramSocket;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import jlibrtp.*;

/**
 *
 * @author btkotte,debopam
 */
public class SoundSender implements RTPAppIntf {

    public RTPSession rtpSession = null;
    static int pktCount = 0;
    static int dataCount = 0;
    private String filename;
    private final int EXTERNAL_BUFFER_SIZE = 1024;
    SourceDataLine auline;
    private Position curPosition;
    boolean local = false;
    SIPParams sipParams;

    enum Position {

        LEFT, RIGHT, NORMAL
    };

    public void receiveData(DataFrame dummy1, Participant dummy2) {
        // We don't expect any data.
    }

    public void userEvent(int type, Participant[] participant) {
        //Do nothing
    }

    public int frameSize(int payloadType) {
        return 1;
    }

    public SoundSender(String ipAddress, int port, SIPParams sipParams) {
        this.sipParams = sipParams;
        System.out.println("IP address: " + ipAddress);
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;
        try {
            rtpSocket = new DatagramSocket(0);
            int lport = rtpSocket.getLocalPort();
            if (lport % 2 == 0) {
                rtcpSocket = new DatagramSocket(lport + 1);
            } else {
                rtcpSocket = new DatagramSocket(lport + 2);
            }
        } catch (Exception ex) {
            Logger.getLogger(SoundSender.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("RTPSession failed to obtain port");
        }

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.RTPSessionRegister(this, null, null);
        System.out.println("CNAME: " + rtpSession.CNAME());
        Participant p = new Participant(ipAddress, port, port + 1);
        rtpSession.addParticipant(p);
        filename = sipParams.getMessageWav();
    }

    public void run(String key) {
        if (RTPSession.rtpDebugLevel > 1) {
            System.out.println("-> Run()");
        }
        File soundFile = new File(filename);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + filename);
            return;
        }
        
        try{
            AudioFileFormat audioFormat = AudioSystem.getAudioFileFormat(soundFile);
            System.out.println("Sound File Details******************************");
            System.out.println("Encoding: "+audioFormat.getFormat().getEncoding());
            System.out.println("sampleRate: "+audioFormat.getFormat().getSampleRate());
            System.out.println("sampleSizeInBits: "+audioFormat.getFormat().getSampleSizeInBits());
            System.out.println("channels: "+audioFormat.getFormat().getChannels());
            System.out.println("frameSize: "+audioFormat.getFormat().getFrameSize());
            System.out.println("frameRate: "+audioFormat.getFormat().getFrameRate());
            System.out.println("************************************************");
        }catch(Exception e){
            e.printStackTrace();
        }
        
        File tempFile = new File(key);
        try {
            Utility.copyFile(soundFile, tempFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(tempFile);
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
            tempFile.delete();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            tempFile.delete();
            return;
        }

        //AudioFormat.Encoding encoding = new AudioFormat.Encoding("ULAW");
        AudioFormat.Encoding encoding = new AudioFormat.Encoding("PCM_SIGNED");
        //AudioFormat format = new AudioFormat(encoding, ((float) 8000.0), 8, 1, 1, ((float) 8000.0), false);
        AudioFormat format = new AudioFormat(encoding, ((float) 8000.0), 16, 1, 2, ((float) 8000.0), false);
        
        //AudioFormat mediaFormat = new AudioFormat(AudioFormat.GSM_RTP, 8000, 8, 1);
        //javax.sound.sampled.AudioFormat format = convertFormat(mediaFormat);
        System.out.println(format.toString());

        if (!this.local) {
            // To time the output correctly, we also play at the input:
            auline = null;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            try {
                auline = (SourceDataLine) AudioSystem.getLine(info);
                auline.open(format);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                tempFile.delete();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                tempFile.delete();
                return;
            }

            if (auline.isControlSupported(FloatControl.Type.PAN)) {
                FloatControl pan = (FloatControl) auline
                        .getControl(FloatControl.Type.PAN);
                if (this.curPosition == Position.RIGHT) {
                    pan.setValue(1.0f);
                } else if (this.curPosition == Position.LEFT) {
                    pan.setValue(-1.0f);
                }
            }

            auline.start();
        }

        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        long start = System.currentTimeMillis();
        try {
            boolean isActive = sipParams.getSessionWiseTagPairs() != null
                    && !sipParams.getSessionWiseTagPairs().isEmpty()
                    && sipParams.getSessionWiseTagPairs().containsKey(key);
            while (nBytesRead != -1 && pktCount < 1000 && isActive) {

                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                
                if (nBytesRead >= 0) {
                    rtpSession.sendData(abData);
                    auline.write(abData, 0, abData.length);
                }
                if (pktCount == 100) {
                    Enumeration<Participant> iter = this.rtpSession.getParticipants();
                    Participant p = null;

                    while (iter.hasMoreElements()) {
                        p = iter.nextElement();

                        String name = "name";
                        byte[] nameBytes = name.getBytes();
                        String data = "abcd";
                        byte[] dataBytes = data.getBytes();

                        int ret = rtpSession.sendRTCPAppPacket(p.getSSRC(), 0, nameBytes, dataBytes);
                        System.out.println("!!!!!!!!!!!! ADDED APPLICATION SPECIFIC " + ret);
                        continue;
                    }
                    if (p == null) {
                        System.out.println("No participant with SSRC available :(");
                    }
                }
                isActive = sipParams.getSessionWiseTagPairs() != null
                        && !sipParams.getSessionWiseTagPairs().isEmpty()
                        && sipParams.getSessionWiseTagPairs().containsKey(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
            tempFile.delete();
            return;
        }
        System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000 + " s");

        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }

        this.rtpSession.endSession();
        tempFile.delete();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        if (RTPSession.rtpDebugLevel > 1) {
            System.out.println("<- Run()");
        }
    }

    /*public static javax.sound.sampled.AudioFormat convertFormat(
            AudioFormat format) {

        String encodingString = format.getEncoding();
        int channels = format.getChannels();
        double frameRate = format.getFrameRate();
        int frameSize = format.getFrameSizeInBits() / 8;
        double sampleRate = format.getSampleRate();
        int sampleSize = format.getSampleSizeInBits();
        boolean endian = (format.getEndian() == AudioFormat.BIG_ENDIAN);
        int signed = format.getSigned();

        Encoding encoding = null;
        if (AudioFormat.LINEAR.equals(encodingString)) {
            switch (signed) {
                case AudioFormat.SIGNED:
                    encoding = Encoding.PCM_SIGNED;
                    break;
                case AudioFormat.UNSIGNED:
                    encoding = Encoding.PCM_UNSIGNED;
                    break;
                default:
                    encoding = Encoding.PCM_SIGNED; // TODO: return null
            }
        } else if (AudioFormat.ALAW.equals(encodingString)) {
            encoding = Encoding.ALAW;
        } else if (AudioFormat.ULAW.equals(encodingString)) {
            encoding = Encoding.ULAW;
        } else if (toMpegEncoding(encodingString) != null) {

            encoding = toMpegEncoding(encodingString);

        } else if (toVorbisEncoding(encodingString) != null) {

            encoding = toVorbisEncoding(encodingString);

        } else {
            encoding = new CustomEncoding(encodingString);
        }

        final javax.sound.sampled.AudioFormat sampledFormat;

        if (encoding == Encoding.PCM_SIGNED) {
            sampledFormat = new javax.sound.sampled.AudioFormat(
                    (float) sampleRate, sampleSize, channels, true, endian);

        } else if (encoding == Encoding.PCM_UNSIGNED) {
            sampledFormat = new javax.sound.sampled.AudioFormat(
                    (float) sampleRate, sampleSize, channels, false, endian);
        } else if (encoding instanceof MpegEncoding) {
			// TODO: perhaps we should use reflection to avoid class not found
            // problems if javazoom is not in the classpath.
            return new MpegAudioFormat(encoding, (float) sampleRate,
                    sampleSize, channels,
                    // signed,
                    frameSize, (float) frameRate, endian, new HashMap());
        } else if (encoding instanceof VorbisEncoding) {
			// TODO: perhaps we should use reflection to avoid class not found
            // problems if javazoom is not in the classpath.
            return new VorbisAudioFormat(encoding, (float) sampleRate,
                    sampleSize, channels,
                    // signed,
                    frameSize, (float) frameRate, endian, new HashMap());
        } else {
            sampledFormat = new javax.sound.sampled.AudioFormat(encoding,
                    (float) sampleRate, sampleSize, channels, frameSize,
                    (float) frameRate, endian);
        }

        return sampledFormat;
    }*/
}
