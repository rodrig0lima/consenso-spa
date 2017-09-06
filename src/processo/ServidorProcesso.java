/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author Rodrigo
 */
public class ServidorProcesso extends Thread{
    Processo processo;
    public ServidorProcesso(Processo processo){
        this.processo = processo;
    }
    
    @Override
    public void run(){
        try{
            ServerSocket servidor = new ServerSocket();
            servidor.setReuseAddress(true); //Permite conexões simultaneas
            servidor.bind(new InetSocketAddress(processo.getPorta()));
            System.out.println("Porta "+processo.getPorta()+" aberta.");
            while(true){
                Socket cliente = servidor.accept();
                // cria tratador de cliente numa nova thread
                 Recebedor tc = new Recebedor(cliente.getInputStream(),cliente.getInetAddress().getHostAddress(), processo);
                tc.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
