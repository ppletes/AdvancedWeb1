package org.foi.nwtis.ppletes.zadaca_1;

public class Korisnik {

    String ime;
    String lozinka;

    public Korisnik(String ime, String lozinka) {
        this.ime = ime;
        this.lozinka = lozinka;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    @Override
    public String toString() {
        return "\nKorisnik [Korisničko ime: " + ime + "| Korisnička lozinka: " + lozinka + "]";
    }

}
