package pl.autyzmsoft.sylabowiec;


import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.MAXS;

/**
 * Obiekt tej klasy (1 szt.) będzie przechowywał sylaby bieżącego wyrazy.
 * Dodatkowe klasa przechowuje liczbę sylab w bieżącym wyrazie.
 * 2019-03-27
 */
public class Sylaby {

  private String[] tabSylab;     //tablica na przechowywanie sylab
  private int lSylab;            //liczba sylab w wyrazie



  public int getlSylab() {
    return lSylab;
  }

  public String getSylabaAt(int i) {
  /* Zwraca jedna z sylab w obiekcie Sylaby spod indeksu i */
    return tabSylab[i];
  }



  public Sylaby(String ciag) {
    tabSylab = new String[MAXS];
    lSylab   = 0;

    char[] sylaba;                 //na wyluskana sylabę (np. 'chrząszcz')
    char[] lancuch;                //do latwiejszego operowania na 'ciag'u
    boolean koniec;                //do sterowania petla

    lancuch = ciag.toCharArray();
    sylaba = "^^^^^^^^^".toCharArray();

    int i = -1;  //indeks na lancuch (=ciag) (uwaga na -1 - ma byc)
    int j = 0;   //indeks na 'globalną' tablice tabSylab
    int k = 0;   //indeks na tabSylab. sylaba

    //Przegladamy caly ciag 'slo-necz-ni-ki' i wyluskujemy sylaby pomiedzy znakami '-' :
    lSylab = 0;
    koniec = false;
    do {
      i = i + 1;
      koniec = (i==ciag.length());
      if (!( (koniec)||(lancuch[i]=='-')) )  {
        sylaba[k] = lancuch[i];
        k++;
      }
      else{
        //tabSylab[j] = String.valueOf(sylaba);//sylaba.toString();

        tabSylab[j] = new String(sylaba);
        tabSylab[j] = tabSylab[j].replace("^","");


        lSylab += 1;
        //budujemy nast. sylabę:
        sylaba = "^^^^^^^^^".toCharArray();
        k = 0;
        j++;
      }
    } while (!koniec);
  }  //Constructor()

} //koniec Klasy

