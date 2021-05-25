import java.io.*;
import java.net.*;
import java.util.*;

class VaccinationList {
    private List<String> locais;
    private List<Integer> SNS_numbers;

    public VaccinationList(){
        locais = new ArrayList<>(); SNS_numbers = new ArrayList<>();    /* Alterar esta parte */
        locais.add("Braga");    locais.add("Porto");    locais.add("Lisboa");   locais.add("Coimbra");  
        SNS_numbers.add(12345678);  SNS_numbers.add(22345678);  SNS_numbers.add(52345678);  SNS_numbers.add(82345678);
    }

    public boolean isValid_SNS(int number){
        return SNS_numbers.contains(number);
    }


    public List<String> getLocais(){
        return this.locais;
    }

}

class Client_conector implements Runnable{
    private Socket socket;
    private VaccinationList vaccinations;

    public Client_conector (Socket s, VaccinationList v) {
        this.socket = s;
        this.vaccinations = v;
    }

    
    @Override
    public void run() {
        System.out.println("Novo Cliente conectado...");
        try{
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String clientMessage;
            int tentativas = 0;
            while( ( clientMessage = in.readLine() ) != null   && !vaccinations.isValid_SNS(Integer.parseInt(clientMessage)) ){
                out.println("Número SNS inválido"); 
                tentativas++;
                if( tentativas == 3){ break; }
            } 


            while(( clientMessage = in.readLine() ) != null && tentativas <= 3){
                if( clientMessage.equals("LOCAIS") ){   out.println(" *** Locais de vacinação ***\n" + vaccinations.getLocais()); }
                if( clientMessage.equals("AGENDAR")){   out.println("Vacinação Agendada ... DATA ");    }

            }

            
            socket.shutdownInput(); socket.shutdownOutput();    socket.close();
            System.out.println("Cliente disconectado...");
    
        }catch(IOException e){
            e.printStackTrace();
        }}

}
public class Server {

    public static void main (String[] args) throws IOException {
       boolean running = true;
       VaccinationList vaccinations = new VaccinationList();

        while (running) {
            ServerSocket serverSocket = new ServerSocket(0);
                System.out.println("("+serverSocket.getLocalPort()+") Waiting new client...");
                Socket clientSocket = serverSocket.accept();
                Thread client_connector = new Thread(new Client_conector(clientSocket, vaccinations));
            client_connector.start();
            
        }
    }

}