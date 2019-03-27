package pl.autyzmsoft.sylabowiec;

/**
 * Obiekt tej klasy (1 szt.) będzie przechowywał sylaby bieżącego wyrazy.
 * Dodatkowe klasa przechowuje liczbę sylab w bieżącym wyrazie.
 * 2019-03-27
 */
public class Sylaby {

  private String[] tab;     //tablica na przechowywanie sylab
  private int lSylab;       //liczba sylab w wyrazie


  public Sylaby() {
    tab = new String[0];
    lSylab = 0;
  }

  public int getlSylab() {
    return lSylab;
  }

  public String getSylabaAt(int i) {
  /* Zwraca jedna z sylab w obiekcie Sylaby spod indeksu i */
    return tab[i];
  }


  public void zaladujCiag(String ciag) {

    char[] sylaba  = new char[5];  //na wyluskana sylabę
    char[] lancuch = new char[0];  //do latwiejszego operowania

    lancuch = ciag.toCharArray();

    int i = 0;  //indeks na lancuch (=ciag)
    int j = 0;  //indeks na 'globalną' tab
    int k = 0;  //indeks na tab. sylaba

    lSylab = 0;
    do {
      sylaba[k] = lancuch[i];
      k++;
      i++;
      if (lancuch[i]=='-'||lancuch[i]=='.') {
        tab[j] = sylaba.toString();
        lSylab++;

        sylaba = null; //budujemy nast. sylabę

        j++;
        k = 0;
      }
    } while (lancuch[i] != '.');

  }  //function()

} //koniec Klasy

