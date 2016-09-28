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

//class to hold HTTP parameters
public class HTTPParams {
    private String httpInterface;
    private int httpPort;

    public String getHttpInterface() {
        return httpInterface;
    }

    public void setHttpInterface(String httpInterface) {
        this.httpInterface = httpInterface;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName()).append(" Object {");
        result.append("\nhttpInterface: ").append(getHttpInterface());
        result.append("\nhttpPort: ").append(getHttpPort());
        result.append("\n}");
        return result.toString();
    }
}
