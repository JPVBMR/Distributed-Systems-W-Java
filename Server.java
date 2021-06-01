import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.util.*;
import java.util.concurrent.locks.*;


class VaccinationsList{
    private List<String> locais;
    private List<Integer> SNS_numbers;
    private Map<Integer,Vaccination> mapa_agendamentos;
    private final Lock locker = new ReentrantLock();

    private class Vaccination{
        String local; LocalDateTime data; int sns;
    }

    public VaccinationsList() throws FileNotFoundException{
        this.locais = new ArrayList<>();        load_locais("Locais.txt");
        this.SNS_numbers = new ArrayList<>();   load_sns_numbers("SNS.txt");
        this.mapa_agendamentos = new HashMap<>();   load_agenda("Agenda.txt");
    }

    public boolean load_locais(String filename) throws FileNotFoundException{
        locker.lock();
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            String location;
            while (myReader.hasNextLine()) {
                location = myReader.nextLine();
                this.locais.add(location);
            }
            myReader.close();
            return this.locais.isEmpty();
        
        } finally {
            locker.unlock();
        }
        
    }
    public boolean load_sns_numbers(String filename) throws FileNotFoundException{
        locker.lock();
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                this.SNS_numbers.add(Integer.parseInt(myReader.nextLine()));
            }
            myReader.close();
            return this.SNS_numbers.isEmpty();
        } finally {
            locker.unlock();
        }
        
    }
    public void load_agenda(String filename) throws FileNotFoundException{
        locker.lock();
        try {
            File myObj = new File(filename);
            if(myObj.exists()){
                Scanner myReader = new Scanner(myObj);
                String line;
                while ( myReader.hasNextLine()){
                    line = myReader.nextLine();
                    Vaccination v = new Vaccination();
                    
                    v.sns = Integer.parseInt(line.substring(0, line.indexOf("|")));
                    v.local = line.substring(line.indexOf("|")+1, line.lastIndexOf("|"));
                    LocalDateTime dateTime = LocalDateTime.parse(line.substring(line.lastIndexOf("|")+1, line.length()));
                    v.data = dateTime;
                    this.mapa_agendamentos.put(v.sns, v);
                }
                myReader.close();
            }else{
                myObj.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            locker.unlock();
        }
    }
    public void save_agenda(String filename){
        try {
            File myObj = new File(filename);
            myObj.createNewFile();
            FileWriter fw = new FileWriter(myObj, false);
            BufferedWriter bw = new BufferedWriter(fw);
            for( Integer number: this.mapa_agendamentos.keySet()){
                String line = number+"|"+this.mapa_agendamentos.get(number).local+"|"+this.mapa_agendamentos.get(number).data+"\n";
                bw.write(line);
            }
            bw.close();
        } catch (FileNotFoundException e) { e.printStackTrace();
        } catch (IOException e){ e.printStackTrace();} 
    }
    public synchronized boolean isValid_SNS(int number) {
        locker.lock();
        try{
            return this.SNS_numbers.contains(number);
        }finally{ locker.unlock(); }
    }
    public synchronized List<String> getLocais(){
        locker.lock();
        try{
            return this.locais;
        }finally{ locker.unlock(); }
    }
    public synchronized String agendar(String location, int sns){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E dd-MM-yyyy HH:mm:ss");  
        locker.lock();
        try{
            if(mapa_agendamentos.containsKey(sns)){ 
                return "Possui uma marcação em "+mapa_agendamentos.get(sns).local+": "+formatter.format(mapa_agendamentos.get(sns).data);
            }
            else{
                List<LocalDateTime> list_datas = new ArrayList<>();
                LocalDateTime availableDate = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

                for(Vaccination vac_aux: mapa_agendamentos.values()){
                    if(location.equals(vac_aux.local)){
                        list_datas.add(vac_aux.data);   // Guardar todas as datas agendadas
                    }
                }
                Collections.sort(list_datas);   // Organizar datas

                for(int i = 0; i < list_datas.size(); i++){
                    if(availableDate.getDayOfYear() == list_datas.get(i).getDayOfYear() && availableDate.getHour() == list_datas.get(i).getHour()){
                        availableDate = availableDate.plusHours(1);
                    }
                    if(availableDate.getHour() > 20 || availableDate.getHour()< 9 ){
                        availableDate = availableDate.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                        i = 0;
                    }
                }

                Vaccination vaccination = new Vaccination();
                    vaccination.sns = sns;  
                    vaccination.local = location;
                    vaccination.data = availableDate;
                this.mapa_agendamentos.put(vaccination.sns, vaccination);

                return "Marcação agendada em "+vaccination.local+": "+formatter.format(vaccination.data);
            }
        }finally{ locker.unlock();}
    }
    public synchronized void desmarcar(int sns){
        locker.lock();
        try {
            this.mapa_agendamentos.remove(sns);
        } finally{ locker.unlock();}
    }
}

class Server_worker implements Runnable{
    private Socket socket;
    private VaccinationsList vaccinationsList;

    public Server_worker(Socket clientSocket, VaccinationsList vac){
        this.socket = clientSocket;
        this.vaccinationsList = vac;
    }
    public void run() {
        String clientMessage; String[] cli_cmds;   int cli_sns=0;

        System.out.println("Novo cliente conectado...");
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Introduza o número de utente (SNS): ");
            while( (clientMessage = in.readLine()) != null ){
                cli_sns = Integer.parseInt(clientMessage);
                if(vaccinationsList.isValid_SNS(cli_sns)) break;
                out.println("Número SNS inválido.");
            }
            
            while( (clientMessage = in.readLine()) != null ){
                if (clientMessage.contains("AGENDAR") && (cli_cmds = clientMessage.split(" ")).length == 2) {
                    out.println(vaccinationsList.agendar(cli_cmds[1], cli_sns));
                }
                else if (clientMessage.contains("DESMARCAR")) {
                    vaccinationsList.desmarcar(cli_sns);
                    out.println("Vacinação desmarcada.");
                }
                else if (clientMessage.contains("LOCAIS")) {
                    out.println("*** LOCAIS DE VACINAÇÃo *** \n"+ vaccinationsList.getLocais());
                }
            }
        } catch (IOException e) { e.printStackTrace();}   
    }    
}

public class Server {
    public static void main(String[] args) throws IOException {
        boolean running = true;
        VaccinationsList vaccinationsList = new VaccinationsList();
        ServerSocket serverSocket;

        Runtime.getRuntime().addShutdownHook(new Thread() { /** Handle Ctrl+C */
            public void run() { 
                vaccinationsList.save_agenda("Agenda.txt");
                System.out.println("\nTerminando Servidor...");
            }
         });

        while (running) {
            serverSocket = new ServerSocket(0);
            System.out.println("(" + serverSocket.getLocalPort() + ") Waiting new client...");
            Socket clientSocket = serverSocket.accept();
            Thread server_worker = new Thread(new Server_worker(clientSocket, vaccinationsList));
            server_worker.start();
        }
    }
}
