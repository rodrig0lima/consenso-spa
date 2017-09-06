/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author rodrigo
 */
public class Consensus extends Thread{
    private final Processo processo;
    private ArrayList<Integer> quorum;
    private ArrayList<AckPrepareRequest> recebido;
    private ArrayList<AckAcceptRequest> confirmado;
    // Round, Last round, valor atual da decisão
    private int r,lr,v;
    private final int n;
    //Executará o proposer sempre
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public Consensus(Processo processo){
        this.processo = processo;
        quorum = new ArrayList<>();
        recebido = new ArrayList<>();
        confirmado = new ArrayList<>();
        n = processo.getNos().size(); //Quantidade de processos
        v = processo.getValor();
    }
    
    //Consensus Task 0
    @Override
    public void run(){
        scheduler.scheduleAtFixedRate(new Task0(), 0, 5, TimeUnit.SECONDS);
       
    }
    
    public class Task0 extends Thread{
        @Override
        public void run(){
            if(processo.getLider()==null){
                try{
                    Thread.sleep(500);
                }catch(InterruptedException e){
                    System.out.println("Consenso "+processo.getPorta()+": "+e.getMessage());
                }
            }
            if(processo.getLider().getPorta()==processo.getPorta())
                new Proposer().start();
        }
    }
    
    //Consensus Task 1
    public class Proposer extends Thread{
        @Override
        public void run(){
            //ri ← max(ri , lri ) + n;
            r = (r>lr?r:lr) + n;
            //Fase 1 da rodada R
            //Quorum ← every correct process that is a member of a synchronous partition and does not belong to f aulty
            quorum = new ArrayList<>();
            recebido = new ArrayList<>();
            //Verifica os processos que não são falty e os adicionam ao arraylist quorum
            for (No no : processo.getNos()) {
                if((!processo.verificaFalty(no))&&(processo.particaoSincrona(no)))
                    quorum.add(no.getPorta());
                
            }
            System.out.println("Consenso "+processo.getPorta()+": propondo rodada "+r);
            processo.broadcast("n"+processo.getPorta()+"---preparerequest---"+r);
        }
    }
    
    //Consensus Task 1 Fase 1
    public class AckPrepareRequestAcceptor extends Thread{
        private final String ip;
        private final int ri,rj,vj,porta;

        public AckPrepareRequestAcceptor(String ip,int porta,int ri,int rj, int vj) {
            //Remove os falty do quorum
            verificaQuorum();
            this.ri = ri;
            this.rj = rj;
            this.vj = vj;
            this.ip = ip;
            this.porta = porta;
        }
        
        @Override
        public void run(){
            if(ri!=r)
                return;
            //Verifica se o processo percente ao quorum
            for (int valor : quorum) {
                if(valor==porta){
                    recebido.add(new AckPrepareRequest(porta, ri, rj, vj));
                }
            }
            //Quando receber todo o quorum, inicia a fase 2
            System.out.println("Consenso "+processo.getPorta()+": Tamanha quorum: "+quorum.size()+" Recebidos: "+recebido.size());
            if(quorum.size()==recebido.size())
                new Fase2Proposer().start();
        }
    }
    
    //Pacote de prepare request
    public class AckPrepareRequest{
        private final int porta,ri,rj,vj;

        public AckPrepareRequest(int porta, int ri, int rj, int vj) {
            this.porta = porta;
            this.ri = ri;
            this.rj = rj;
            this.vj = vj;
        }

        public int getPorta() {
            return porta;
        }

        public int getRi() {
            return ri;
        }

        public int getRj() {
            return rj;
        }

        public int getVj() {
            return vj;
        }
    }
    
    //Consensus Task 1 Fase 2
    public class Fase2Proposer extends Thread{
        @Override
        public void run(){
            AckPrepareRequest maior = recebido.get(0);
            for (AckPrepareRequest pacote : recebido) {
                //Linha 2, 
                if(pacote.getRj()>=maior.getRj())
                    maior = pacote;
            }
            System.out.println("Consenso "+processo.getPorta()+": quorum recebido. Propondo valor: "+maior.getVj());
            //Atualiza Quorum
            verificaQuorum();
            String quorumString = "";
            for (Integer no : quorum) {
                quorumString += "---"+no;
            }
            processo.broadcast("n"+processo.getPorta()+"---acceptrequest---"+maior.getRi()+"---"+maior.getVj()+quorumString);
        }
    }
    
    //Consensus Task 2
    public class PrepareRequestAcceptor extends Thread{
        private final int rodada,porta;
        private final String ip;
        
        public PrepareRequestAcceptor(String ip,int porta,int rodada){
            this.rodada = rodada;
            this.ip = ip;
            this.porta = porta;
        }
        
        @Override
        public void run(){
            if((rodada>r)&&(rodada>lr)||(processo.getPorta()==processo.getLider().getPorta())){
                //Zera o array de espera de accept para todos os processos
                confirmado = new ArrayList<>();
                lr = rodada;
                processo.enviaMensagem(ip, porta, "n"+processo.getPorta()+"---ackpreparerequest---"+rodada+"---"+r+"---"+v);
            }
        }
    }
    
    //Consensus Task 3
    public class AcceptRequestAcceptor extends Thread{
        private final int rodada,porta,valor;
        private final String ip;
        ArrayList<Integer> quorumc;
        
        public AcceptRequestAcceptor(String ip,int porta,int rodada,int valor,ArrayList<Integer> quorumc){
            this.rodada = rodada;
            this.ip = ip;
            this.valor = valor;
            this.porta = porta;
            this.quorumc = quorumc;
        }
        
        @Override
        public void run(){
            if((rodada>=r)&&(rodada>=lr)){
                lr = rodada;
                v = valor;
                r = rodada;
                quorum = quorumc;
                processo.broadcast("n"+processo.getPorta()+"---ackacceptrequest---"+rodada+"---"+v);
            }
        }
    }
    
    public class AckAcceptRequestAcceptor extends Thread{
        private final int porta,rodada,valor;
        private final String ip;
        public AckAcceptRequestAcceptor(String ip,int porta,int rodada,int valor){
            this.ip = ip;
            this.porta = porta;
            this.rodada = rodada;
            this.valor = valor;
            verificaQuorum();
        }
        @Override
        public void run(){
            if(rodada!=r)
                return;
            //Verifica se o processo percente ao quorum
            for (int no : quorum) {
                if(no==porta){
                    confirmado.add(new AckAcceptRequest(porta,rodada,valor));
                }
            }
            if(confirmado.size()==quorum.size())
                processo.broadcast("n"+processo.getPorta()+"---decisionmessage---"+valor);
        }
    }
    //Pacote ackacceptrequest auxiliar
    public class AckAcceptRequest{
        private final int porta,rodada,valor;
        public AckAcceptRequest(int porta,int rodada,int valor){
            this.porta = porta;
            this.rodada = rodada;
            this.valor = valor;
        }

        public int getPorta() {
            return porta;
        }

        public int getRodada() {
            return rodada;
        }

        public int getValor() {
            return valor;
        }
    }
    
    public class DecisionMessage extends Thread{
        private final int valor,porta;
        private final String ip;
        public DecisionMessage(String ip,int porta, int valor){
            this.ip = ip;
            this.porta = porta;
            this.valor = valor;
        }
        @Override
        public void run(){
            System.out.println("Consenso "+processo.getPorta()+": Decision message "+valor);
            processo.setValor(valor);
        }
    }
    
    //Funções auxiliares
    public synchronized void verificaQuorum() {
        for (Integer atual : quorum) {
            for(No no : processo.getFalty()){
                if(atual==no.getPorta()){
                    quorum.remove(atual);
                }
            }
            if(!processo.particaoSincrona(atual)){ //Remove todos os processos que não são de partições sincronas
                quorum.remove(atual);
            }
        }
    }
}
