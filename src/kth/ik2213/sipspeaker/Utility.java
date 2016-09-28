/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.regex.Pattern;
import javax.sound.sampled.AudioFileFormat.Type;

/**
 *
 * @author btkotte,debopam
 */
public class Utility {

    private static final String IP_PATTERN = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[\\d]{1,5}$";
    
    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void createVoiceFile(String message, String fileName) throws Exception {
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice voice = voiceManager.getVoice(Constants.VOICE_NAME);
        if (voice == null) {
            throw new Exception("Voice not found for: " + Constants.VOICE_NAME);
        } else {
            try {
                voice.allocate();
                AudioPlayer player = null;
                System.out.println("Trying to create voice file: " + fileName);
                player = new SingleFileAudioPlayer(fileName.split("\\.")[0], Type.WAVE);
                voice.setAudioPlayer(player);
                voice.speak(message);
                voice.deallocate();
                player.close();
                System.out.println("Voice file created successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getIPAddress(String senderUri) throws Exception {
        if (Pattern.matches(IP_PATTERN, senderUri) == false) {
            InetAddress address = InetAddress.getByName(senderUri);
            senderUri = address.getHostAddress();
        } else {
            String ip[] = senderUri.split(":");
            senderUri = ip[0];
        }
        return senderUri;

    }
}
