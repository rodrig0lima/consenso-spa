/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

/**
 *
 * @author Rodrigo
 */
public class No{
    private String ip;
    private int porta;
    public No(String ip,int porta){
        this.ip = ip;
        this.porta = porta;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }
    @Override
    public String toString(){
        return ip+":"+porta;
    }
}