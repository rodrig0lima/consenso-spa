
import java.util.ArrayList;

public class Testador{
    public static void main(String [] args){
        ArrayList<Teste1> teste = new ArrayList<>();
        for(int i=0;i<5;i++){
            teste.add(new Teste1(i));
            teste.get(i).start();
        }
        try{
            Thread.sleep(5000);
        }catch(InterruptedException e){
            
        }
        for(int i=0;i<5;i++){
            teste.get(i).start();
        }
    }
}