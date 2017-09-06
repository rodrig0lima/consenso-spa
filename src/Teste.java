/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Rodrigo
 */
import com.sun.applet2.preloader.CancelException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Teste {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Task());

        try {
            String msg = "n10000---areyoualive";
            
            System.out.println("Started..");
            ThreadTeste teste = new ThreadTeste(future);
            teste.start();
            System.out.println(future.get(3, TimeUnit.SECONDS));
        } catch(CancellationException e){
            System.out.println("Finished!");
        }catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Terminated!");
        }
        System.out.println("Ola");
        executor.shutdownNow();
    }
}

class Task implements Callable<String> {
    @Override
    public String call() throws Exception {
        Thread.sleep(4000); // Just to demo a long running task of 4 seconds.
        return "Ready!";
    }
}
class ThreadTeste extends Thread{
    Future<String> future;
    public ThreadTeste(Future<String> future){
        this.future = future;
    }
    @Override
    public void run(){
        try{
            Thread.sleep(1000);
            future.cancel(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}