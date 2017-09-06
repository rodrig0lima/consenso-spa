/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rodrigo
 */
public class Teste1 extends Thread{
    private int valor;
    public Teste1(int valor){
        this.valor = valor;
    }
    @Override
    public void run(){
        System.out.println("Valor: "+valor);
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}

