/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Rodrigo
 */
public final class Processo extends Thread{
    public static final int INICIO = 10000;
    public static final int FIM = 10010;
    private int porta;
    private String ip;
    private ArrayList<No> nos;
    private ArrayList<No> falty;
    private No lider;
    private FailureDetector failureDetector;
    private Consensus consenso;
    private int valor;
    private boolean []canal = new boolean[FIM-INICIO];
    private boolean []processo = new boolean[FIM-INICIO];
    
    public Processo(String ip,int porta){
        this.porta = porta;
        this.ip = ip;
        this.nos = new ArrayList<>();
        this.falty = new ArrayList<>();
        this.valor = porta;
        Random aleatorio = new Random();
        int qt = aleatorio.nextInt(10);
        /* Cada nó terá 10 nós conectados a ele, criaremos a sua lista de clientes
         abaixo. As portas irão de 10000 até 10009, e como é uma aplicação local
         todos os clientes terão o mesmo IP */
        for(int i=INICIO;i<FIM;i++){
            novoCliente("127.0.0.1", i);
            canal[i%10000] = processo[i%10000] = true;
        }
        canal[8] = false;
        processo[8] = false;
        canal[9] = false;
        processo[9] = false;
    }
    
    @Override
    public void run() {
        new ServidorProcesso(this).start();
        failureDetector = new FailureDetector(this);
        failureDetector.start();
        new LeaderElection(this).start();
        consenso = new Consensus(this);
        System.out.println("Processo "+porta+": Inicializado!");
        try{
            Thread.sleep(1000);
            consenso.start();
        }catch(InterruptedException e){
            
        }
        
        /*try{
            broadcastArrayList();
        }catch(InterruptedException e){
            
        }*/
        
        
        /*while(true){
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                System.out.println("Processo "+porta+": Erro! "+e.getMessage());
            }
            System.out.println(" Processo "+porta+": Executando!");
        }*/
    }
    
    public void broadcast(String mensagem){
        for (No no : nos) {
            /*if(porta==no.getPorta())
                continue;*/
            enviaMensagem(no.getIp(), no.getPorta(),mensagem);
        }
    }
    
    public void enviaMensagem(String host,int porta,String mensagem){
        boolean msgEnviada = false;
        while(!msgEnviada){
            try{
                Socket cliente = new Socket(host, porta);
                System.out.println("Thread "+this.porta+": me conectei ao nó "+porta+"!");
                PrintStream saida = new PrintStream(cliente.getOutputStream());
                System.out.println("Thread "+this.porta+": enviou a "+porta+": "+mensagem);
                saida.println(mensagem);
                saida.close();
                cliente.close();
                msgEnviada = true;
                System.out.println("Processo "+this.porta+": finalizei conexão com o nó "+porta+"!");
            }catch(UnknownHostException e){
                System.out.println("Processo "+ porta+": Servidor não encontrado! Tentando conexão novamente em 500ms ...");
                try{
                    Thread.sleep(500);
                }catch(InterruptedException ex){
                    System.out.println("Processo "+this.porta+": Erro! "+ex.getMessage());
                }
            }catch(IOException e){
                System.out.println("Processo "+this.porta+": Erro! "+e.getMessage());
            }catch(Exception e){
                System.out.println("Processo "+this.porta+": Erro! "+e.getMessage());
            }
        }
    }
    
    public boolean novoCliente(String ip,int porta){
        for (No no : nos) {
            if(no.getIp().equals(ip)&&no.getPorta()==porta){
                return false;
            }
        }
        nos.add(new No(ip,porta));
        return true;
    }
    
   /* public boolean excluirCliente(String ip,int porta){
        for (No no : nos) {
            if(no.getIp().equals(ip)&&no.getPorta()==porta){
                nos.remove(no);
                return true;
            }
        }
        return false;
    }*/
   
    public No getNo(String ip,int porta){
        for (No no : nos) {
            if(no.getIp().equals(ip)&&no.getPorta()==porta){
                return no;
            }
        }
        return null;
    }
    
    public int getPorta() {
        return porta;
    }   
    
    public String getIp(){
        return ip;
    }
    
    public void adicionaFalty(No no){
        if(falty.contains(no))
            return;
        System.out.println("Processo "+porta+": adicionou falty "+no);
        falty.add(no);
    }
    
    public void removeFalty(No no){
        if(falty.remove(no))
            System.out.println("Processo "+porta+": removeu falty "+no);
    }
    
    public ArrayList<No> getNos(){
        return nos;
    }
    
    public ArrayList<No> getFalty(){
        return falty;
    }
    
    public boolean verificaFalty(No v){
        for (No no : falty) {
            if(no.getIp().equals(v.getIp())&&no.getPorta()==v.getPorta()){
                return true;
            }
        }
        return false;
    }
    
    public void notification(No noFailure){
        for (No no : nos) {
            if((porta==no.getPorta())||(noFailure.getPorta()==no.getPorta()))
                continue;
            enviaMensagem(no.getIp(), no.getPorta(),"n"+getPorta()+"---notification---"+noFailure.getIp()+"---"+noFailure.getPorta());
        }
    }
    
    public No getLider(){
        return lider;
    }
    
    public void setLider(No lider){
        this.lider = lider;
    }
    
    public FailureDetector getFailureDetector(){
        return failureDetector;
    }
    
    public void iAmAlive(String ipCliente,int portaCliente){
        enviaMensagem(ipCliente, portaCliente, "n"+porta+"---iamalive");
    }
    
    public void novaEleicao(){
        new LeaderElection(this).start();
    }
    
    public Consensus getConsenso(){
        return consenso;
    }
    
    public void setValor(int valor){
        this.valor = valor;
    }
    
    public int getValor(){
        return this.valor;
    }
    
    public boolean qos(No no){
        return canal[no.getPorta()%10000];
    }
    
    public boolean particaoSincrona(No no){
        return processo[no.getPorta()%10000];
    }
    
    public boolean particaoSincrona(int no){
        return processo[no%10000];
    }
}
