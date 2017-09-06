/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Random;

/**
 *
 * @author Rodrigo
 */
public class FailureDetectorSender extends Thread{
    private No no;
    private Processo processo;
    private ExecutorService executor;
    Future<String> future;
    public FailureDetectorSender(Processo processo,No no) {
        this.processo = processo;
        this.no = no;
    }
    
    @Override
    public void run(){
        executor = Executors.newSingleThreadExecutor();
        future = executor.submit(new Task1());
        try {
            future.get(1000, TimeUnit.MILLISECONDS);
            throw new TimeoutException();
        } catch(CancellationException e){
            
        }catch (TimeoutException e){ //Caso o envio da mensagem venha com timeout
            if(processo.qos(no)){
                //Failure Detector Task 2
                future.cancel(true);
                // Caso a tarefa falhe, adicionar o no atual ao conjunto falty 
                processo.adicionaFalty(no);
                //Notificar todos os outros processos
                processo.notification(no);
                System.out.println("FailureDetector "+processo.getPorta()+": Falty "+no);
                if(no.equals(processo.getLider()))
                    processo.novaEleicao();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        executor.shutdownNow();
        future = null;
    }
    //Failure Detector Task 1
    class Task1 implements Callable<String> {
        @Override
        public String call() throws Exception {
            //Emvia a mensagem areyoualive
            processo.enviaMensagem(no.getIp(), no.getPorta(),"n"+processo.getPorta()+"---areyoualive"); 
            //Aguarda 500 ms, tempo de resposta
            Thread.sleep(600);
            return "";
        }
    }
    
    public void cancelarTimeout(){
        if(future==null)
            return;
        if(future.isCancelled())
            return;
        future.cancel(true);
    }
}
