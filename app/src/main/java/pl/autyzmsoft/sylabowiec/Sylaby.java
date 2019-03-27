package pl.autyzmsoft.sylabowiec;

/**
 * Obiekt tej klasy (1 szt.) będzie przechowywał sylaby bieżącego wyrazy.
 * Dodatkowe klasa przechowuje liczbę sylab w bieżącym wyrazie.
 * 2019-03-27
 */
public class Sylaby {

  private String[] tab;     //tablica na przechowywanie sylab
  private int lSylab;       //liczba sylab w wyrazie


  public int getlSylab() {
    return lSylab;
  }


  public Sylaby(String ciag) {

    private char[] sylaba  = new char[0];
    private char[] lancuch = new char[0];

    lancuch = ciag.toCharArray();

    private int i = 0;
    private int j = 0;
    private int k = 0;

    lSylab = 0;
    do {
      sylaba[k] = lancuch[i];
      i++;
      if (lancuch[i]=='-') {
        tab[j] = sylaba.toString();
        lSylab++;
        j++;
        k = 0;
      };
    } while (lancuch[i] != '.');

  }  //Constructor

}
