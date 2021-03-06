package pl.autyzmsoft.sylabowiec;


import static pl.autyzmsoft.sylabowiec.MainActivity.getRemovedExtensionName;
import static pl.autyzmsoft.sylabowiec.MainActivity.usunLastDigitIfAny;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.MAXS;

/**
 * Obiekt tej klasy (1 szt.) będzie przechowywał sylaby bieżącego wyrazu.
 * Dodatkowo klasa przechowuje/udostępnia liczbę sylab w bieżącym wyrazie.
 * 2019-03-27
 */
public class Sylaby {

  private String[] tabSylab;     //tablica na przechowywanie sylab
  private int      lSylab;       //liczba sylab w wyrazie


  public int getlSylab() {
    return lSylab;
  }

  public String getSylabaAt(int i) {
  /* Zwraca sylabę z obiektu Sylaby spod indeksu i */
    return tabSylab[i];
  }

  public int dlugoscNajdluzszej() {
  /* Zwraca dlugosc najdluzszej sylaby wyrazu */
    int max = 0, dlug;
    for (int i=0; i<lSylab; i++ ) {
      dlug = tabSylab[i].length();
      if (dlug>max)  max=dlug;
    }
    return max;
  }

  //Konstruktor:
  public Sylaby(String ciag) {
    /**
     * przyklad parametru: "nie-za-po-mi-naj-ki"
    */
    tabSylab = new String[MAXS];
    lSylab   = 0;

    char[] sylaba;                 //na wyluskana sylabę (np. 'chrząszcz')
    char[] lancuch;                //do latwiejszego operowania na 'ciag'u
    boolean koniec;                //do sterowania petla

    ciag = getRemovedExtensionName(ciag); //na wszelki wypadek
    ciag = usunLastDigitIfAny(ciag);      //na wszelki wypadek
    lancuch = ciag.toCharArray();
    sylaba  = "~~~~~~~~~~~~~~".toCharArray();  //wypelniacz...

    int i = -1;  //indeks na lancuch (=ciag) (uwaga na -1 -> ma byc!)
    int j = 0;   //indeks na skłądową tabSylab
    int k = 0;   //indeks na wewn. tablice 'sylaba'

    //Przegladamy caly ciag 'slo-necz-ni-ki' i wyluskujemy sylaby pomiedzy znakami '-' :
    lSylab = 0;
    koniec = false;
    do {
      i = i + 1;
      koniec = (i==ciag.length());
      if (!( (koniec)||(lancuch[i]=='-')) ) { //uwaga - kolejnosc w alternatywie jest istotna (null-owe wartosci!!!)
        sylaba[k] = lancuch[i];
        k++;
      }
      else {
        tabSylab[j] = String.valueOf(sylaba);  //tabSylab[j] = new String(sylaba); -> tak też można...
        tabSylab[j] = tabSylab[j].replace("~","");
        lSylab += 1;
        //budujemy nast. sylabę:
        sylaba = "~~~~~~~~~~~~~~".toCharArray();
        k = 0;
        j++;
      }
    } while (!koniec);
  }  //Constructor()

} //koniec Klasy

