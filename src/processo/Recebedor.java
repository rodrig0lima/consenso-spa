/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Rodrigo
 */
public class Recebedor extends Thread{
    private InputStream cliente;
    private Processo processo;
    private String ipCliente;
    public Recebedor(InputStream cliente,String ipCliente,Processo processo) {
        this.cliente = cliente;
        this.processo = processo;
        this.ipCliente = ipCliente;
    }

    @Override
    public void run() {
        Scanner s = new Scanner(this.cliente);
        while (s.hasNextLine()) {
            //TRATAR TODAS AS MENSAGENS DE ACORDO COM O PADRÃO CRIADO PARA:
            String msg = s.nextLine();
            System.out.println("Recebedor "+processo.getPorta()+": recebeu "+msg);
            String[] partes = msg.split("---");
            int nProcesso = Integer.parseInt(partes[0].split("n")[1]);
            No no = processo.getNo(ipCliente, nProcesso);
            switch(partes[1]){
                case "areyoualive":
                    //Failure Detector Task 5
                    processo.iAmAlive(ipCliente, nProcesso);
                break;
                case "iamalive":
                    //Failure Detector Task 3
                    processo.getFailureDetector().isAlive(no);
                break;
                case "notification":
                    /*  Parâmetros esperados:
                        partes[2] = ip
                        partes[3] = porta */
                    //Failure Detector Task 4
                    no = processo.getNo(partes[2],Integer.valueOf(partes[3]));
                    processo.adicionaFalty(no);
                    if(no==processo.getLider())
                        processo.novaEleicao();
                break;
                case "preparerequest":
                    /*  Parâmetros esperados:
                            partes[2] = Rodada proposta */
                    //Consensus Task 2
                    processo.getConsenso().new PrepareRequestAcceptor(ipCliente,nProcesso,Integer.valueOf(partes[2])).start();
                break;
                case "ackpreparerequest":
                    /*   Parâmetros esperados:
                            partes[2] = Rodada proposta
                            partes[3] = Rodada do processo i
                            partes[4] = Valor do processo i */
                    processo.getConsenso().new AckPrepareRequestAcceptor(ipCliente,nProcesso,Integer.valueOf(partes[2]),Integer.valueOf(partes[3]),Integer.valueOf(partes[4])).start();
                break;
                case "acceptrequest":
                    /*  Parâmetros esperados:
                            partes[2] = Rodada proposta
                            partes[3] = Valor do processo i 
                        Quorum:
                            partes[4] = Id do processo i 
                            partes[5] = Id do processo i+1 
                                    ...
                            partes[n] = Id do processo i+x */
                    //Adiciona os valores recebidos ao vetor quorum
                    ArrayList<Integer> quorum = new ArrayList<>();
                    for(int i = 4;i<partes.length;i++){
                        quorum.add(Integer.valueOf(partes[i]));
                    }
                    processo.getConsenso().new AcceptRequestAcceptor(ipCliente, nProcesso, Integer.valueOf(partes[2]), Integer.valueOf(partes[3]), quorum).start();
                break;
                case "ackacceptrequest":
                    /*  Parâmetros esperados:[
                            partes[2] = Rodadada processo do i
                            partes[3] = Valor do processo i */
                    processo.getConsenso().new AckAcceptRequestAcceptor(ipCliente, nProcesso,Integer.parseInt(partes[2]), Integer.parseInt(partes[3])).start();
                break;
                case "decisionmessage":
                    /*  Parâmetros esperados:
                            partes[2] = Valor decidido */
                        processo.getConsenso().new DecisionMessage(ipCliente, nProcesso,Integer.parseInt(partes[2])).start();
                break;
                default:
                    System.out.println("Erro! Mensagem recebida: "+partes[1]);
            }
            /* - 
                ARE YOU ALIVE? 
                NOTIFICATION
                PREPARE REQUEST
                ACK PREPARE REQUEST
                ACCEPT REQUEST
                ACK ACCEPT REQUEST
                DECISION MESSAGE
            */
        }
        s.close();
    }
}
