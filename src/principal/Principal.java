/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package principal;
import processo.Processo;
/**
 *
 * @author Rodrigo
 */
public class Principal {
    public static void main(String [] args){
        for(int i=Processo.INICIO;i<Processo.FIM;i++){
            Processo processo = new Processo("127.0.0.1",i);
            processo.start();
        }
    }
}
