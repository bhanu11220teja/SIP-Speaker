/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author btkotte,debopam
 */

//Class that handles the accepted HTTP request
public class HTTPRequestHandler extends Thread {

    private Socket clientSocketForHTTP = null;
    private SIPParams sipParams;
    BufferedReader in = null;
    PrintWriter out = null;

    public HTTPRequestHandler(Socket clientSocket, SIPParams sipParams) {
        this.clientSocketForHTTP = clientSocket;
        this.sipParams = sipParams;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocketForHTTP.getInputStream()));
            out = new PrintWriter(clientSocketForHTTP.getOutputStream(), true);
            String[] httpRequest = GetRequest();
            String headerType = getHeaderType(httpRequest);
            if ("GET".equals(headerType)) {
                SendMessageForm();
            } else if ("POST".equals(headerType)) {
                //Process User Modification
                String content = httpRequest[httpRequest.length - 1];
                //System.out.println("POST Content: "+ content);
                String[] params = content.split("&");
                if (params.length > 1) {
                    //Delete Current Message Logic
                    //Set Default message and wav as current message and wav resp.
                    sipParams.setMessageText(sipParams.getDefaultMessageText());
                    File currentWavFile = new File(sipParams.getMessageWav());
                    Utility.copyFile(new File(sipParams.getDefaultMessageWav()), currentWavFile);
                } else {
                    //Modify Current Message
                    String[] messageParts = params[0].split("=");
                    if (messageParts[1] != null && !"".equals(messageParts[1].trim())) {
                        //Change the current message
                        String newMessage = messageParts[1].replace("%0D%0A", " ");
                        newMessage = newMessage.replace("+", " ");
                        newMessage = newMessage.replace("%26", "&");
                        newMessage = newMessage.replace("%2B", "+");
                        newMessage = newMessage.replace("%2C", ",");
                        newMessage = newMessage.replace("%3B", ";");
                        newMessage = newMessage.replace("%3F", "?");
                        newMessage = newMessage.replace("%21", "!");
                        newMessage = newMessage.replace("%25", "%");
                        System.out.println("New Message: " + newMessage);
                        sipParams.setMessageText(newMessage);
                        //Create wav file
                        Utility.createVoiceFile(sipParams.getMessageText(), sipParams.getMessageWav());
                    }
                }
                if (sipParams.getConfigurationFileName() != null && !"".equals(sipParams.getConfigurationFileName())) {
                    Properties arguments = new Properties();
                    arguments.load(new FileInputStream(sipParams.getConfigurationFileName()));
                    arguments.setProperty("message_text", sipParams.getMessageText());
                    arguments.store(new FileOutputStream(sipParams.getConfigurationFileName()), null);
                }
                //Return back the Message Form to the user
                SendMessageForm();
            } else if ("GET_OTHER".equals(headerType)) {
                //Invalid Request
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html\r\n");
                out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
                out.println("<html><head>");
                out.println("<title>Message</title>");
                out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-15\">");
                out.println("</head><body>");
                out.println("Invalid URL\r\n");
                out.println("<form action='/' method=GET><br/>");
                out.println("<input type='submit' value='Click Here'>");
                out.println("</form></body></html>");
                out.flush();
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ex) {
                System.out.println("Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private String[] GetRequest() throws IOException {

        ArrayList<String> request = new ArrayList<String>();
        boolean isPost = false;
        String contentLength = "0";
        String inLine = in.readLine();
        request.add(inLine);
        if (inLine != null && inLine.matches("^POST / HTTP/1\\..$")) {
            isPost = true;
        }
        while (!(inLine = in.readLine()).equals("")) {
            if (inLine.matches("^Content-Length: [0-9]+")) {
                contentLength = inLine.replaceAll("Content-Length: ", "").trim();
            }
            request.add(inLine);
        }
        if (isPost == true) {
            char[] content = new char[Integer.parseInt(contentLength)];
            in.read(content);
            request.add(new String(content));
        }

        String[] httpRequest = request.toArray(new String[request.size()]);
        return httpRequest;

    }

    private static String getHeaderType(String[] httpRequest) throws ParseException {
        if (httpRequest[0].matches("^GET /(\\?)? HTTP/1\\..$")) {
            return "GET";
        } else if (httpRequest[0].matches("^POST / HTTP/1\\..$")) {
            return "POST";
        } else if (httpRequest[0].matches("^GET /.* HTTP/1\\..$")) {
            return "GET_OTHER";
        } else {
            throw new ParseException("INVALID_REQUEST", 0);
        }
    }

    private void SendMessageForm() {

        BufferedReader br = null;
        String inLine = null;
        String formLocation = new File("").getAbsolutePath() + "/index.html";
        //System.out.println("HTML Form location: " + formLocation);
        try {
            br = new BufferedReader(new FileReader(formLocation));
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html\r\n");
            while ((inLine = br.readLine()) != null) {
                if (inLine.contains("_CURR_MSG_")) {
                    inLine = inLine.replaceAll("_CURR_MSG_", sipParams.getMessageText());
                }
                out.println(inLine);
            }
            br.close();
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
