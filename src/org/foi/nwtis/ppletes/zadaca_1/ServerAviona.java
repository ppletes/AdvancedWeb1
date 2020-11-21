package org.foi.nwtis.ppletes.zadaca_1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.ppletes.konfiguracije.Konfiguracija;
import org.foi.nwtis.ppletes.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ppletes.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.ppletes.konfiguracije.NemaKonfiguracije;
import static org.foi.nwtis.ppletes.zadaca_1.ServisAviona.deSerialization;

public class ServerAviona {

    static int brojVeze = 0;
    static int brojDretvi;
    boolean slijedno = false;
    Avioni avio;
    Konfiguracija konfig;
    public static ServisAviona dAvioni;
    static int dret;

    public static void main(String[] args) {

        ServerAviona posluzitelj = new ServerAviona();
        posluzitelj.izvrsi(args);
    }

    protected void izvrsi(String[] args) {
        Konfiguracija konfig;
        int pauza = 3000;

        String sintaksa = "([^\\s]+\\.(?i)txt|xml)( +([01]?\\d|20))? *$";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (!status || m.group(1) == null || m.group(2) == null) {
            System.out.println("Parametri nisu korektno upisani ili niste upisali sve parametre!");
            return;
        }
        String datotekaKonfig = m.group(1);
        brojDretvi = Integer.parseInt(args[1]);
        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaKonfig);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            System.out.println("Nema konfiguracijske datoteke!");
            return;
        }

        String datotekaKorisnici = konfig.dajPostavku("datoteka.korisnici");
        String datotekaAerodromi = konfig.dajPostavku("datoteka.aerodromi");
        String datotekaAvioni = konfig.dajPostavku("datoteka.avioni");

        if (datotekaKorisnici == null || datotekaAerodromi == null) {
            System.out.println("Nema postavke za datoteku korisnika ili aerodrome!");
            return;
        }

        kolekcijaObjekataKorisnikaIAerodroma();

        int datPort = Integer.parseInt(konfig.dajPostavku("port.avioni"));
        int datCek = Integer.parseInt(konfig.dajPostavku("maks.cekaca"));

        try {
            ServerSocket server = new ServerSocket(datPort, datCek);

            if (datotekaAvioni == null) {
                System.out.println("Ne postoji datoteka sa serijaliziranim podacima aviona!");

                try {
                    konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju("avioni.bin");

                } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
                    Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.out.println("Čekam vezu na vratima: " + datPort);
            System.out.println("Maks broj čekača: " + datCek);

            dAvioni = new ServisAviona(konfig, avio);
            dAvioni.start();

            while (true) {
                Socket veza = server.accept();
                Thread dretva = new PosluziteljDretva(veza, brojDretvi, pauza);
                dretva.start();
                if (slijedno) {
                    dretva.join();
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void kolekcijaObjekataKorisnikaIAerodroma() {
        String nazivDatoteke = "korisnici.xml";
        try {
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
            Properties p = konf.dajSvePostavke();
            for (Object o : p.keySet()) {
                String k = (String) o;
            }
        } catch (NeispravnaKonfiguracija | NemaKonfiguracije ex) {
            System.out.println("Greška kod učitavanja korisnici.xml!" + ex.getMessage());
        }
        File file = new File("aerodromi.csv");
        String line;
        try (BufferedReader br
                = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                List<String> map = new ArrayList<>();
                map.add(line);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    class PosluziteljDretva extends Thread {

        int mojBroj = 0;
        Socket veza;
        int pauza;
        int brojDretvi;
        Konfiguracija konfig;

        public PosluziteljDretva(Socket veza, int brojDretvi, int pauza) {
            this.brojDretvi = brojDretvi; //brojDretvi--
            this.veza = veza;
            mojBroj = ServerAviona.brojVeze++;
            this.pauza = pauza;
        }

        @Override
        public void run() {

            try (
                    BufferedReader inps = new BufferedReader(new InputStreamReader(veza.getInputStream(), "UTF-8"));
                    OutputStream outs = veza.getOutputStream();) {

                StringBuilder tekst = new StringBuilder();

                while (true) {
                    int i = inps.read();
                    if (i == -1) {
                        break;
                    }

                    tekst.append((char) i);
                }

                ispis(tekst.toString());

                if (tekst.toString().contains("KRAJ")) {
                    try {
                        dAvioni = new ServisAviona(konfig, avio);
                        dAvioni.start();
                        System.out.println("\nSerijalizacija uspiješna!");
                        outs.write("OK".getBytes());
                        outs.flush();
                        veza.close();
                    } catch (IOException e) {
                        outs.write("ERROR 11; Nije moguće završiti sa radom servera ili nešto ne valja sa serilizacijom aviona!;".getBytes());
                        outs.flush();
                        System.err.println("ERROR 11; Nije moguće završiti sa radom servera ili nešto ne valja sa serilizacijom aviona!;");
                        System.exit(1);
                    } finally {
                        System.exit(0);
                    }
                }
                if (tekst.toString().contains("DODAJ")) {

                    try {
                        String broj = tekst.substring(tekst.lastIndexOf(" ") + 1);
                        int br = Integer.parseInt(broj);
                        dret = brojDretvi + br;
                        outs.write("OK".getBytes());
                        outs.flush();
                    } catch (IOException e) {
                        outs.write("ERROR 12; Nije moguće povećati broj dretvi;".getBytes());
                        outs.flush();
                        System.err.println("ERROR 12; Nije moguće povećati broj dretvi;");
                    } finally {
                    }

                }
                if (tekst.toString().contains("ISPIS")) {
                    String sin = "(^+KORISNIK[ a-zA-Z0-9\\_\\-]{3,10}; LOZINKA[ a-zA-Z0-9\\_\\-]{3,10}; ISPIS[ a-zA-Z0-9\\_\\-]{3,30};";
                    Pattern p = Pattern.compile("ISPIS[ a-zA-Z0-9\\_\\-]{3,30};");
                    Matcher m = p.matcher(tekst.toString());
                    m.find();

                    String avionn = m.group().substring(m.group().lastIndexOf(" ") + 1);
                    String avion = avionn.substring(0, avionn.length() - 1);
                    try {
                        Avioni avionii = (Avioni) deSerialization();
                        List<Avioni> list2 = new ArrayList();
                        list2.add(avionii);
                        boolean av = list2.stream().map(Avioni::getAvion).filter(avion::equals).findFirst().isPresent();
                        if (av) {
                            String dat = "NWTiS_ppletes_zadaca_1.txt";
                            Konfiguracija konfig;
                            try {
                                String isp = "ISPIS";
                                konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(dat);
                                int datPort = Integer.parseInt(konfig.dajPostavku("port.simulator"));
                                Thread dretvaLet = new ZahtjevAviona(datPort, isp, avion);
                                dretvaLet.start();
                            } catch (NemaKonfiguracije ex) {
                                Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (NeispravnaKonfiguracija ex) {
                                Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            outs.write("OK".getBytes());
                            outs.flush();
                        }
                        veza.close();
                    } catch (IOException e) {
                        outs.write("ERROR 16; Nije moguće dobiti podatke aviona!;".getBytes());
                        outs.flush();
                        System.err.println("ERROR 16; Nije moguće dobiti podatke aviona!;");
                        System.exit(1);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        System.exit(0);
                    }
                }

                if (tekst.toString().contains("UZLETIO")) {
                    String sin = "(^+KORISNIK[ a-zA-Z0-9\\_\\-]{3,10}; LOZINKA[ a-zA-Z0-9\\_\\-]{3,10}; UZLETIO[ a-zA-Z0-9\\_\\-]{3,30}; POLAZIŠTE[ a-zA-Z0-9\\_\\-]{3,10}; ODREDIŠTE[ a-zA-Z0-9\\_\\-]{3,10}; TRAJANJE[ 0-9;]*$)";
                    Pattern p = Pattern.compile("POLAZIŠTE[ a-zA-Z0-9\\_\\-]{3,10};");
                    Pattern p2 = Pattern.compile("ODREDIŠTE[ a-zA-Z0-9\\_\\-]{3,10};");
                    Pattern p3 = Pattern.compile("UZLETIO[ a-zA-Z0-9\\_\\-]{3,30};");
                    Pattern p4 = Pattern.compile("TRAJANJE[ 0-9;]*$");
                    Matcher m = p.matcher(tekst.toString());
                    Matcher m2 = p2.matcher(tekst.toString());
                    Matcher m3 = p3.matcher(tekst.toString());
                    Matcher m4 = p4.matcher(tekst.toString());
                    m.find();
                    m2.find();
                    m3.find();
                    m4.find();
                    String polazistee = m.group().substring(m.group().lastIndexOf(" ") + 1);
                    String polaziste = polazistee.substring(0, polazistee.length() - 1);
                    String odredistee = m2.group().substring(m2.group().lastIndexOf(" ") + 1);
                    String odrediste = odredistee.substring(0, odredistee.length() - 1);
                    String avionn = m3.group().substring(m3.group().lastIndexOf(" ") + 1);
                    String avion = avionn.substring(0, avionn.length() - 1);
                    String trajanjee = m4.group().substring(m4.group().lastIndexOf(" ") + 1);
                    String trajanje = trajanjee.substring(0, trajanjee.length() - 1);

                    String uzl = "UZLETIO";

                    List<Aerodrom> list = Aerodrom.readAerodrom();
                    boolean pol = list.stream().map(Aerodrom::getIcao).filter(polaziste::equals).findFirst().isPresent();
                    boolean odr = list.stream().map(Aerodrom::getIcao).filter(odrediste::equals).findFirst().isPresent();
                    try {
                        if (pol && odr) {

                            Avioni avionii;
                            try {
                                avionii = (Avioni) deSerialization();

                                List<Avioni> list2 = new ArrayList();
                                list2.add(avionii);
                                boolean av = list2.stream().map(Avioni::getAvion).filter(avion::equals).findFirst().isPresent();
                                if (av) {
                                    boolean po = list2.stream().map(Avioni::getAerodromP).filter(polaziste::equals).findFirst().isPresent();
                                    boolean od = list2.stream().map(Avioni::getAerodromS).filter(odrediste::equals).findFirst().isPresent();
                                    if (po && od) {
                                        String portSim = konfig.dajPostavku("port.simulator");
                                        String polijetanje = avionii.getPoljetanja();
                                        String slijetanje = avionii.getSljetanja();

                                        String dat = "NWTiS_ppletes_zadaca_1.txt";
                                        Konfiguracija konfig;
                                        try {
                                            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(dat);
                                            int datPort = Integer.parseInt(konfig.dajPostavku("port.simulator"));
                                            Thread dretvaLet = new ZahtjevAviona(datPort, uzl, avion, polijetanje, slijetanje);
                                            dretvaLet.start();
                                        } catch (NemaKonfiguracije ex) {
                                            Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                                        } catch (NeispravnaKonfiguracija ex) {
                                            Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    } else {
                                        outs.write("ERROR 14;  Nisu isti podaci aerodroma odredišta i polazišta;".getBytes());
                                        outs.flush();
                                        System.out.println("ERROR 14;  Nisu isti podaci aerodroma odredišta i polazišta;");
                                    }
                                } else {
                                    int polijetanje = Integer.parseInt(trajanje);
                                    Calendar calendar = Calendar.getInstance();
                                    Calendar calendar2 = Calendar.getInstance();
                                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                    String formatted2 = format1.format(calendar2.getTime());
                                    calendar.add(Calendar.SECOND, polijetanje);

                                    String formatted = format1.format(calendar.getTime());

                                    //int datPort = Integer.parseInt(konfig.dajPostavku("port.simulator"));
                                    try {
                                        avio = new Avioni(avion, polaziste, formatted2, odrediste, formatted);
                                        ServisAviona.serialization(avio);
                                        String dat = "NWTiS_ppletes_zadaca_1.txt";
                                        Konfiguracija konfig;
                                        try {
                                            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(dat);
                                            int datPort = Integer.parseInt(konfig.dajPostavku("port.simulator"));
                                            Thread dretvaLet = new ZahtjevAviona(datPort, uzl, avion, formatted2, formatted);
                                            dretvaLet.start();
                                        } catch (NemaKonfiguracije ex) {
                                            Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                                        } catch (NeispravnaKonfiguracija ex) {
                                            Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    } catch (IOException ex) {

                                    }
                                }

                            } catch (ClassNotFoundException ex) {
                                Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            veza.close();
                        } else {
                            outs.write("ERROR 13; Icao polazišta ili odredišta nije uredu;".getBytes());
                            outs.flush();
                            System.out.println("ERROR 13; Icao polazišta ili odredišta nije uredu;");
                        }
                    } catch (IOException e) {

                    } finally {

                    }
                }

                if (dret == 0) {

                    outs.write("Povecajte broj dretvi".getBytes());
                    outs.flush();

                }

                veza.shutdownOutput();
                veza.shutdownInput();
                veza.close();
                Thread.sleep(pauza);
            } catch (SocketException e) {
                ispis(e.getMessage());
            } catch (InterruptedException | IOException ex) {
                ispis(ex.getMessage());
            }

        }

        public synchronized void ispis(String tekst) {
            System.out.println("\nPpletes_SD_" + mojBroj + "  " + tekst);
        }
    }
}
