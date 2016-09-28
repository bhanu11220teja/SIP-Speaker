package kth.ik2213.sipspeaker;

import java.io.*;
import javax.media.*;
import java.net.*;
import javax.media.rtp.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

/**
 *
 * @author btkotte,debopam
 */

public class RTPHandler {

    private String destinationAddress;
    private int destinationPort;
    private String localAddress;
    private SIPParams sipParams;
    private Processor mProcessor;
    private RTPManager rtpManager;
    private SendStream mStream;
    private String tempFileName;
    File mediaFile;
    private boolean isStopped;

    public RTPHandler(String destinationAddress, int destinationPort, String localAddress, SIPParams sipParams, String key) {
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.localAddress = localAddress;
        this.sipParams = sipParams;
        tempFileName = key;
        isStopped = false;
        createTempFile();

    }

    //Sends audio to the voip client
    public void start() {
        try {
            rtpManager = RTPManager.newInstance();
            SessionAddress localSessionAddress = new SessionAddress(InetAddress.getByName(localAddress), SessionAddress.ANY_PORT);
            rtpManager.initialize(localSessionAddress);
            InetAddress destAddress = InetAddress.getByName(destinationAddress);
            SessionAddress remoteAddress = new SessionAddress(destAddress, destinationPort);
            rtpManager.addTarget(remoteAddress);
            mediaFile = new File(tempFileName);
            DataSource source = Manager.createDataSource(new MediaLocator(mediaFile.toURL()));
            Format[] formats = new Format[]{new AudioFormat(AudioFormat.ULAW_RTP, 8000, 8, 1)};
            ContentDescriptor contentDesc = new ContentDescriptor(ContentDescriptor.RAW_RTP);
            mProcessor = Manager.createRealizedProcessor(new ProcessorModel(source, formats, contentDesc));
            mProcessor.start();
            mStream = rtpManager.createSendStream(mProcessor.getDataOutput(), 0);
            mStream.start();
            double duration = mProcessor.getDuration().getSeconds();
            Thread.sleep(1000 + 1000 * (int) duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (isStopped) {
            return;
        }
        isStopped = true;
        try {
            mStream.stop();
            mStream.close();
            mProcessor.stop();
            mProcessor.close();
            rtpManager.removeTargets("Session isStopped.");
            rtpManager.dispose();
            mediaFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void createTempFile() {
        InputStream in = null;
        OutputStream out = null;

        try {
            File source = new File(sipParams.getMessageWav());
            File destination = new File(tempFileName);

            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buf)) > 0) {
                out.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
            }

        }
    }

    private void sleep(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
