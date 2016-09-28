/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.ik2213.sipspeaker;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author btkotte,debopam
 */


public class HTTPHandler extends Thread {

    private SIPParams sipParams;
    private HTTPParams httpParams;
    private ServerSocket serverSocketForHTTP = null;
    private Socket clientSocketForHTTP = null;

    public HTTPHandler(SIPParams sipParams, HTTPParams httpParams) {
        this.sipParams = sipParams;
        this.httpParams = httpParams;
    }

    //Accepts incoming HTTP connections and spawns a request handler
    public void run() {
        try {
            serverSocketForHTTP = new ServerSocket(httpParams.getHttpPort());
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        while (true) {
            try {
                clientSocketForHTTP = serverSocketForHTTP.accept();
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            (new HTTPRequestHandler(clientSocketForHTTP, sipParams)).start();
        }
    }
}
