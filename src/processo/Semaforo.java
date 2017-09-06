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
public class Semaforo {
    int sem;
    
    public Semaforo(int s){
        sem = s;
    }
    
    public synchronized void down(){
        while(sem==0){
            try{
                wait();
            }catch(InterruptedException e){       
            }
        }
        sem--;
    }
    
    public synchronized void up(){
        if(sem==0)
            notifyAll();
        sem++;
    }
}
