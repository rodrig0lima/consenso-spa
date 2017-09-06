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
class LeaderElection extends Thread{
    private Processo processo;
    //LeaderElection Task T0
    public LeaderElection(Processo processo){
        this.processo = processo;
    }
    //LeaderElection Task T1
    @Override
    public void run(){
        No lider = processo.getLider();
        for(No no : processo.getNos()){
            if(lider==null){
                lider = no;
                break;
            }
            if((no.getPorta()>lider.getPorta())&&(!processo.verificaFalty(no)&&(processo.particaoSincrona(no)))){
                lider = no;
                break;
            }
        }
        processo.setLider(lider);
        System.out.println("LeaderElection "+processo.getPorta()+": Novo lider "+lider);
    }
}
