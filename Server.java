import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.*;
import java.util.*;
import java.util.concurrent.locks.*;

class VaccinationList {
    private List<String> locais;
    private List<Integer> SNS_numbers;
    private HashMap<String, HashMap<Date, Integer>> mapa_agendamentos;
    private final Lock locker = new ReentrantLock();
    Condition cond = locker.newCondition();

    public VaccinationList() {
        this.locais = new ArrayList<>();
        this.SNS_numbers = new ArrayList<>();
        this.mapa_agendamentos = new HashMap<>();
        try {
            setLocais();
            setSNS();
            setAgenda();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void setLocais() throws FileNotFoundException {
        locker.lock();
        try {
            File myObj = new File("Locais.txt");
            Scanner myReader = new Scanner(myObj);
            String location;
            while (myReader.hasNextLine()) {
                location = myReader.nextLine();
                this.locais.add(location);
                this.mapa_agendamentos.put(location, new HashMap<>());
            }
            myReader.close();
        } finally {
            locker.unlock();
        }
    }

    public void setSNS() throws FileNotFoundException {
        locker.lock();
        try {
            File myObj = new File("SNS.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                this.SNS_numbers.add(Integer.parseInt(myReader.nextLine()));
            }
            myReader.close();

        } finally {
            locker.unlock();
        }
    }

    public void setAgenda() throws FileNotFoundException {
        locker.lock();
        try {
            File myObj = new File("Agenda.txt");
            Scanner myReader = new Scanner(myObj);
            String line;
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss aa", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));

            while (myReader.hasNextLine()) {
                if ((line = myReader.nextLine()).contains("#")) {
                    String local = line.replace("#", "");
                    HashMap<Date, Integer> mapAUX = new HashMap<>();
                    while (!(line = myReader.nextLine()).contains("!")) {
                        String sns = line.substring(0, line.indexOf("|"));
                        String data = line.substring(line.indexOf("|") + 1, line.length());
                        Date date = formatter.parse(data);
                        mapAUX.put(date, Integer.parseInt(sns));
                    }
                    mapa_agendamentos.put(local, mapAUX);
                }
            }
            myReader.close();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            locker.unlock();
        }
    }

    public Date agendar(String location, int sns) {
        System.out.println("*** AGENDAR ***");
        int horas = 9, min = 0, sec = 0;
        List<Date> datas = new ArrayList<>();
        datas.addAll(mapa_agendamentos.get(location).keySet());
        Collections.sort(datas);
        LocalDate availableDate = LocalDate.now();

        horas = LocalDateTime.now().getHour() + 1;
        int i = 0;
        for (i = 0; i <= datas.size() - 1; i++) {
            LocalDateTime d = datas.get(i).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (availableDate.getMonth().equals(d.getMonth()) && availableDate.getYear() == d.getYear()
                    && availableDate.getDayOfMonth() == d.getDayOfMonth()) {
                if (d.getHour() == horas) {
                    horas++;
                }
                if (horas > 20 || horas < 9) {
                    availableDate = availableDate.plusDays(1);
                    horas = 9;
                    i = 0;
                }
            }
        }
        mapa_agendamentos.get(location).put(
            java.util.Date.from(availableDate.atTime(horas, min, sec).atZone(ZoneId.systemDefault()).toInstant()),
            sns);
        updateAgenda();
        return java.util.Date.from(availableDate.atTime(horas, min, sec).atZone(ZoneId.systemDefault()).toInstant());
    }

    public void updateAgenda() {
        locker.lock();
        try {
            File myObj = new File("Agenda.txt");
            Scanner myReader = new Scanner(myObj);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss aa", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));

            FileWriter fw = new FileWriter(myObj, true);
            BufferedWriter bx = new BufferedWriter(new FileWriter(myObj, false));
            bx.write("");

            BufferedWriter bw = new BufferedWriter(fw);
            for (String local : this.mapa_agendamentos.keySet()) {
                if (!this.mapa_agendamentos.get(local).isEmpty()) {
                    bw.write("#" + local);
                    for (Date d : this.mapa_agendamentos.get(local).keySet()) {
                        bw.write("\n" + this.mapa_agendamentos.get(local).get(d) + "|" + formatter.format(d));
                    }
                    bw.write("\n!\n");
                }
            }
            bw.close();
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            locker.unlock();
        }
    }

    public void desmarcar(String local, Date data, int sns) {
        locker.lock();
        try {
            this.mapa_agendamentos.get(local).remove(data, sns);
        } finally {
            locker.unlock();
        }
        updateAgenda();
    }

    public HashMap<String, HashMap<Date, Integer>> getAgendamentos() {
        return this.mapa_agendamentos;
    }

    public boolean isValid_SNS(int number) {
        return SNS_numbers.contains(number);
    }

    public List<String> getLocais() {
        return this.locais;
    }
}

class Client_conector implements Runnable {
    private Socket socket;
    private VaccinationList vaccinations;
    private HashMap<String, HashMap<Date, Integer>> mapa;

    public Client_conector(Socket s, VaccinationList v) {
        this.socket = s;
        this.vaccinations = v;
    }

    @Override
    public void run() {
        System.out.println("Novo Cliente conectado...");
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMessage, sns;
            int tentativas = 1;
            out.println("Introduza o seu número de utente (SNS): ");
            while ((sns = in.readLine()) != null && !vaccinations.isValid_SNS(Integer.parseInt(sns))) { /* Alterar */
                out.println("Número SNS inválido");
                tentativas++;
                if (tentativas == 4) {
                    out.println("Tentativas ultrapassadas, a disconectar ...");
                    break;
                }
            }

            String[] cmds;
            while ((clientMessage = in.readLine()) != null && tentativas <= 3) {
                mapa = vaccinations.getAgendamentos();
                if (clientMessage.equals("LOCAIS")) {
                    out.println(" *** Locais de vacinação ***\n" + vaccinations.getLocais());
                } else if (clientMessage.contains("AGENDAR ") && (cmds = clientMessage.split(" ")).length == 2) {
                    out.println(agendar(cmds[1], sns));
                } else if (clientMessage.equals("DESMARCAR")) {
                    if (mapa.values().toString().contains(sns)) {
                        desmarcar(sns);
                        out.println("Vacinação desmarcada...");
                    } else {
                        out.println("Não existe nenhuma marcação...");
                    }
                }
            }
            socket.shutdownInput(); socket.shutdownOutput();
            socket.close();
            System.out.println("Cliente disconectado...");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String agendar(String local, String sns) {
        String result = "";
        if (mapa.values().toString().contains(sns)) {
            
            for (String l : mapa.keySet()) {
                for (Date d : mapa.get(l).keySet()) {
                    if (mapa.get(l).get(d) == Integer.parseInt(sns)) {
                        result = "Possui uma vacinação agendada em "+l+": " + d;
                    }
                }
            }
        } else {
            result = "Nova marcação: " + vaccinations.agendar(local, Integer.parseInt(sns));
        }
        return result;
    }

    public void desmarcar(String sns) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss aa", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        String str = "", l = "";
        for (String local : mapa.keySet()) {
            for (Date d : mapa.get(local).keySet()) {
                if (mapa.get(local).get(d) == Integer.parseInt(sns)) {
                    str = formatter.format(d);
                    l = local;
                }
            }
        }
        try {
            vaccinations.desmarcar(l, formatter.parse(str), Integer.parseInt(sns));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    public static void main(String[] args) throws IOException {
        boolean running = true;
        VaccinationList vaccinations = new VaccinationList();

        while (running) {
            ServerSocket serverSocket = new ServerSocket(0);
            System.out.println("(" + serverSocket.getLocalPort() + ") Waiting new client...");
            Socket clientSocket = serverSocket.accept();
            Thread client_connector = new Thread(new Client_conector(clientSocket, vaccinations));
            client_connector.start();
        }
    }

}
