/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Rodrigo
 */
public class FailureDetector extends Thread{
    Processo processo;

    public FailureDetector(Processo processo) {
        this.processo = processo;
    }
    
    //private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<Integer,FailureDetectorSender> timeout = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //Failure Detector Task 1
    @Override
    public void run(){
        for(No no: processo.getNos()){
            if(no.getPorta()==processo.getPorta())
                continue;
            //Criara um objeto que enviará a mensagem a um nó específico e aguardará a resposta
            scheduler.scheduleAtFixedRate(new Task1(no), 1, 1, TimeUnit.SECONDS);
        }
    }
    private class Task1 extends Thread{
        private No no;
        public Task1(No no){
            this.no = no;
        }
        @Override
        public void run(){
            timeout.put(no.getPorta(),new FailureDetectorSender(processo,no));
            timeout.get(no.getPorta()).start();
        }
    }
    //Failure Detector Task 3
    public void isAlive(No no){
        if(no==null)
            return;
        //Busca o objeto failure detectors sender
        FailureDetectorSender fds = timeout.get(no.getPorta());
        //Cancela o timeout do cliente x
        fds.cancelarTimeout();
        //Remove o objeto da lista
        processo.removeFalty(no);
    }
}
