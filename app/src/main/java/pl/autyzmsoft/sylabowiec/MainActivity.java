package pl.autyzmsoft.sylabowiec;

import static android.graphics.Color.RED;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.LATWE;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.MAXS;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.NODISPL;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.NORMAL;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.POSYLAB;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.SREDNIE;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.TRUDNE;
import static pl.autyzmsoft.sylabowiec.ZmienneGlobalne.WSZYSTKIE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

//import android.util.Log;

//Prowadzenie litery po ekranie Wykonalem na podstawie: https://github.com/delaroy/DragNDrop
//YouTube: https://www.youtube.com/watch?v=H3qr1yK6u3M   szukać:android drag and drop delaroy

public class MainActivity extends Activity implements View.OnLongClickListener {

  public static final int DELAY_EXERC = 1000;
  //opoznienie w pokazywaniu rozrzuconych liter i podpisu pod Obrazkiem

  public static final long DELAY_ORDER = 600; //opoznienie uporządkowania Obszaru po Zwyciestwie

  //Pliki dzwiekowe komentarzy-nagrod:
  private static final String DEZAP_SND = "nagrania/komentarze/negatywy/male/nie_e2.m4a";  //dzwiek dezaprobaty, gdy bledny caly wyraz

  private static final String PLUSK_SND = "nagrania/komentarze/efekty/plusk_curbed.ogg";//dzwiek poprawnie polozonej litery

  private static final String BRR_SND = "nagrania/komentarze/efekty/brrr.mp3";  //dzwiek blednie polozonej litery i/lub calego wyrazu

  private static final String DING_SND = "nagrania/komentarze/efekty/ding.mp3";  //dzwiek poprawnie ulozonego wyrazu

  private static final String OKLASKI_SND = "nagrania/komentarze/efekty/oklaski.ogg";  //dzwiek poprawnie ulozonego wyrazu

  public static MojTV[] lbs;

  public static File katalogSD;                 //katalog z obrazkami na SD (internal i external)

  public static String[] listaObrazkowSD = null;

  public static String katalogAssets = null;

  public static String[] listaObrazkowAssets = null;

  public static String[] listaOper = null;

  public static int currImage = -1;      //indeks biezacego obrazka

  public static int density;          //gestosc ekranu - przydatne system-wide

  public static boolean PW = true;    //Pierwsze Wejscie do aplikacji

  //tablica zawierajaca (oryginalne) litery wyrazu; onomastyka: lbs = 'labels'
  static MediaPlayer mp = null;

  //tablica robocza, do dzialań (m.in. latwego wykrycia prawidlowego porzadku ulozenia etykiet
  // w Obszarze); podzbior tab. lbs
  private static MojTV[] lbsRob;

  /*Ponizej, do konca metody onRequestPermissionResult() kod zapewniajacy dostep do kart SD: */
  final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

  public boolean inUp = false; //czy jestesmy w trybie duzych/malych liter

  public String currWord = "*";

  Intent intModalDialog;  //Na okienko dialogu 'modalnego' orzy starcie aplikacji

  Intent intUstawienia;   //Na przywolanie ekranu z ustawieniami

  //wspolrzedne pionowe ygrek Linij Górnej i Dolnej oraz wspolrzedne poziome x linij Lewej i
  // Prawej obszaru 'gorącego'
  TextView tvNazwa;

  ///polozenie  linii 'Trimowania' - srodek Obszaru, do tej linii dosuwam etykiety (kosmetyka
  // znaczaca)
  //Kontener z obrazkiem (do [long]klikania (lepiej niz na image - rozwiazuje problem z niewidzialnym obrazkiem):
  RelativeLayout l_imageContainer;

  //Placeholders'y na etykiety (6 szt.):
  MojTV L00, L01, L02, L03, L04, L05;

  TextView tvInfo, tvInfo1, tvInfo2, tvInfo3;

  TextView tvShownWord; //na umieszczenie wyrazu po Zwyciestwie

  boolean nieGraj = true;

  Button bDajGestosc; //sledzenie

  ZmienneGlobalne mGlob;

  //na przesuwanie w Lewo etykiet z Obszaru (zeby zrobic miejsce z prawej na dalsze ukladanie);
  // przesuniecie w lewo calego Wyrazu; zakladam,ze klawisz czly czas obecny na ekranie
  KombinacjaOpcji currOptions, newOptions;

  Animation animShakeShort, animShakeLong;

  //button na przechodzenie po kolejne cwiczenie
  private ViewGroup rootLayout;

  //na pominiecie/ucieczke z cwiczenia nie konczac go
  //Obrazek i nazwa pod obrazkiem:
  private ImageView imageView;

  private int sizeH, sizeW;    //wymiary Urzadzenia

  //lista obrazkow w katalogu na SD (internal i externa)
  private int _xDelta;

  //Katalogu w Assets, w ktorym trzymane beda obrazki
  private int _yDelta;

  //lista obrazkow z Assets/obrazki - dla wersji demo (i nie tylko...)
  private int yLg, yLd, xLl, xLp;

  //listas 'operacyjna', z niej ostateczne pobieranie obrazkow
  private int yLtrim;

  //przelacznik(semafar) : grac/nie grac - jesli start apk. to ma nie grac slowa (bo glupio..)
  private RelativeLayout.LayoutParams lParams, layoutParams;

  private Button bUpperLower;   //wielkie/male litery

  //bieżacy, wygenerowany wyraz, wziety z currImage; sluzy do porownan; nie jest wyswietlany (w
  // starych wersjach byl...)
  private Button bHint;         //klawisz podpowiedzi

  private Button bAgain;        //wymieszanie liter; klawisz pod Obszarem

  private Button bAgain1;       //wymieszanie liter; klawisz podbDalej

  private Button bShiftLeft;

  //do pamietania przydzielonych obrazkow, zeby w miare mozliwosci nie powtarzaly sie
  private LinearLayout lObszar;

  //'m-member' na zmienne globalne - obiekt singleton klasy ZmienneGlobalne
  private Button bDalej;

  //biezace (obowiazujace do chwili wywolania UstawieniaActivity) ustawienia i najnowsze,
  // ustawione w UstawieniaActivity)
  private Button bPomin;
  //potrzasanie litera[mi] - bledny ciag->short, litera ok->long ; definuję 'wysoko' - wydajnosc

  //obiekt do przechowywania sylab bieżacego wyrazu:
  public Sylaby sylaby;



  /* eksperymenty ze status barem - 2018.08.11 - KONIEC*/
  private Pamietacz mPamietacz;

  private float tvWyrazSize;  //rozmiar wyrazu pod obrazkiem

  private double screenInches;

  private static String[] listaOgraniczonaDoPoziomuTrudnosci(String[] lista, int poziom) {
    /*************************************************************************************/
    /* Ograniczenie Listy obrazkow (Assets bądź SD) do wybranego poziomu.                */
    /* (tworze tablice robocza, a nastepnie lista obrazkow wskaze na tę tablicę roboczą) */
    /*************************************************************************************/

    int dlug_min = 1;
    int dlug_max = MAXS;

    switch (poziom) {
      case LATWE:
        dlug_min = 1;
        dlug_max = 2;
        break;
      case SREDNIE:
        dlug_min = 3;
        dlug_max = 3;
        break;
      case TRUDNE:
        dlug_min = 4;
        dlug_max = MAXS;
        break;
      case WSZYSTKIE:
        dlug_min = 1;
        dlug_max = Integer.MAX_VALUE;
        break; //nazwa dluzsza niz 6 (MAXS) sylab - trzeba ja uwzglednic, bo inaczej pozniej exception.. (potem i tak przytne do 6)
    }

    //Tworze liste robocza:
    //dzieki temu okresle ile jest wymaganych obrazkow, a tym samym bede mial rozmiar tablicy roboczej
    ArrayList<String> lRob = new ArrayList<String>();
    for (String el : lista) {
      String elTmp = getRemovedExtensionName(el);
      int dlug0 = elTmp.length();         //uwaga na kot1
      elTmp = usunLastDigitIfAny(elTmp);  //gdyby byl kot1 to kot1->kot

      Sylaby sylTmp = new Sylaby((elTmp));
      int dlug = sylTmp.getlSylab(); //w wersji Sylabowiec ; w wersji Literowanki->int dlug = elTmp.length();

      //Czysta sytuacja - wyraz miesci sie w kryterium:
      if ((dlug >= dlug_min) && (dlug <= dlug_max)) {
        lRob.add(el); //dodajemy z rozszerzeniem - pelna nazwa pliku!!!
      }
      //Sprawdzamy, bo moze byc 'kot1', 'kot2' .... - taki wyraz, choc dluzszy, trzeba
      // wziac, bo last digit bedzie w przyszlosci wyciety i zostanie 3-literowe kot, tak
      // jak trzeba...
//      else { //2-019-03-31 -> to bylo w Literowance, w Literowcu wylaczam...
//        if (dlug0 == dlug_max + 1) {
//          int idxEnd = dlug - 1;
//          Character lastChar = elTmp.charAt(idxEnd);
//          if (Character.isDigit(lastChar)) {
//            lRob.add(el);
//          }
//        }
//      }
    } //for

    //Przepisanie lRob -> tabRob:
    String[] tabRob = new String[lRob.size()];
    int i = 0;
    for (String s : lRob) {
      tabRob[i] = s;
      i++;
    }

    return tabRob;

  }  //koniec Metody()



  public static String[] findObrazki(File katalog) {
    /*
     * ******************************************************************************************************************* */
    /* Zwraca liste-tablice obrazkow (plikow graficznych .jpg .bmp .png) z katalogu katalogAssets - uzywana tylko dla przypadku SD karty */
    /*
     * ******************************************************************************************************************* */

    //      Wersja ok, ale na <ArrayList<File>:

    ArrayList<File> al = new ArrayList<File>(); //al znaczy "Array List"
    File[] files = katalog.listFiles(); //w files WSZYSTKIE pliki z katalogu (rowniez nieporządane)
    if (files != null) { //lepiej sprawdzic, bo wali sie w petli for na niektorych emulatorach...
      for (File singleFile : files) {
        if ((singleFile.getName().toUpperCase().endsWith(".JPG")) || (singleFile.getName().toUpperCase().endsWith(".PNG")) || (singleFile.getName().toUpperCase()
            .endsWith(".BMP")) || (singleFile.getName().toUpperCase().endsWith(".WEBP")) || (singleFile.getName().toUpperCase().endsWith(".JPEG"))) {
          al.add(singleFile);
        }
      }
    }
    //return al;

    //Przepisanie na tablice stringow:

    String[] wynikowa = new String[al.size()];
    int i = 0;
    for (File file : al) {
      wynikowa[i] = file.getName();
      i++;
    }

    return wynikowa;

/*
        List<String> zawartosc = new ArrayList<String>();
        File[] files = katalog.listFiles(); //w files WSZYSTKIE pliki z katalogu (rowniez
        nieporządane)
        if (files != null) { //lepiej sprawdzic, bo wali sie w petli for na niektorych
        emulatorach...
            for (File singleFile : files) {
                String plikPath = singleFile.getName();
                if ((plikPath.toUpperCase().endsWith(".JPG"))
                        || (plikPath.toUpperCase().endsWith(".PNG"))
                        || (plikPath.toUpperCase().endsWith(".BMP"))
                        || (plikPath.toUpperCase().endsWith(".WEBP"))
                        || (plikPath.toUpperCase().endsWith(".JPEG"))) {
                    zawartosc.add(plikPath);
                }
            }
        }

        return zawartosc;
*/

  }  //koniec Matody()

  public static String getRemovedExtensionName(String name) {
    /**
     * Pomocnicza, widoczna wszedzie metodka na pozbycie sie rozszerzenia z nazwy pliku -
     * dostajemy "goly" wyraz
     */
    String baseName;
    if (name.lastIndexOf(".") == -1) {
      baseName = name;
    } else {
      int index = name.lastIndexOf(".");
      baseName = name.substring(0, index);
    }
    return baseName;
  }  //koniec metody()

  public static String usunLastDigitIfAny(String name) {
    /**
     * Pomocnicza, widoczna wszedzie, usuwa ewentualna ostatnia cyfre w nazwie zdjecia (bo
     * moze byc pies1.jpg, pies1.hjpg. pies2.jpg - rozne psy)
     * Zakladamy, ze dostajemy nazwe bez rozszerzenia i bez kropki na koncu
     */
    int koniec = name.length() - 1;
    if (Character.isDigit(name.charAt(koniec))) {

      return name.substring(0, koniec);
    } else {

      return name;
    }
  } //koniec Metody()

  /**
   * Make a View Blink for a desired duration
   *
   * @param obiekt   Button we blink the text on
   * @param duration for how long in ms will it blink
   * @param offset   start offset of the animation
   * @param ileRazy  ile razy ma mrugnac
   * @param kolor    jakim kolorem ma mrugac zrodlo: https://gist.github .com/cesarferreira/4fcae632b18904035d3b slaby punk: text na buttonie traci "dziewictwo" i pozostaje potem
   *                 nie do ruszenia... -
   *                 dlatego w innych punktach kodu niszcze go i regeneruję
   */

  private static void makeMeBlink(Button obiekt, int duration, int offset, int ileRazy, int kolor) {

    final int savedColor = obiekt.getCurrentTextColor();

    obiekt.setTextColor(kolor);
    Animation anim = new AlphaAnimation(0.0f, 1.0f);
    anim.setDuration(duration);
    anim.setStartOffset(offset);
    anim.setRepeatMode(Animation.REVERSE);
    //anim.setRepeatCount(Animation.INFINITE);
    anim.setRepeatCount(ileRazy);
    obiekt.startAnimation(anim);

    //Przywrocenie pierwotnego koloru klawiszowi po skonczonej animacji:
    final Button finalBtn = obiekt;
    Handler h = new Handler();
    h.postDelayed(new Runnable() {
      @Override
      public void run() {
        finalBtn.setTextColor(savedColor);
      }
    }, duration * (ileRazy + 2) + offset);  //wyr. arytm. - doswiadczalnie....


  } //koniec Metody()

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    /* Wywolywana po udzieleniu/odmowie zezwolenia na dostęp do karty (od API 23 i wyzej) */
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case REQUEST_CODE_ASK_PERMISSIONS: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          //reload my activity with permission granted or use the features what
          // required the permission
        } else {
          //toast("Nie udzieliłeś zezwolenia na odczyt. Opcja 'obrazki z mojego
          // katalogu' nie będzie działać. Możesz zainstalować aplikacje ponownie lub
          // zmienić zezwolenie w Menadżerze aplikacji.");
          wypiszOstrzezenie("Nie udzieliłeś zezwolenia na odczyt. Opcja 'mój katalogAssets' nie " + "będzie działać. Możesz zainstalować aplikację ponownie lub zmienić"
              + " zezwolenie w Menadżerze aplikacji.");
          mGlob.ODMOWA_DOST = true;  //dajemy znać, ze odmowiono dostepu; bedzie potrzebne na
          // Ustawieniach przy próbie wybrania wlasnych zasobow
        }
      }
    }
  } //koniec Metody

  @Override
  public void onCreate(Bundle savedInstanceState) {

   /* ZEZWOLENIA NA KARTE _ WERSJA na MARSHMALLOW, jezeli dziala na starszej wersji, to ten
    kod wykona sie jako dummy */
    int jestZezwolenie = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (jestZezwolenie != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
    }
    /* KONIEC **************** ZEZWOLENIA NA KARTE _ WERSJA na MARSHMALLOW */

    super.onCreate(savedInstanceState);

    //Na caly ekran:
    //1.Remove title bar:
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    //2.Remove notification bar:
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    //3.Set content view AFTER ABOVE sequence (to avoid crash):

    setContentView(R.layout.activity_main);

    mGlob = (ZmienneGlobalne) getApplication();

    rootLayout = (ViewGroup) findViewById(R.id.view_root);

    l_imageContainer = (RelativeLayout) findViewById(R.id.l_imgContainer);
    imageView = (ImageView) rootLayout.findViewById(R.id.imageView);

    tvNazwa = (TextView) findViewById(R.id.tvNazwa);
    lObszar = (LinearLayout) findViewById(R.id.l_Obszar);
    bDalej = (Button) findViewById(R.id.bDalej);
    bPomin = (Button) findViewById(R.id.bPomin);
    bAgain = (Button) findViewById(R.id.bAgain);
    bAgain1 = (Button) findViewById(R.id.bAgain1);
    bShiftLeft = (Button) findViewById(R.id.bShiftLeft);
    tvShownWord = (TextView) findViewById(R.id.tvShownWord);
    bUpperLower = (Button) findViewById(R.id.bUpperLower);
    bHint = (Button) findViewById(R.id.bHint);

    //kontrolki do sledzenia:
    tvInfo = (TextView) findViewById(R.id.tvInfo);
    tvInfo1 = (TextView) findViewById(R.id.tvInfo1);
    tvInfo2 = (TextView) findViewById(R.id.tvInfo2);
    tvInfo3 = (TextView) findViewById(R.id.tvInfo3);
    bDajGestosc = (Button) findViewById(R.id.bDajGestosc);

    przypiszLabelsyAndListenery();

    //animacja na potrzasanie litera[mi]:
    animShakeShort = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shaking_short);
    animShakeLong = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shaking_long);

    //Poprawienie wydajnosci? (zeby w onTouch nie tworzyc stale obiektow) L01 - placeholder
    lParams = (RelativeLayout.LayoutParams) L01.getLayoutParams();
    layoutParams = (RelativeLayout.LayoutParams) L01.getLayoutParams();

    //ustalam polozenie obrazkow - przy pelnej wersji - duuzo więcej... ;):
    katalogAssets = "obrazki_demo_ver";
    if (mGlob.PELNA_WERSJA) {
      katalogAssets = "obrazki_pelna_ver";
    }


    dostosujDoUrzadzen();

    dajWspObszaruInfo();

    pokazUkryjEtykietySledzenia(false);

    resetujLabelsy();

    //Trzeba czekac, bo problemy (doswiadczalnie):
    lObszar.post(new Runnable() {
      @Override
      public void run() {
        ustawLadnieEtykiety();
        ustawWymiaryKlawiszy();
      }
    });

    //Stworzenie statycznej, raz na zawsze(?) listy z Assets:
    tworzListeFromAssets();
    listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM);
    //gdyby byly jakies problemy, to na WSZYSTKIE... :
    if (listaOper.length == 0) {
      mGlob.POZIOM = WSZYSTKIE;
      listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM);
    }

    //ewentualna lista z SD (jezeli ma byc na starcie):
    if (mGlob.ZRODLEM_JEST_KATALOG) {
      tworzListeFromKatalog();
      listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
      //gdyby byly jakies problemy (cos. nie ok. w np. SharedPref) lub zgubiono jakies obrazki, to na WSZYSTKIE...:
      if (listaOper.length == 0) {
        mGlob.POZIOM = WSZYSTKIE;
        listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
      }
    }

    mPamietacz = new Pamietacz(listaOper); //do pamietania przydzielonych obrazkow

    //Zapamietanie ustawien:
    currOptions = new KombinacjaOpcji();
    newOptions = new KombinacjaOpcji();

    dajNextObrazek();                   //daje index currImage obrazka do prezentacji oraz wyraz currWord odnaleziony pod indeksem currImage
    setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone przez currImage
    rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego przez currImage

    pokazModal();                       //startowe okienko modalne z logo i objasnieniami 'klikologii'

  }  //koniec onCreate()

  private void ladowanieDanych() {
  } //koniec metody ladowanieDanych()

  private void tworzListeFromAssets() {
    //Pobranie listy obrazkow z Assets (statyczna, raz na zawsze, wiec najlepiej tutaj):
    AssetManager mgr = getAssets();
    try {
      listaObrazkowAssets = mgr.list(katalogAssets);  //laduje wszystkie obrazki z Assets
    } catch (IOException e) {
      e.printStackTrace();
    }
  }  //koniec Metody()

  public void setCurrentImage() {
    /* Wyrysowanie Obrazka; Odegranie dźwieku; Animacja */

    String nazwaObrazka; //zawiera rozszerzenie (.jpg , .bmp , ...)
    Bitmap bitmap;       //nie trzeba robic bitmapy, mozna bezposrednio ze strumienia, ale
    // bitmap pozwala uzyc bitmat.getWidth() (patrz setCornerRadius())

    if (mGlob.BEZ_OBRAZKOW) {
      imageView.setVisibility(INVISIBLE);
    } else {
      imageView.setVisibility(VISIBLE);
    }

    nazwaObrazka = listaOper[currImage];
    try {
      if (mGlob.ZRODLEM_JEST_KATALOG) { //pobranie z Directory

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        String robAbsolutePath = katalogSD + "/" + nazwaObrazka;
        bitmap = BitmapFactory.decodeFile(robAbsolutePath, options);
        //Wykrycie orientacji i ewentualny obrot obrazka:
        //bitmap = obrocJesliTrzeba(bitmap, robAbsolutePath); - wylaczam, bo chyba(?) nie
        // dziala
      } else {  //pobranie obrazka z Assets
        InputStream streamSki = getAssets().open(katalogAssets + "/" + nazwaObrazka);
        bitmap = BitmapFactory.decodeStream(streamSki);

        /********* obrazek "klasyczny", bez rounded corners (wersja poprzednia): ***********
         a) - z Assets:
         Drawable drawable = Drawable.createFromStream(stream, null);
         imageView.setImageDrawable(drawable);

         b) - z SD:
         imageView.setImageBitmap(bitmap);
         ******* obrazek "klasyczny" - koniec ******************/
      }

      /******* rounded corners 2018.08.03 *************/
      RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), bitmap); //ostatnim parametrem moglby byc stremSki (patrz wyzej)
      dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 20.0f);
      imageView.setImageDrawable(dr);
      /******* rounded corners koniec *************/

      //Pokazania obrazka z 'efektem' :
      if (!mGlob.BEZ_OBRAZKOW) { //trzeba sprawdzic warunek, bo animacja pokazalaby obrazek
        // na chwile...
        Animation a = AnimationUtils.loadAnimation(this, R.anim.skalowanie);
        imageView.startAnimation(a);
      }
      //Ewentualna nazwa pod obrazkiem (robie tutaj, bo lepszy efekt wizualny niż gdzie indziej):
      if (mGlob.JAK_WYSW_NAZWE != NODISPL) {
        pokazUkryjNazwe();
        Animation b = AnimationUtils.loadAnimation(this, R.anim.skalowanie);
        tvNazwa.startAnimation(b);
      }

    } catch (Exception e) {
      Log.e("4321", e.getMessage());
      Toast.makeText(this, "Problem z wyswietleniem obrazka...", Toast.LENGTH_SHORT).show();
    }

    //ODEGRANIE DŹWIĘKU
    odegrajWyraz(400);

  }  //koniecMetody()

  private void odegrajWyraz(int opozniacz) {
    /*************************************************/
    /* Odegranie prezentowanego wyrazu               */
    /*************************************************/
    //najpierw sprawdzam, czy trzeba:
    //Jezeli w ustawieniech jest, zeby nie grac - to wychodzimy:
    if (mGlob.BEZ_DZWIEKU == true) {
      return;
    }
    //zeby nie gral zaraz po po starcie apki:
    if (nieGraj) {
      nieGraj = false;
      return;
    }
    //Granie wlasciwe:

    String nazwaObrazka = listaOper[currImage];
    String rdzenNazwy = usunLastDigitIfAny(getRemovedExtensionName(nazwaObrazka));
    if (!mGlob.ZRODLEM_JEST_KATALOG) {
      //odeggranie z Assets (tam TYLKO ogg):
      String sciezka_do_pliku_dzwiekowego = "nagrania/wyrazy/" + rdzenNazwy + ".ogg";
      odegrajZAssets(sciezka_do_pliku_dzwiekowego, opozniacz);
    } else {  //pobranie nagrania z directory
      //odegranie z SD (na razie nie zajmujemy sie rozszerzeniem=typ pliku dzwiekowego jest
      // (prawie) dowolny):
      String sciezka_do_pliku_dzwiekowego = katalogSD + "/" + rdzenNazwy; //tutaj przekazujemy rdzen nazwy, bez rozszerzenia, bo mogą być
      // różne (.mp3, ogg, .wav...)
      odegrajZkartySD(sciezka_do_pliku_dzwiekowego, opozniacz);
    }
  }  //koniec Metody()

  private void odegrajZAssets(final String sciezka_do_pliku_parametr, int delay_milisek) {
    /* ***************************************************************** */
    // Odegranie dzwieku umieszczonego w Assets (w katalogu i podkatalogach 'nagrania'):
    /* ***************************************************************** */

    if (mGlob.nieGrajJestemW105) {
      return; //na czas developmentu....
    }

    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        try {
          if (mp != null) {
            mp.release();
            mp = new MediaPlayer();
          } else {
            mp = new MediaPlayer();
          }

          String Msciezka_do_pliku_parametr = sciezka_do_pliku_parametr.replace("-",""); //przystosowanie Literowiec->Sylabowiec

          final String sciezka_do_pliku = Msciezka_do_pliku_parametr; //udziwniam, bo klasa wewn. i kompilator sie czepia....
          AssetFileDescriptor descriptor = getAssets().openFd(sciezka_do_pliku);
          mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
          descriptor.close();
          mp.prepare();
          mp.setVolume(1f, 1f);
          mp.setLooping(false);
          mp.start();
          //Toast.makeText(getApplicationContext(),"Odgrywam: "+sciezka_do_pliku,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
          //Toast.makeText(getApplicationContext(), "Nie można odegrać pliku z dźwiękiem.", Toast.LENGTH_LONG).show();
          e.printStackTrace();
        }
      }
    }, delay_milisek);
  } //koniec Metody()


  private void grajSylabe(String sylaba) {
    if (!mGlob.ZRODLEM_JEST_KATALOG)
      grajSylabeZAssets(sylaba);
    else
      getGrajSylabeZKatalogu(sylaba);
    return;
  }

  private void getGrajSylabeZKatalogu(String sylaba) {
    /**
     * na razie puste; jak grajSylabeZAsstes()-> TO DO
      */
    return;
  }


  private void grajSylabeZAssets(String sylaba) {
    /**
     * Odegranie Sylaby; nie używam odegrajZAssets() w sposob bezposredni,
     * bo mogą się zdarzyć wyrazy 1-dno sylabowe, a przy takich (często) nie ma
     * sylaby w /nagrania/sylaby/... , ale jest caly wyraz w nagrania/wyrazy...
     * Wtedy warto sprawdzić /nagrania/wyrazy - wieksze p-stwo, ze odegramy dzwiek
     */

    String pliczek = sylaba + ".ogg";
    try {
      if ( Arrays.asList(getResources().getAssets().list("nagrania/sylaby")).contains(pliczek) )
          odegrajZAssets("nagrania/sylaby/"+pliczek,0);
      else //jak nie ma w kat. /sylaby , to sprawdzam w kat. /wyrazy - moze tam jest....
          odegrajZAssets("nagrania/wyrazy/"+pliczek,0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }



  private void odegrajZkartySD(final String sciezka_do_pliku_parametr, int delay_milisek) {
    /* ************************************** */
    /* Odegranie pliku dzwiekowego z karty SD */
    /* ************************************** */

    if (mGlob.nieGrajJestemW105) {
      return; //na czas developmentu....
    }

    //Na pdst. parametru metody szukam odpowiedniego pliku do odegrania:
    //(typuję, jak moglby sie nazywac plik i sprawdzam, czy istbieje. jezeli istnieje - OK,
    // wychodze ze sprawdzania majac wytypowaną nazwe pliku)
    String pliczek;
    pliczek = sciezka_do_pliku_parametr + ".m4a";
    File file = new File(pliczek);
    if (!file.exists()) {
      pliczek = sciezka_do_pliku_parametr + ".mp3";
      file = new File(pliczek);
      if (!file.exists()) {
        pliczek = sciezka_do_pliku_parametr + ".ogg";
        file = new File(pliczek);
        if (!file.exists()) {
          pliczek = sciezka_do_pliku_parametr + ".wav";
          file = new File(pliczek);
          if (!file.exists()) {
            pliczek = sciezka_do_pliku_parametr + ".amr";
            file = new File(pliczek);
            if (!file.exists()) {
              pliczek = ""; //to trzeba zrobic, zeby 'gracefully wyjsc z metody (na
              // Android 4.4 sie wali, jesli odgrywa plik nie istniejacy...)
              //dalej nie sprawdzam/nie typuję... (na razie) (.wma nie sa
              // odtwarzane na Androidzie)
            }
          }
        }
      }
    }
    //Odegranie znalezionego (if any) poliku:
    if (pliczek.equals("")) {
      return;  //bo Android 4.2 wali sie, kiedy próbujemy odegrac plik nie istniejący
    }
    Handler handler = new Handler();
    final String finalPliczek = pliczek; //klasa wewnetrzna ponizej - trzeba "kombinowac"...
    handler.postDelayed(new Runnable() {
      public void run() {
        try {
          Uri u = Uri.parse(finalPliczek); //parse(file.getAbsolutePath());
          mp = MediaPlayer.create(getApplicationContext(), u);
          mp.start();
        } catch (Exception e) {
          //toast("Nie udalo się odegrać pliku z podanego katalogu...");
          Log.e("4321", e.getMessage()); //"wytłumiam" komunikat
        } finally {
          //Trzeba koniecznie zakonczyc Playera, bo inaczej nie slychac dzwieku:
          //mozna tak:
          mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
              mp.release();
            }
          });
          //albo mozna tak:
          //mPlayer.setOnCompletionListener(getApplicationContext()); ,
          //a dalej w kodzie klasy zdefiniowac tego listenera, czyli public void
          // onCompletion(MediaPlayer xx) {...}
        }
      }
    }, delay_milisek);
  } //koniec metody odegrajZkartySD

  private void dajNextObrazek() {
    //Daje index currImage obrazka do prezentacji oraz wyraz currWord odnaleziony pod
    // indeksem currImage;
    //Na podst. currImage ustawia nazwe currWord.

    currImage = dajLosowyNumerObrazka();

    //Nazwe odpowiadajacego pliku oczyszczamy z nalecialosci:

    String nazwaPliku = listaOper[currImage];

    nazwaPliku = getRemovedExtensionName(nazwaPliku);
    nazwaPliku = usunLastDigitIfAny(nazwaPliku);

    //Uwaga - Uwaga : przyciecie do 12 liter !!!!
    currWord = nazwaPliku.substring(0, Math.min(19, nazwaPliku.length())); //19, bo max. nie-za-po-mi-naj-ki)

    //Odsiewam/zamieniam ewentualne spacje z wyrazu bo problemy :
    if (currWord.contains(" ")) {
      //currWord = currWord.replaceAll("\\s","");
      currWord = currWord.replaceAll("\\s", "_");
    }

  } //koniec Metody()

  private void pokazUkryjNazwe() {
    //Umieszcza nazwę pod obrazkiem (jesli ustawiono w ustawieniach)

    tvNazwa.setVisibility(INVISIBLE);  //wymazanie (rowniez) ewentualnej poprz. nazwy

    if (mGlob.JAK_WYSW_NAZWE == NODISPL) {
      return;
    }

    switch (mGlob.JAK_WYSW_NAZWE) {
      case NORMAL:
        String str = currWord;
        str = str.replace("-","");
        tvNazwa.setText(str);
        break;
      case POSYLAB:
        tvNazwa.setText(currWord); //currWord jest posylabowany, nie robimy nic
        break;
    }

    if (inUp) {
      tvNazwa.setText(tvNazwa.getText().toString().toUpperCase(Locale.getDefault()));
    }
    tvNazwa.setVisibility(VISIBLE);

  }  //koniec Metedy()



  private void rozrzucWyraz() {
    /* Rozrzucenie currWord po tablicy lbs (= po Ekranie)              */
    //Wyswietla tez nazwe pod obrazkiem

    //bDajGestosc.setText("TV :   Ol: "); //sledzenie

    //currWord = "SPODNIE";
    //currWord = "nie";
    //currWord = "ABCDEFGHIJKL";
    //currWord = "cytryna";
    //currWord = "************";
    //currWord   = "abcdefghijkl";
    //currWord   = "pomarańczowy";
    //currWord   = "niedźwiedzie";
    //currWord   = "rękawiczki";
    //currWord   = "jękywiłzkóśp";
    //currWord   = "wwwwwwwwwwww";
    //currWord   = "WWWWWWWWWWWW";
    //currWord   = "mmmmmmmmmmmm";
    //currWord   = "tikjńfźlóśżk";
    //currWord   = "mikrofalówka";
    //currWord   = "pies";
    //currWord   = "mmmm";
    //currWord   = "Mikołaj";
    //currWord   = "Mikołajm";
    //currWord   = "lalka";
    //currWord   = "jabłko";
    //currWord   = "słoneczniki";
    //currWord   = "podkoszulek";
    //currWord   = "ogórek";
    //currWord   = "makaron";
    //currWord   = "zegar";
    //currWord   = "nóż";
    //currWord   = "kot";
    //currWord   = "huśtawka";
    //currWord   = "buty";
    //currWord   = "W";
    //currWord   = "ze spacjom";
    //currWord   = "0123456789AB";
    //currWord   = "chrząszcz-chrząszcz-89AB-abcd-efghj-chleb";
    //currWord   = "1hrząszcz-2hrząszcz-3hrząszcz-4hrząszcz-5hrząszcz-6hrząszcz";
    //currWord   = "nie-za-po-mi-naj-ki";
    //currWord     = "Mi-ko-łaj";
    //currWord     = "chrząszcz";
    //currWord     = "a-a-a-a-a-a";
    //currWord     = "chleb-chleb-chleb-chleb-chleb-chleb";
    //currWord   = "chrząszcz-chrząszcz-chrząszcz-chrząszcz-chrząszcz-chrząszcz";



    //Pobieramy wyraz do rozrzucenia:
    sylaby = new Sylaby(currWord);

    final Random rand = new Random();
    final Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.skalowanie);
    a.setDuration(500);

    //Pokazujemy z lekkim opoznieniem (efekciarstwo...):
    Handler mHandl = new Handler();
    mHandl.postDelayed(new Runnable() {
      @Override
      public void run() {
        //Kazda sylaba wyrazu ląduje w losowej komorce tablicy lbs :
        for (int i = 0; i < sylaby.getlSylab(); i++) {
          String z = sylaby.getSylabaAt(i); //pobranie sylaby
          //String z = Integer.toString(i); //do celów testowania

          //Losowanie pozycji w tablicy lbs:
          int k;  //na losową pozycję
          do {
            k = rand.nextInt(lbs.length);
          } while  ( (lbs[k].getVisibility() == VISIBLE) ); //petla gwarantuje, ze trafiamy tylko w puste (=niewidoczne) etykiety

          //Umieszczenie sylaby na wylosowanej pozycji (i w strukturze obiektu MojTV) + pokazanie:
          lbs[k].setOrigSyl(z);
          lbs[k].setText(z);
          lbs[k].setTextColor(Color.BLACK);  //kosmetyka, ale wazna...
          lbs[k].setVisibility(VISIBLE);

          //podpiecie animacji:
          lbs[k].startAnimation(a);
        } //for
        if (inUp)   //ulozylismy z malych (oryginalnych) liter. Jesli trzeba - podnosimy
        {
          podniesLabels();
        }
      }  //run()
    }, DELAY_EXERC);

    //Odblokowanie dodatkowych klawiszy - chwilke po pokazaniu liter (lepszy efekt):
    Handler mH2 = new Handler();
    mH2.postDelayed(new Runnable() {
      @Override
      public void run() {
        odblokujZablokujKlawiszeDodatkowe();
      }
    }, 2 * DELAY_EXERC);

  } //koniecMetody();


  private void resetujLabelsy() {
    //Resetowanie tablicy i tym samym zwiazanycyh z nia kontrolek ekranowych:
    for (MojTV lb : lbs) {
      lb.setText("*");
      lb.setOrigSyl("*");
      lb.setInArea(false);
      lb.setVisibility(INVISIBLE);
    }
  }

  public void bDalejOnClick(View v) {
    /* ***************************** */
    /* Przejscie do nowego cwiczenia */
    /* ***************************** */
    //sledzenie:
    //bUpperLower.setText(sizeW+"x"+sizeH);

    //Usuniecie Grawitacji z lObszar, bo mogla byc ustawiona w korygujJesliWystaje() ):
    usunGrawitacje();

    blokujKlawiszeDodatkowe();

    resetujLabelsy();
    ustawLadnieEtykiety();
    dajNextObrazek();                   //daje indeks currImage obrazka do prezentacji oraz currWord = nazwa obrazka bez nalecialosci)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //efekciarstwo
      getAnimatorSkib(imageView, 300).start();
      getAnimatorSkib(tvNazwa, 300).start();
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone przez currImage
          rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego przez currWord
        }
      }, 500);
    } else {
      setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone
      // przez currImage
      rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego
      // pr888888888888888888888zez currWord
    }

    tvShownWord.setVisibility(INVISIBLE);

    //Wygaszenie bDalej i bAgain1:
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //efekciarstwo
      getAnimatorSkib(bAgain1, 500).start();
      getAnimatorSkib(bDalej, 500).start();
    } else {
      bDalej.setVisibility(INVISIBLE);
      bAgain1.setVisibility(INVISIBLE);
    }

  } //koniecMetody()


  private void unieczynnijNaChwile(final Button klawisz, int chwila) {
    /**
     * Na chwile unieczynnnia podany klawisz
     * Uzywana w bAgainObCkick(), zeby zapobiec problemom gdy 2 szybkie kliki na klawiszu bAgain1
     */
    klawisz.setEnabled(false);
      Handler mHandl = new Handler();
      mHandl.postDelayed(new Runnable() {
          @Override
          public void run() {
              klawisz.setEnabled(true);
          }
      }, chwila);
  }

  public void bAgainOnClick(View v) {
    //bAgain -  kl. pod Obszarem
    //bAgain1 - kl. pod bDalej

    //trzeba zabokowac na chwilke, bo 2 szybkie kliki sa wylapywane i kaszana... (2019.04)
    if (v==bAgain1) unieczynnijNaChwile(bAgain1,500);

    //Usuniecie Grawitacji z lObszar, bo mogla byc ustawiona w korygujJesliWystaje() ):
    usunGrawitacje();

    blokujKlawiszeDodatkowe();

    ustawLadnieEtykiety();
    resetujLabelsy();
    rozrzucWyraz();

    tvShownWord.setVisibility(INVISIBLE);

    //Wygaszenie klawiszy bAgain1 i bDalej (jezeli mozliwe, z efektem ;) ):
    //(bAgain 'zalatwiany' przez blokujKlawiszeDodatkowe() powyżej)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //efekciarstwo
      getAnimatorSkib(bAgain1, 500).start();
      getAnimatorSkib(bDalej, 500).start();
    } else {
      bAgain1.setVisibility(INVISIBLE);
      bDalej.setVisibility(INVISIBLE); //gdyby byl widoczny
    }

  }  //koniec Metody()

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @NonNull
  private Animator getAnimatorSkib(final View v, long duration) {
    //Tworzy animacje 'zanikajacy klawisz'; 'bajer... na pdst. Android Big Nerd Ranch str. 150
    int cx = v.getWidth() / 2;
    int cy = v.getHeight() / 2;
    float radius = v.getWidth();
    Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, radius, 0);
    anim.setDuration(duration);
    anim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        v.setVisibility(INVISIBLE);
      }
    });
    return anim;
  } //koniec Metody()

  private void blokujKlawiszeDodatkowe() {
    bPomin.setEnabled(false);
    bUpperLower.setEnabled(false);

    bAgain.setEnabled(false);
    bAgain.setText(""); //czyszcze, bo cos moze zostac po animacji.... (patrz opi MakeMeBlink()

    bHint.setEnabled(false);
    //setEnabled(false);
  }

  public void bPominOnClick(View v) {
    bDalej.callOnClick();
  }

  public void bUpperLowerOnClick(View v) {
    //Zmiana male/duze litery (w obie strony)

    inUp = !inUp;

    odegrajZAssets(PLUSK_SND, 0);

    //Kosmetyka - zmiana symbolu na buttonie:
    if (!inUp) {
      ((Button) v).setText("-----");
    } else {
      ((Button) v).setText("|");
    }

    //1.Wyraz juz ulozony:
    if (tvShownWord.getVisibility() == VISIBLE) {
      if (inUp) {
        podniesWyraz();
      } else {
        restoreOriginalWyraz();
      }
    }
    //2.Wyraz jeszcze nie ulozony:
    else {
      if (inUp) {
        podniesLabels();
      } else {
        restoreOriginalLabels();
      }
    }

  } //koniec Metody()

  private void podniesLabels() {
    //Etykiety podnoszone są do Wielkich liter
    //Oryginałów l.origL - nie ruszam!!!

    String coWidac;
    for (MojTV lb : lbs) {
      if (!lb.getOrigSyl().equals("*")) { //(lb.getVisibility()== View.VISIBLE) {
        coWidac = lb.getText().toString();
        coWidac = coWidac.toUpperCase(Locale.getDefault());
        lb.setText(coWidac);
      }
    }
    tvNazwa.setText(tvNazwa.getText().toString().toUpperCase(Locale.getDefault())); //podniesienie nazwy pod Obrazkiem
  } //koniec Metody()

  private void restoreOriginalLabels() {
    //Etykiety przywracane są do oryginalnych (małych) liter
    //Uwzględnia to problem MIKOŁAJ->Mikołaj

    String coPokazac;
    for (MojTV lb : lbs) {
      if (!lb.getOrigSyl().equals("*")) { //(lb.getVisibility()==View.VISIBLE) {
        coPokazac = lb.getOrigSyl();
        lb.setText(coPokazac);
      }
    }
    tvNazwa.setText(currWord);  //nazwa pod Obrazkiem wraca do oryginalnych liter
  } //koniec Metody()

  private void podniesWyraz() {
    //Poprawny wyraz z Obszaru podnoszony do Wielkich liter
    //Ewentualne odsuniecie, jezeli wyraz po powiekszeniu wychodzi za prawa krawedz Obszaru

    String coWidac = tvShownWord.getText().toString();
    coWidac = coWidac.toUpperCase(Locale.getDefault());
    tvShownWord.setText(coWidac);

    tvNazwa.setText(tvNazwa.getText().toString().toUpperCase(Locale.getDefault())); //podniesienie nazwy pod Obrazkiem

    korygujJesliWystaje();

  }  //koniec Metody()

  private void korygujJesliWystaje() {
    //Robimy w post bo trzeba zaczekac, az tvShownWord pojawi sie na ekranie.

    tvShownWord.post(new Runnable() {
      @Override
      public void run() {
        int rightT = tvShownWord.getRight();  //T - od 'textV..'
        int rightL = lObszar.getRight();      //L - od 'lOb...'

        //bDajGestosc.setText("TV : "+Integer.toString(rightT)+" Ol :"+Integer.toString(rightL)); //sledzenie

        if (rightT >= rightL) {
          addGravityToParent();
        }
      }
    });

  } //koniec Metody()

  private void restoreOriginalWyraz() {
    /**
     *Wyraz z Obszaru zmniejszany jest do małych (scislej: oryginalnych) liter.
     *Uwzględnia to problem MIKOŁAJ->Mikołaj
     *Wywolywane w kontekscie zmiany z Wielkich->małe, wiec staram sie, zeby wyraz z malymi
     *literami rozpoczynal sie tam, gdzie zaczynal sie wyraz  "macierzysty" (jezeli wyraz<10 znakow)
     */


    String coPokazac = currWord.replace("-","");
    tvShownWord.setText(coPokazac);

    //Jezeli wyraz nie jest zbyt dlugi, to wyraz zacznie sie tam, gdzie zaczynal sie wyraz z
    // Wielimi literami
    //(przy b.dlugich wyrazach nie mozna sobie na to pozwolic - patrz 'niedziedzie' przy
    // zmianie Wielki->male nie miesci sie w Obszarze(!)
    //(wieloliterowy wyraz malymi literami moze byc dluzszy niz ten sam wyraz Wielkimi, bo
    // wielki ma usuniety letterSpacing(!)):
    final int pocz = tvShownWord.getLeft();
    if (currWord.length() < 10) {
      tvShownWord.post(new Runnable() {
        @Override
        public void run() {
          tvShownWord.setLeft(pocz);
        }
      });
    }
    tvNazwa.setText(currWord); //nazwa pod Obrazkiem wraca do malyh liter
  } //koniec Metody()

  public void bDajWielkoscEkranuOnClick(View v) {

    //        bDalej.getLayoutParams().height = yLg;
    //        bDalej.requestLayout();

    int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

    String toastMsg;
    switch (screenSize) {
      case Configuration.SCREENLAYOUT_SIZE_XLARGE:
        toastMsg = "XLarge screen";
        break;
      case Configuration.SCREENLAYOUT_SIZE_LARGE:
        toastMsg = "Large screen";
        break;
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:
        toastMsg = "Normal screen";
        break;
      case Configuration.SCREENLAYOUT_SIZE_SMALL:
        toastMsg = "Small screen";
        break;
      default:
        toastMsg = "Screen size is neither xlarge, large, normal or small";
    }
    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
  } //koniec Metody()

  private void dajWspObszaruInfo() {
    //Daje wspolrzedne prostokatnego Obszaru, gdzie ukladany jest wyraz

    lObszar.post(new Runnable() { //czekanie az obszar sie narysuje
      @Override
      public void run() {
        int[] location = new int[2];
        lObszar.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        //Przekazanie do zmiennych Klasy parametrow geograficznych Obszaru:
        xLl = x;
        yLg = y;
        xLp = xLl + lObszar.getWidth();
        yLd = yLg + lObszar.getHeight();
        //Przekazanie do zmiennek klasy współrzędnej y linii 'Trymowania':
        yLtrim = yLg + ((int) ((yLd - yLg) / 2.0));
      }
    });
  } //koniec Metody()

  private void pokazUkryjEtykietySledzenia(boolean czyPokazac) {
    int rob;
    rob = INVISIBLE;
    if (czyPokazac) {
      rob = VISIBLE;
    }
    tvInfo.setVisibility(rob);
    tvInfo1.setVisibility(rob);
    tvInfo2.setVisibility(rob);
    tvInfo3.setVisibility(rob);
  } //koniec Metody();

  public void bDajGestoscOnClick(View view) {
    int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

    int density = getResources().getDisplayMetrics().densityDpi;
    switch (density) {
      case DisplayMetrics.DENSITY_LOW:
        Toast.makeText(this, "LDPI", Toast.LENGTH_SHORT).show();
        break;
      case DisplayMetrics.DENSITY_MEDIUM:
        Toast.makeText(this, "MDPI", Toast.LENGTH_SHORT).show();
        break;
      case DisplayMetrics.DENSITY_HIGH:
        Toast.makeText(this, "HDPI", Toast.LENGTH_SHORT).show();
        break;
      case DisplayMetrics.DENSITY_XHIGH:
        Toast.makeText(this, "XHDPI", Toast.LENGTH_SHORT).show();
        break;
      case DisplayMetrics.DENSITY_XXHIGH:
        Toast.makeText(this, "XXHDPI", Toast.LENGTH_SHORT).show();
        break;
      case DisplayMetrics.DENSITY_XXXHIGH:
        Toast.makeText(this, "XXXHDPI", Toast.LENGTH_SHORT).show();
        break;
      case DisplayMetrics.DENSITY_560:
        Toast.makeText(this, "560 ski ski", Toast.LENGTH_SHORT).show();
        break;
      default:
        Toast.makeText(this, "nie znalazłem...", Toast.LENGTH_SHORT).show();
    }

  } //koniec Metody()

  @Override
  /**
   * dotyczy: imageView
   * Co na dlugim kliknieciu na obrazku - powolanie ekranu z opcjami
   */ public boolean onLongClick(View view) {
    intUstawienia = new Intent("pl.autyzmsoft.sylabowiec.UstawieniaActivity");
    startActivity(intUstawienia);
    return true;
  } //koniec Metody()

  private void przesunWLewo(int dx) {
    /*
     * ************************************************************************************************************* */
        /* WSZYSKIE etykiety z Obszaru zostają przesuniete w lewo o dx, zeby zrobic wiecej
        miejsca z prawej na układanie */
    /*
     * ************************************************************************************************************* */
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        //lb.setLeft(lb.getLeft()-x); - to nie jest dobre, bo nie ma czegos w rodzaju
        // 'commit'owania'... (patrz nizej)
        RelativeLayout.LayoutParams lPar = (RelativeLayout.LayoutParams) lb.getLayoutParams();
        lPar.leftMargin -= dx;
        lb.setLayoutParams(lPar);       //"commit" na View, view bedzie siedzial 'twardo'
      }
    }
  } //koniec Metody()

  public void bShiftLeftOnClick(View view) {
    /* ***************************************************************************/
    /* Ścieśnienie lub Przesuniecie w lewo etykiet bądź gotowego wyrazu          */
    /* Jezeli wyraz ulozony - przesuwamy wyraz;                                  */
    /* jezeli nie ulozony - sciesniam/przesuwam etykiety                         */
    /* ***************************************************************************/

    if (ileWObszarze() == 0 && (tvShownWord.getVisibility() != VISIBLE)) {
      return; //kiedy nic nie ma w Obszarze - nie robie nic
    }

    int x;
    //ewentualne przesuniecie calego wyrazu:
    if (tvShownWord.getVisibility() == VISIBLE) {
      usunGrawitacje();           //usuniecie Grawitacji z lObszar, bo mogla byc ustawiona
      // w korygujJesliWystaje() i przeszkodzilaby w przesunieciu tvShownWord
      x = tvShownWord.getLeft();
      x = x / 2;                  //Przesuwamy o polowe dystansu do lewego brzegu Obszaru
      //tvShownWord.setLeft(x);  //ta instrukcja nie 'commituje', tylko na oglad
      // 'tymczasowy', ponizej OK
      LinearLayout.LayoutParams lPar = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
      lPar.leftMargin = x;
      tvShownWord.setLayoutParams(lPar);       //"commit" na View, view bedzie siedzial 'twardo'
    }
    //ewentualne przesuniecie etykiet:
    else {
      boolean sciesnil = likwidujBiggestGap();
      if (!sciesnil) { //przesuwamy Wszystko w lewo
        MojTV mojTV = dajLeftmostInArea();  //skrajna lewa
        x = mojTV.getLeft();
        x = x / 2;
        przesunWLewo(x);
      }
    }
  }  //koniec Metody()

  private boolean likwidujBiggestGap() {
    /***********************************************************************************/
    /* Zmiejsza odcinek zajmowany przez litery w Obszarze.                             */
    /* Zasada: wyszukuje najwieksza dziurę i likwiduja ją.                             */
    /* Likwidacja dziury poprzez przesuniecie etykiet z prawej w lewo o jej szerokosc. */
    /* Jezeli nie znajdzie dziury>0 , zwraca false. Uwaga - padding brany pod uwage.   */
    /***********************************************************************************/

    int licznik = ileWObszarze(); //jednoczesnie licznosc lRob[] ponizej (=ile w Obszarze);

    if (licznik == 0) {
      return false; //jak pusty Obszar lub ulozono wyraz - nie robie nic
    }

    MojTV[] tRob = posortowanaTablicaFromObszar();     //tablica robocza, do dzialań
    //Szukamy najwiekszej 'dziury' pomiedzy literami w Obszarze:
    int maxDX = Integer.MIN_VALUE;
    int wsk = 0;
    for (int i = 0; i < licznik - 1; i++) {
      int dx = tRob[i + 1].getLeft() - tRob[i].getRight(); //uwaga - tutaj wchodzi tez padding
      if (dx > maxDX) {
        maxDX = dx;
        wsk = i + 1; //najwieksza dziura jest na lewo od tego indeksu
      }
    }
    //Przesuwamy/sciesniamy litery:
    if (maxDX > 0) {
      //Sciesniamy (wszystkie na prawo od wsk (wlacznie z wsk) pojda w lewo ):
      for (int i = wsk; i < licznik; i++) {
        RelativeLayout.LayoutParams lPar = (RelativeLayout.LayoutParams) tRob[i].getLayoutParams();
        lPar.leftMargin -= maxDX;
        tRob[i].setLayoutParams(lPar);       //"commit" na View, view bedzie siedzial 'twardo'
      }
    }
    return (maxDX > 0); //bylo/nie bylo scieśniania
  } //koniec metody

  private void Zwyciestwo() {
    /* **************************************************************** */
    /* Dzialania po Zwyciestwie = poprawnym polozeniu ostatniej sylaby: */
    /* Porzadkowanie Obszaru, blokowanie klawiszy, dzwieki              */
    /* **************************************************************** */
    if (mGlob.SND_VICTORY_EF) {
      odegrajZAssets(DING_SND, 10);
    }
    //
    dajNagrode(); //nagroda dzwiekowa (ewentualna)
    //
    //Zeby w (krotkim) czasie DELAY_ORDER nie mogl naciskac - bo problemy(!) :
    blokujKlawiszeDodatkowe();
    //
    //uporzadkowanie w Obszarze z lekkim opoznieniem:
    Handler mHandl = new Handler();
    mHandl.postDelayed(new Runnable() {
      @Override
      public void run() {
        uporzadkujObszar();
      }
    }, DELAY_ORDER);
  }  //koniec Metody()

  private void dajNagrode() {
    /* ****************************** */
    /* nagroda dzwiekowa (ewentualna) */
    /* ****************************** */
    if (mGlob.BEZ_KOMENT) {
      return;
    }
    if (mGlob.TYLKO_OKLASKI) {
      odegrajZAssets(OKLASKI_SND, 400);
      return;
    }

    //Teraz mamy pewnosc, ze to Glos [+Oklaski], losujemy plik z mową:
    String komcie_path = "nagrania/komentarze/pozytywy/female";
    //facet, czy kobieta:
    Random rand = new Random();
    int n = rand.nextInt(4); // Gives n such that 0 <= n < 4
    if (n == 3) {
      komcie_path = "nagrania/komentarze/pozytywy/male"; //kobiecy glos 3 razy czesciej
    }
    //teraz konkretny (losowy) plik:
    String doZagrania = dajLosowyPlik(komcie_path);

    odegrajZAssets(komcie_path + "/" + doZagrania, 400);    //pochwala glosowa

    if (mGlob.TYLKO_GLOS) {
      return;
    }

    //teraz Oklaski (bo to jeszcze pozostalo):
    odegrajZAssets(OKLASKI_SND, 2900); //oklaski

  } //koniec Metody()

  private String dajLosowyPlik(String aktywa) {
    //Zwraca losowy plik z podanego katalogu w Assets; używana do generowania losowej
    // pochwały/nagany

    String[] listaKomciow = null;
    AssetManager mgr = getAssets();
    try {
      listaKomciow = mgr.list(aktywa);
    } catch (IOException e) {
      e.printStackTrace();
    }
    int licznosc = listaKomciow.length;
    int rnd = (int) (Math.random() * licznosc);
    int i = -1;
    String plik = null;
    for (String file : listaKomciow) {
      i++;
      if (i == rnd) {
        plik = file;
        break;
      }
    }
    return plik;
  } //koniec metody()

  private int dajLeftmostX() {
    //Daje wspolrzedną X najbardziej na lewo polozonej przez usera etykiety z Obszaru;
    // pomocnicza

    int min = Integer.MAX_VALUE;
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        if (lb.getLeft() < min) {
          min = lb.getLeft();
        }
      }
    }
    return min;
  }

  private MojTV dajLeftmostInArea() {
    //Daje wskaznik do najbardziej na lewo polozonej przez usera etykiety z Obszaru; pomocnicza

    int min = Integer.MAX_VALUE;
    MojTV leftMostLabel = null;
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        if (lb.getLeft() < min) {
          min = lb.getLeft();
          leftMostLabel = lb;
        }
      }
    }
    return leftMostLabel;
  }

  private void uporzadkujObszar() {
    /*
     * ******************************************************************************************* */
    /* Po Zwyciestwie:
     *  */
        /* Gasimy wszysko (litery w obszarze); wyswietlamy zwycieski wyraz, przywracamy klawisz
        bDalej ś*/
        /* Jesli trzeba - robimy korekcje miejsca wyswietlania (zeby wyraz sie miescil w
        Obszarze)     */
    /* Gasimy niektore klawisze pod Obszarem.
     *  */
    /*
     * ******************************************************************************************* */

    //Wyswietlenie wyrazu rozpoczynam od miejsca, gdzie user umiescil 1-sza litere (z
    // ewentualnymi poprawkami):
    LinearLayout.LayoutParams lPar;
    lPar = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
    int leftMost = dajLeftmostX();
    //Gdyby mialo wypasc troche przed poczatkiem lObszar'u:
    if (leftMost + lbs[0].getPaddingLeft() <= xLl) {
      leftMost = xLl;
    }

    lPar.leftMargin = leftMost;

    //ski ski ++
    // 2018.09.06 - kod poniżej zapewnia, ze tvShownWord zostanie pokazane "równo" z
    // ulozonymi literami, nie będzie "podskoku"
    //Trudno wyelimimnowac ten 'podskok' w xml, ale kod ponizej wydaje sie troche
    // 'nadmiarowy' i niepewny (lbs[0])
    //Sprawdzic na tablecie Marcina - tam widac wysokie 'skoki' ;)
    int h = lbs[0].getHeight(); //wys=sokosc litery (? czy aby na pewno -> wielka vs. mala)
    int lSrWz = (int) lObszar.getHeight() / 2;  //linia Srodkowa Wzgledna (w przestrzeni wspolrzednych lObszar)
    ////rownowazne getHeightt90 -> int lSrWz = ((int) ((yLd-yLg)/2.0));
    lPar.topMargin = lSrWz - (int) (h / 2.0) - 6;  //odejmowanie zeby srodek etykiety wypadl na lTrim; -6 bo 'y' jest ucinaneŁ
    // od dolu...
    //ski ski --

    tvShownWord.setLayoutParams(lPar);

    pokazWyraz();                     //w Obszarze pokazany zostaje ulozony wyraz
    // (umieszczaam w tvSHownWord; + ewentualna korekcja polozenia)

    //Gasimy wszystkie etykiety:
    for (MojTV lb : lbs) {
      lb.setVisibility(INVISIBLE);
    }

    //Przywrocenie/pokazanie klawisza bDalej i bAgain1 oraz niektorych dodatkowych (z lekkim opoznieniem):
    Handler mHandl = new Handler();
    mHandl.postDelayed(new Runnable() {
      @Override
      public void run() {
        bDalej.setVisibility(VISIBLE);
        bAgain1.setVisibility(VISIBLE);
        if (mGlob.BUPLOW_ALL) {
          bUpperLower.setEnabled(true);
        }
      }
    }, 2000); //zeby dziecko mialo czas na 'podziwianie' ;)

    //Animacja w 'nagrode':
    if (!mGlob.BEZ_OBRAZKOW) {
      if (mGlob.IMG_TURN_EF) {
        Handler mHandl1 = new Handler();
        mHandl1.postDelayed(new Runnable() {
          @Override
          public void run() {
            Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.obrot);
            imageView.startAnimation(a);
          }
        }, DELAY_ORDER + DELAY_ORDER / 2);
      }
    }

  } //koniec Metody()

  private void usunGrawitacje() {
    //Usuwa grawitacje z lObszar
    RelativeLayout.LayoutParams lPar = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();
    lObszar.setGravity(Gravity.NO_GRAVITY);
    lObszar.setLayoutParams(lPar);
  }



  private void pokazWyraz() {
    //Pokazanie ulozonego wyrazu w Obszarze;
    //Wyraz skladam z tego, co widac na ekranie, nie uzywając currWord (bo problemśy z
    // duze/male litery)

    tvShownWord.setText(coWidacInObszarLiterowo());

    //Kolor biore z etykiet, bo fabryczny jest troche za jasny... kosmetyka
    ColorStateList kolor = null;
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        kolor = lb.getTextColors();
        break;
      }
    }

    tvShownWord.setTextColor(kolor);

    tvShownWord.setVisibility(VISIBLE);

    //!!! BARDZO WAZNE: !!!
    korygujJesliWystaje();

  } //koniec Metody()

  private void addGravityToParent() {
    /*
     * **************************************************************************************** */
    /* Dodanie grawitacji sciagajacej do prawego boku do lObszar;     *  */
    /* Dzieki temu, mamy gwarancje, jezeli wyraz wystaje za lObszar, to zostanie "cofnięty"  */
    /* i pokazany w całości w lObszar.     */
    /* **************************************************************************************** */

    RelativeLayout.LayoutParams lPar = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();

    //"usuniecie" marginesu z TextView'a (bo mogl byc programowo ustawiony i w takim wypadku
    // grawitacja by nie zadzialala):
    LinearLayout.LayoutParams lTV = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
    lTV.leftMargin = 0;
    tvShownWord.setLayoutParams(lTV);

    //Teraz ustawienie grawitacji u parenta:
    lObszar.setGravity(Gravity.END);
    lObszar.setLayoutParams(lPar);
  } //koniec Metody()



  private boolean poprawnieUlozono() {
    /* **************************************** */
    /* zalozenie wejsciowe:                     */
    // Wszystkie sylaby znajduja sie w Obszarze */
    /* Sprawdzenie, czy poprawnie ulozone.      */
    /* **************************************** */

    String mCurrWord; //dodatkowa w stosunku do Literowanki, bo to Sylabowiec i currWord=np. "nie-za-po-mi-naj-ki" -> trzeba 'strippowac' te myślniki....

    String coUlozyl = coWidacInObszarLiterowo();

    //Uwaga - nie nalezy podnosic do upperCase obydwu stron "równania" i porownywac bez
    // warunku 'if' (jak ponizej) --> problemy (Mikolaj-Mikolaj):
    if (!inUp) {
      mCurrWord = currWord.replace("-","");
      return coUlozyl.equals(mCurrWord);
    } else {
      mCurrWord = currWord.toUpperCase(Locale.getDefault());
      mCurrWord = mCurrWord.replace("-","");
      return coUlozyl.equals(mCurrWord);
    }

  } //koniec Metody();

  private int ileWObszarze() {
    //Zlicza, ile elementow znajduje sie aktualnie w Obszarze
    int licznik = 0;
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        licznik++;
      }
    }
    return licznik;
  }


  private void ustawListyNaJezykPolski() {
    /**
     * Przywraca list na jezyk polski (bo byl ustawiona na obcy)
     */

    mGlob.POZIOM = WSZYSTKIE;  //zeby uniknac problemow...

    //Najpierw lista z Assets:
    katalogAssets = "obrazki_demo_ver";
    if (mGlob.PELNA_WERSJA) {
      katalogAssets = "obrazki_pelna_ver";
    }
    AssetManager mgr = getAssets();

    try {
      listaObrazkowAssets = mgr.list(katalogAssets);  //laduje obrazki z Assets
      listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM);
    } catch (IOException e) {
      e.printStackTrace();
    }

    //Nastepnie ewentualna lista z SD:
    if (mGlob.ZRODLEM_JEST_KATALOG) {
      tworzListeFromKatalog();
      listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
    }

  }  //koniec Metody()


  @Override
  protected void onResume() {
    /* *************************************   */
    /* Aplikowanie zmian wprowadzonych w menu  */
    /* Bądż pierwsze uruchomienie (po splashu) */
    /* *************************************   */
    super.onResume();

    //Kwestia pierwszego wejscia (PW):
    if (PW) {
      PW = false;
      return;
    }

    if (mGlob.PO_DIALOGU_MOD) {
      mGlob.PO_DIALOGU_MOD = false;
      odegrajWyraz(200);
    }

    //Pokazujemy cwiczenie z parametrami ustawionymi na Zmiennych Glob. (np. poprzez UstawieniaActivity):

    //Jezeli bez obrazkow - gasimy biezacy obrazek, z obrazkami - pokazujemy (gdyby byl niewidoczny):
    if (mGlob.BEZ_OBRAZKOW) {
      imageView.setVisibility(INVISIBLE);
      l_imageContainer.setBackgroundResource(R.drawable.border_skib_gray);
    } else {
      imageView.setVisibility(VISIBLE);
      l_imageContainer.setBackgroundColor(Color.TRANSPARENT);
      //l_imageContainer.setBackgroundResource(0); - alternatywa do Color.TRANSPARENT (podobno TRANSPARENT lepszy)
    }
    odblokujZablokujKlawiszeDodatkowe();    //pokaze/ukryje klawisze zgodnie z sytuacja na UstawieniaActivity = w obiekcie mGlob
    pokazUkryjNazwe();                      //j.w. - nazwa pod obrazkiem

    //Badamy najistotniejsze opcje; Gdyby zmieniono Katalog lub poziom, to naczytanie na nowo:
    newOptions.pobierzZeZmiennychGlobalnych();         //jaki byl wynik ostatniej 'wizyty' w UstawieniaActivity
    if (!newOptions.takaSamaJak(currOptions)) {        //musimy naczytac ponownie, bo zmieniono zrodlo obrazkow (chocby poprzez zmiane poziomu trudnosci) i/lub jezyk

      currOptions.pobierzZeZmiennychGlobalnych();    //zapamietanie na przyszlosc

      //Zmiany byly "normalne", nie zwiazane z jezykiem (bo sterowanie doszlo tutaj):
      if (!mGlob.ZRODLEM_JEST_KATALOG) {
        listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM); //nie trzeba tworzyc listy z Assets - jest stworzona na zawsze w onCreate()
      } else {
        tworzListeFromKatalog();
        listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
      }
      //Gdyby okazalo sie, ze nie ma obrazkow o wybranym poziomie trudnosci, bierzemy
      // wszystkie (list zrodlowych tworzyc w tym punkcie sterowania nie trzeba):
      if (listaOper.length == 0) {
        wypiszOstrzezenie("Brak ćwiczeń o wybranym poziomie trudności. Zostaną pokazane wszystkie " + "ćwiczenia.");
        mGlob.POZIOM = WSZYSTKIE;
        currOptions.pobierzZeZmiennychGlobalnych();      //bo sie zmienily linie wyzej...
        if (!mGlob.ZRODLEM_JEST_KATALOG) {
          listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM);
        } else {
          listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
        }
      }
      mPamietacz = new Pamietacz(listaOper); //nowa lista, wiec Pamietacz na nowo....
      bDalej.callOnClick();
    }

  } //koniec onResume()

  private void tworzListeFromKatalog() {
    //Tworzenie listy obrazków z Katalogu:

    katalogSD = new File(mGlob.WYBRANY_KATALOG);
    listaObrazkowSD = findObrazki(katalogSD);

  } //koniec Metody()

  private int dajLosowyNumerObrazka() {

    if (mGlob.ROZNICUJ_OBRAZKI) {
      return mPamietacz.dajSwiezyZasob();
    }
    //Nie korzystamy z Pamietacza:
    else {
      int rob;
      int rozmiar_tab = listaOper.length;
      //Generujemy losowy numer, ale tak, zeby nie wypadl ten sam:
      if (rozmiar_tab != 1) { //przy tylko jednym obrazku kod ponizej jest petla nieskonczona, więc if
        do {
          rob = (int) (Math.random() * rozmiar_tab);
        } while (rob == currImage);
      } else {
        rob = 0; //bo 0-to jest de facto numer obrazka
      }
      return rob;  //105-rzeka 33=lalendarz
    }

  } //koniec Metody()

  private void przypiszLabelsyAndListenery() {

    L00 = (MojTV) findViewById(R.id.L00);
    L01 = (MojTV) findViewById(R.id.L01);
    L02 = (MojTV) findViewById(R.id.L02);
    L03 = (MojTV) findViewById(R.id.L03);
    L04 = (MojTV) findViewById(R.id.L04);
    L05 = (MojTV) findViewById(R.id.L05);

    //ustawienie tablicy do operowania na ww. etykietach:
    lbs = new MojTV[] {L00, L01, L02, L03, L04, L05};

    //podpiecie listenerow pod labelsy:
    for (MojTV lb : lbs) {
      lb.setOnTouchListener(new ChoiceTouchListener()); //na prezesuwanie sylaby

//      jezeli OnCloick() w tym miejscu (ponizej), to 'gryzie' sie z onTouch i nie zadziala...; przenosze w srodek onTouch'a ...
//      lb.setOnClickListener(new OnClickListener() {     //na odegranie sylaby
//        @Override
//        public void onClick(final View view) {
//          Toast.makeText(getApplicationContext(), "Kliknieto sylabe...2", Toast.LENGTH_SHORT).show();
//          odegrajZAssets("nagrania/wyrazy/chleb.ogg",0);
//        }
//      });
    }

    //Listenery na obszarze na Obrazek:
    l_imageContainer.setOnLongClickListener(this);
    l_imageContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        odegrajWyraz(0);
      }
    });

    //listener na long click na bShiftLeft:
    bShiftLeft.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Kompresuj();
        return true; //wazne - znaczy ze 'wybrzmial' i nie wykona sie krotki OnClick
      }
    });

  } //koniec Metody()

  private void Kompresuj() {
    /****************************************************************/
    /* Kompresuje (=scieesnia) CALOSC liter                         */
    /* Wyglada 'dziwnie' ale iteracyjnie nie dawalo sie zrobic...   */
    /* Zrobilem intuicyjno-dopswiadczalnie...                       */
    /* mtv to 'byt' formalny, bo na czyms trzeba bylo oprzec post'a */
    /****************************************************************/

    if (ileWObszarze() == 0) {
      return;
    }

    likwidujBiggestGap();
    MojTV mtv = dajLeftmostInArea();
    mtv.post(new Runnable() {
      @Override
      public void run() {
        likwidujBiggestGap();
        MojTV mtv = dajLeftmostInArea();
        mtv.post(new Runnable() {
          @Override
          public void run() {
            likwidujBiggestGap();
            MojTV mtv = dajLeftmostInArea();
            mtv.post(new Runnable() {
              @Override
              public void run() {
                likwidujBiggestGap();
                MojTV mtv = dajLeftmostInArea();
                mtv.post(new Runnable() {
                  @Override
                  public void run() {
                    likwidujBiggestGap();
                    MojTV mtv = dajLeftmostInArea();
                    mtv.post(new Runnable() {
                      @Override
                      public void run() {
                        likwidujBiggestGap();
                        MojTV mtv = dajLeftmostInArea();
                        mtv.post(new Runnable() {
                          @Override
                          public void run() {
                            likwidujBiggestGap();
                          }
                        });
                      }
                    });
                  }
                });
              }
            });
          }
        });
      }
    });
  } //koniec Metody()

  private void dostosujDoUrzadzen() {
    RelativeLayout.LayoutParams lPar;

    //Pobieram wymiary ekranu na potrzeby dostosowania wielkosci Obrazka i Prostokata/Obszaru
    // 'gorącego' do ekranu urządzenia:
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    //przekazanie na zewnatrz:
    sizeW = displaymetrics.widthPixels;
    sizeH = displaymetrics.heightPixels;

    //sledzenie - pokazania wymiarow urządzenia i rozdzielczosci dpi
    //tvInfo3.setText(Integer.toString(sizeW) + "x" + Integer.toString(sizeH) + " dpi=" + Integer.toString(displaymetrics.densityDpi));
    //Toast.makeText(mGlob, Integer.toString(sizeW) + "x" + Integer.toString(sizeH) + " dpi="+Integer.toString(displaymetrics.densityDpi), Toast.LENGTH_LONG).show();

    //Obrazek - ustawiam w lewym górnym rogu:
    lPar = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
    lPar.width = sizeW / 3;
    lPar.height = sizeH / 2;
    lPar.topMargin = 5;
    lPar.leftMargin = 10;
    imageView.setLayoutParams(lPar);

    //nazwa pod obrazkiem - szerokosc jak Obrazek:
    tvNazwa.getLayoutParams().width = lPar.width;

    //Obszar-Prostokat na ukladanie wyrazu:
    RelativeLayout.LayoutParams lPar1 = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();
    lPar1.topMargin = (int) (sizeH / 1.6);
    lPar1.height = sizeH / 4;
    lObszar.setLayoutParams(lPar1);

    //gestosc ektranu:
    density = getResources().getDisplayMetrics().densityDpi;

  } //koniec Metody()

  private boolean pokazModal() {

    if (!mGlob.POKAZ_MODAL) {
      return true;
    }

    //Pokazanie modalnego okienka.
    //Okienko realizowane jest jako Activity  o nazwie DialogModalny
    intModalDialog = new Intent(getApplicationContext(), DialogModalny.class);
    startActivity(intModalDialog);
    return true;
  }


  private void ustawLadnieEtykiety() {
    /*
     * *************************************************************************************************** */
    /* Ustawiam Literki/Etykiety L0n wzgledem obrazka i wzgledem siebie - na lewo od obrazka               */
    /* Kazdy rząd (3 rzedy) ustawiam niejako osobno, poczynajac od 1-go elementu w rzedzie jako od wzorca. */
     /* *************************************************************************************************** */

    //wstawka dla duzych ekranow - powiekszam litery:
    //        if (sizeW>1100) {
    //            int litera_size = (int) getResources().getDimension(R.dimen.litera_size);
    //            float wsp = 1.07f;
    //            if (sizeW>1900) wsp = 1.2f;
    //            for (MojTV lb : lbs) lb.setTextSize(TypedValue.COMPLEX_UNIT_PX, wsp*litera_size);
    //            tvShownWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, wsp*litera_size); //wyraz
    // wyswietlany nie powinien roznic sie od liter
    //        }

    final int odstepWpionie = yLg / 4; //od gory ekranu do Obszaru sa 3 wiersze etykiet, wiec 4 przerwy

    int od_obrazka = (int) getResources().getDimension(R.dimen.od_obrazka); //odstep 1-szej litery 1-go rzedu od obrazka

    RelativeLayout.LayoutParams lPar;
    //L00 (1-szy rząd):
    lPar = (RelativeLayout.LayoutParams) L00.getLayoutParams();

    //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
    int marginesTop = 1 * odstepWpionie - L00.getHeight() / 2;  //*1 - bo 1-szy wiersz

    final int poprPion = 25;
    lPar.topMargin = marginesTop - poprPion;
    int poprPoziom = (rootLayout.getRight() - imageView.getRight()) / MAXS;
    //troche w prawo, jesli dobre urzadzenie:

    if (sizeW > 1100) {
      poprPoziom = (int) (1.24 * poprPoziom);
    }

    lPar.leftMargin = imageView.getRight() - L00.getPaddingLeft() + poprPoziom;
    //Jesli dlugie sylaby, to 1-szy rząd troche na lewo, zeby zmniejszyc szansę, że coś 'wylezie' z prawej strony:
    //(inne rzędy - "raczej" ok...
    if (sylaby.dlugoscNajdluzszej()>=5) {
      lPar.leftMargin -= poprPoziom/2;
    }

    L00.setLayoutParams(lPar);

    final int poprawka = (int) getResources().getDimension(R.dimen.poprawka);
    //Toast.makeText(this,"poprawka: "+pxToDp(poprawka),Toast.LENGTH_SHORT).show();
    int lsize = (int) getResources().getDimension(R.dimen.litera_size);
    //Toast.makeText(this,"litera_size: "+pxToSp(lsize),Toast.LENGTH_SHORT).show();


    //L01*:
    L00.post(new Runnable() {
      @Override
      public void run() { //czekanie aż policzy/usadowi się L01
        RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L01.getLayoutParams();
        lParX.leftMargin = ((RelativeLayout.LayoutParams) L00.getLayoutParams()).leftMargin + (int) (1.8*poprawka);
        int marginesTop = 1 * odstepWpionie - L00.getHeight() / 2 - poprPion;
        lParX.topMargin = marginesTop;
        L01.setLayoutParams(lParX); //n
      }
    });


    //L02*: (2-gi rząd):
    lPar = (RelativeLayout.LayoutParams) L02.getLayoutParams();
    lPar.leftMargin = ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).leftMargin + imageView.getLayoutParams().width + od_obrazka / 4;
    marginesTop = 2 * odstepWpionie - L00.getHeight() / 2; //2- bo 2-gi wiersz
    lPar.topMargin = marginesTop;
    lPar.leftMargin = imageView.getRight() + poprPoziom / 4;
    L02.setLayoutParams(lPar);


    //L03*:
    L02.post(new Runnable() {
      @Override
      public void run() {
        RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L03.getLayoutParams();
        lParX.leftMargin = ((RelativeLayout.LayoutParams) L02.getLayoutParams()).leftMargin + 2*poprawka;
        int marginesTop = 2 * odstepWpionie - L02.getHeight() / 2;
        lParX.topMargin = marginesTop;
        L03.setLayoutParams(lParX); //n
      }
    });


    //L04*: (3-ci rząd):
    lPar = (RelativeLayout.LayoutParams) L04.getLayoutParams();
    lPar.leftMargin = ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).leftMargin + imageView.getLayoutParams().width + od_obrazka / 2;
    marginesTop = 3 * odstepWpionie - L00.getHeight() / 2; //3- bo 3-szy wiersz
    lPar.topMargin = marginesTop + poprPion;
    lPar.leftMargin = imageView.getRight() + poprPoziom/2; //bylo: - L00.getPaddingLeft() + poprPoziom;
    L04.setLayoutParams(lPar);

    //L05*:
    L04.post(new Runnable() {
      @Override
      public void run() {
        RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L05.getLayoutParams();
        lParX.leftMargin = ((RelativeLayout.LayoutParams) L04.getLayoutParams()).leftMargin + 2*poprawka;
        int marginesTop = 3 * odstepWpionie - L04.getHeight() / 2 + poprPion;
        lParX.topMargin = marginesTop;
        L05.setLayoutParams(lParX); //n
      }
    });


    //Dodatkowe przemieszanie wyzej-nizej po kazdej etykiecie:
    for (final MojTV lb : lbs) {
      lb.post(new Runnable() {
        @Override
        public void run() {
          RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) lb.getLayoutParams();
          Random rand = new Random();
          int k = rand.nextInt(3);
          if (k == 0) {
            k = 0;
          }
          if (k == 1) {
            k = +15;
          }
          if (k == 2) {
            k = -15;
          }

          //Zmieniamy w 2-gim wierszu :
          if (lb == lbs[2] || lb == lbs[3]) {
            lParX.topMargin += k;
          }
          //Zmieniamy w 1-szym wierszu; w 1-szym wierszu pozwalam tylko w dol :
          if (lb == lbs[0] || lb == lbs[1]) {
            //w 1-szym wierszu zmieniamy tylko przy duzych gestosciach, inaczej
            // główki liter wystają poza górną krawędź ekranu:
            if (density > DisplayMetrics.DENSITY_MEDIUM) {
              k = -Math.abs(k);
              if (density > DisplayMetrics.DENSITY_HIGH) {
                k = 2 * k;
              }
              lParX.topMargin += k;
            }
          }
          //Zmieniamy w 3-cim wierszu :
          if (lb == lbs[4] || lb == lbs[5]) { //w 3-cim wierszu pozwalam tylko w gore
            k = Math.abs(k);
            lParX.topMargin -= k;
          }

          lb.setLayoutParams(lParX);
        }
      });
    }  //for

    //cofniecie w lewo skrajnych etykiet, bo na niektorych urzadz. wyłażą...:
    //cofnijWlewo();

  }  //koniec Metody()


  private void cofnijWlewo() {
    /* Cofa w lewo skrajne etykiety 1-go i 3-go rzedu, bo na niektórych urządzeniach wystają za bandę */
    /* 2019-04-01- Sylabowiec - nie wykorzystuję... */

    boolean saDlugie = (lbs[1].length()>=5||lbs[3].length()>=5||lbs[5].length()>=5);
    if (!saDlugie) {
      return;
    }

    lbs[5].post(new Runnable() { //lbs[11] bo czekam, az wszystko zostanie ulozone (doswiadczlnie)
      @Override
      public void run() {
        for (int i=0; i<lbs.length; i++) {
          //tylko 3-cia kolumna, 1-szy i 3-ci rzad:
          if (i==1 || i==3 || i==5) {
            //if (lbs[i].length()>=5) {
              //int lewy = xLp - (int) (1.55 * lbs[0].getWidth());  //lbs[0] bo potrzebne cos 'statycznego' - doswiadczalnie
              //RelativeLayout.LayoutParams lParY = (RelativeLayout.LayoutParams) lb.getLayoutParams();
              //lParY.leftMargin = lewy;
              //lb.setLayoutParams(lParY);

              RelativeLayout.LayoutParams lParY = (RelativeLayout.LayoutParams) lbs[i].getLayoutParams();
              lParY.leftMargin -= 100;
              lbs[i].setLayoutParams(lParY);
            //}
          }
        }
      }
    });

  } //koniec Metody()

  private void ustawWymiaryKlawiszy() {
    //Wymiarowuje klawisze bDalej, bPomin, bAgain, bHint, bUpperLOwer
    //bDalej zajmuje przestrzen od gory do gornej krawedzi Obszaru, ale zostawia 2/3 swojej
    // wysokosci miejsce na bAgain1:

    bDalej.getLayoutParams().height = (int) (0.66 * yLg);
    bDalej.requestLayout();

    bAgain1.getLayoutParams().height = (int) (0.32 * yLg);
    bAgain1.getLayoutParams().width = bDalej.getWidth();
    int lsize = (int) getResources().getDimension(R.dimen.litera_size); //30% rozmiaru liter-etykiet
    bAgain1.setTextSize(pxToSp(lsize / 3));
    bAgain1.requestLayout();

    //cala przestrzen od dolnej krawedzi Obszaru do konca ekranu:
    bPomin.getLayoutParams().height = sizeH - yLd;
    bPomin.requestLayout();

    bAgain.getLayoutParams().height = sizeH - yLd;
    bAgain.requestLayout();

    bShiftLeft.getLayoutParams().height = sizeH - yLd;
    bShiftLeft.getLayoutParams().width = 2 * bAgain.getWidth();
    bShiftLeft.requestLayout();

    bUpperLower.getLayoutParams().height = sizeH - yLd;
    bUpperLower.getLayoutParams().width = 2 * bAgain.getWidth();
    bUpperLower.requestLayout();

    bHint.getLayoutParams().height = sizeH - yLd;
    bHint.getLayoutParams().width = (int) (1.5 * bAgain.getWidth());
    bHint.requestLayout();


  } //koniec metody()


  private void odblokujZablokujKlawiszeDodatkowe() {
    //Pokazanie (ewentualne) klawiszy pod Obszarem"

    if (mGlob.BPOMIN_ALL) {
      bPomin.setVisibility(VISIBLE);
    } else {
      bPomin.setVisibility(INVISIBLE);
    }

    if (mGlob.BUPLOW_ALL) {
      bUpperLower.setVisibility(VISIBLE);
    } else {
      bUpperLower.setVisibility(INVISIBLE);
    }

    if (mGlob.BAGAIN_ALL) {
      bAgain.setVisibility(VISIBLE);
      bAgain.setText(R.string.bAgain_text);  //odtwarzam, bo Animacja mogla zaburzyc...
    } else {
      bAgain.setVisibility(INVISIBLE);
    }

    if (mGlob.BHINT_ALL) {
      bHint.setVisibility(VISIBLE);
    } else {
      bHint.setVisibility(INVISIBLE);
    }


    /*if (mGlob.BPOMIN_ALL)*/
    bPomin.setEnabled(true);
    /*if (mGlob.BUPLOW_ALL)*/
    bUpperLower.setEnabled(true);
    /*if (mGlob.BAGAIN_ALL)*/
    bAgain.setEnabled(true);
    /*ifbShiftLeft. (mGlob.BHINT_ALL) */
    bHint.setEnabled(true);
    //setEnabled(true);
  }


  public void bHintOnClick(View view) {
    /**
    Podpowiada kolejna sylabe do ulozenia
    Idea algorytmu - iteruje po obiekcie 'sylaby i wskazuje 1-sza sylabe nie na swoim miejscu w Obszarze
    Wziete z Sylabowanki (Lazarus):
    Idea algorytmu : przegladam wyraz sylaba po sylabie (od lewej) i jezeli przegladana
    sylaba nie jest na swoim miejscu w ramce, to wyrozniam ją (inaczej: pokazuję pierwszą sylabę,
    która nie jest na swoim miejscu).
    Inne podejscia prowadzily do b. skomplikowanego algorytmu.
    */

    final char[] wyraz = currWord.toCharArray();       //bo latwiej operowac na Char'ach

    int lsylab = sylaby.getlSylab();
    for (int i = 0; i < lsylab; i++) {
      String sylaba = sylaby.getSylabaAt(i);
      if (!jestGdzieTrzeba(sylaba, i)) {
        //podswietlLabel(wyraz[i]);
        podswietlLabel(sylaba);
        break;
      }
    }
    return;
  }  //koniec Metody()


  private boolean jestGdzieTrzeba(String sylaba, int pozycja) {
    /* Bada, czy przekazana 'sylaba' znajduje sie na pozycji 'pozycja' w Obszarze */

    String[] textInArea = coWidacInObszarSylabowo();

    if (textInArea == null) //w Obszarze nic jeszcze nie ma
    {
      return false;
    }

    if (!(pozycja>(ileWObszarze()-1))) {                            //(textInArea.length() - 1))   //text w Obszarze jest krotszy niz pozycja litery
        //Sylaba w tekscie w Obszarze i Sylaba w parametrach jako Stringi (do porownań):
        String sylabaWtext = textInArea[pozycja];                   //String litWtext = Character.toString(tChar[pozycja]);
        String sylabaWpar = sylaba;                                 //String litWpar  = Character.toString(sylaba);
        //Bedziemy porownywac przez upperCasy - bezpieczniej:
        sylabaWtext = sylabaWtext.toUpperCase(Locale.getDefault()); //litWtext = litWtext.toUpperCase(Locale.getDefault());
        sylabaWpar = sylabaWpar.toUpperCase(Locale.getDefault());         //litWpar = litWpar.toUpperCase(Locale.getDefault());
        return (sylabaWpar.equals(sylabaWtext));
    }
    else
        return false;

  } //koniec Metody()


  private MojTV[] posortowanaTablicaFromObszar() {
    /*
     * ******************************************************************************************** */
    /* Znajdujace sie w Obszarze elementy zwraca w tablicy, POSORTOWANEJ wg. lefej fizycznej wsp. X */
    /*
     * ******************************************************************************************** */

    MojTV[] tRob = new MojTV[MAXS];                //tablica robocza, do dzialań
    //Wszystkie z Obszaru odzwierciedlam w tRob:
    int licznik = 0;                               //po wyjsciu z petli bedzie zawieral liczbe
    // liter w Obszarze
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        tRob[licznik] = lb;
        licznik++;
      }
    }

    if (licznik == 0) {
      return null;
    }

    //Sortowanie (babelkowe) tRob względem lewej wspolrzednej:
    //nieoptymalne?: 2018-10-23
    //MojTV elRob = new MojTV(this);    //element roboczy

    MojTV elRob;     //element roboczy
    boolean bylSort = true;
    while (bylSort) {
      bylSort = false;
      for (int j = 0; j < licznik - 1; j++) {
        if (tRob[j].getX() > tRob[j + 1].getX()) {
          elRob = tRob[j + 1];
          tRob[j + 1] = tRob[j];
          tRob[j] = elRob;
          bylSort = true;
        }
      }
    }  //while

    return tRob;

  } //koniec Metody()


  private String coWidacInObszarLiterowo() {
    /* ********************************************************** */
    /* Zwraca w postaci Stringa to, co AKTUALNIE widac w Obszarze */
    /* (widziane jako ciąg LITER(!)                               */
    /* ********************************************************** */

    //nieoptymalne?: 2018-10-23
    //MojTV[] tRob = new MojTV[MAXS];                //tablica robocza, do dzialań
    //tRob = posortowanaTablicaFromObszar();

    MojTV[] tRob = posortowanaTablicaFromObszar();    //tablica robocza, do dzialań

    //Wypakowanie do Stringa i zwrot na zewnatrz:
    StringBuilder sb = new StringBuilder();

    //Jaka jest licznosc tablicy tRob[] ?:
    int licznik = ileWObszarze();

    for (int i = 0; i < licznik; i++) {
      //sb.append(tRob[i].getOrigSyl());
      sb.append(tRob[i].getText());
    }

    //nieoptymalne?: 2018-10-23
    //String coWidac = sb.toString();
    //return coWidac;

    return sb.toString();

  } //koniec Metody()

  private String[] coWidacInObszarSylabowo() {
    /* ******************************************************************* */
    /* Zwraca w postaci tablicy Stringów to, co AKTUALNIE widac w Obszarze */
    /* (widziane jako ciąg SYLAB(!)                                        */
    /* ******************************************************************* */

    MojTV[] tRob = posortowanaTablicaFromObszar();
    String[] tStr = new String[MAXS];
    //Jaka jest licznosc tablicy tRob[] ?:
    int licznik = ileWObszarze();
    for (int i = 0; i < licznik; i++) {
      tStr[i] = tRob[i].getText().toString();
    }
    return tStr;
  }


  private void podswietlLabel(String c) {
  /* Podswietla pierwsza napotkana etykiete zawierajaca sylabe z parametru */

    String coDostalem = c; //w Literowance->String.valueOf(c);

    for (MojTV lb : lbs) {
      if (!lb.equals("*")) {
        String etyk = lb.getOrigSyl();
        if (etyk.equals(coDostalem) && !lb.isInArea()) {  //tylko mrugamy poza Obszarem - inaczej niejednznacznosci....
          lb.makeMeBlink(400, 5, 4, RED);
          return;
        }
      }
    }
    //Nie mrugnal sylabą spoza Obszaru, zatem walę po calym Obszarze, bo ulozono 'kaszanę' i
    // trzeba jakos dac znac:
    //koniecznie odblokowuję bAgain (jesli zablokowany)....:
    bAgain.setEnabled(true);
    bAgain.setVisibility(VISIBLE);
    bAgain.setText(R.string.bAgain_text);
    makeMeBlink(bAgain, 400, 5, 10, Color.BLUE);  //... i sugeruję, zeby to nacisnal
    for (MojTV lb : lbs) {
      if (lb.isInArea()) {
        lb.makeMeBlink(400, 5, 4, Color.BLUE);
      }
    }

  }  //koniec Metody()

  @Override
  protected void onDestroy() {
    /* Zapisanie (niektorych!) ustawienia w SharedPreferences na przyszła sesję */
    super.onDestroy();
    ZapiszDoSharedPreferences();
  } //onDestroy

  private void ZapiszDoSharedPreferences() {
    /* Zapisanie (niektorych!) ustawienia w SharedPreferences na przyszła sesję */

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor edit = sharedPreferences.edit();

    edit.putInt("JAK_WYSW_NAZWE", mGlob.JAK_WYSW_NAZWE);

    edit.putInt("POZIOM", mGlob.POZIOM);

    edit.putBoolean("BEZ_DZWIEKU", mGlob.BEZ_DZWIEKU);

    edit.putBoolean("GLOS_OKLASKI", mGlob.GLOS_OKLASKI);
    edit.putBoolean("BEZ_KOMENT", mGlob.BEZ_KOMENT);
    edit.putBoolean("TYLKO_OKLASKI", mGlob.TYLKO_OKLASKI);
    edit.putBoolean("TYLKO_GLOS", mGlob.TYLKO_GLOS);
    edit.putBoolean("DEZAP", mGlob.DEZAP);

    edit.putBoolean("BHINT_ALL", mGlob.BHINT_ALL);
    edit.putBoolean("BPOMIN_ALL", mGlob.BPOMIN_ALL);
    edit.putBoolean("BUPLOW_ALL", mGlob.BUPLOW_ALL);
    edit.putBoolean("BAGAIN_ALL", mGlob.BAGAIN_ALL);

    edit.putBoolean("IMG_TURN_EF", mGlob.IMG_TURN_EF);
    edit.putBoolean("WORD_SHAKE_EF", mGlob.WORD_SHAKE_EF);
    edit.putBoolean("LETTER_HOPP_EF", mGlob.LETTER_HOPP_EF);
    edit.putBoolean("SND_ERROR_EF", mGlob.SND_ERROR_EF);
    edit.putBoolean("SND_OK_EF", mGlob.SND_LETTER_OK_EF);
    edit.putBoolean("SND_VICTORY_EF", mGlob.SND_VICTORY_EF);

    edit.putBoolean("ODMOWA_DOST", mGlob.ODMOWA_DOST);

    edit.putBoolean("ROZNICUJ_OBRAZKI", mGlob.ROZNICUJ_OBRAZKI);

    edit.putBoolean("ZRODLEM_JEST_KATALOG", mGlob.ZRODLEM_JEST_KATALOG);
    edit.putString("WYBRANY_KATALOG", mGlob.WYBRANY_KATALOG);

    edit.apply();
  }

  private Bitmap obrocJesliTrzeba(Bitmap bitmap, String sciezkaDoPliku) {
    //Wykrywa(?) orientacje obrazka i ewentualnie obraca obrazek pobrany z dysku tak aby byl
    // pokazany prawidłowo

    ExifInterface ei = null;
    try {
      ei = new ExifInterface(sciezkaDoPliku);
    } catch (IOException e) {
      e.printStackTrace();
    }

    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

    Bitmap rotatedBitmap = null;

    switch (orientation) {

      case ExifInterface.ORIENTATION_ROTATE_90:
        rotatedBitmap = rotateImage(bitmap, 90);
        break;

      case ExifInterface.ORIENTATION_ROTATE_180:
        rotatedBitmap = rotateImage(bitmap, 180);
        break;

      case ExifInterface.ORIENTATION_ROTATE_270:
        rotatedBitmap = rotateImage(bitmap, 270);
        break;

      case ExifInterface.ORIENTATION_NORMAL:
      default:
        rotatedBitmap = bitmap;
    }

    return rotatedBitmap;

  } //koniec Metody();

  private Bitmap rotateImage(Bitmap source, float angle) {
    //Wyrzucic Toasta
    //Toast.makeText(this, "Będzie Obrót "+angle, Toast.LENGTH_LONG).show();

    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
  } //koniec Metody()

  private void wypiszOstrzezenie(String tekscik) {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(this, R.style.MyDialogTheme);
    builder1.setMessage(tekscik);
    builder1.setCancelable(true);
    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dialog.cancel();
      }
    });
    AlertDialog alert11 = builder1.create();
    alert11.show();
  } //koniec Metody()

  public int dpToPx(int dp) {
    //Convert dp to pixel:
    DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }

  public int pxToDp(int px) {
    //Convert pixel to dp:
    DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
    return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }

  public int pxToSp(int px) {
    //Convert pixel to sp:
    float scaledDensity = this.getResources().getDisplayMetrics().scaledDensity;
    return Math.round(px / scaledDensity);
  }




  private final class ChoiceTouchListener implements OnTouchListener {

    private boolean bylMoove = false; //Semafor blokujacy odgrywanie sylaby podczas jej przesuwania; Sylaba bedzie odgrywac się tylko po 'funkcjonalnym' OnClick

    private void przywrocKolor(final View view, int czas) {
      /* ****************************************************************** */
      /* przywroceni koloru przeciaganej bądż KLIKNIETEJ sylaby - kosmetyka */
      /* ****************************************************************** */
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          ((MojTV) view).setTextColor(Color.BLACK);
        }
      },czas);
    }


    public boolean onTouch(View view, MotionEvent event) {

      final int X = (int) event.getRawX();
      final int Y = (int) event.getRawY();

      switch (event.getAction() & MotionEvent.ACTION_MASK) {

        case MotionEvent.ACTION_MOVE:

          bylMoove = true;

          layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
          layoutParams.leftMargin = X - _xDelta;
          layoutParams.topMargin = Y - _yDelta;
          layoutParams.rightMargin = -250;
          layoutParams.bottomMargin = -250;
          view.setLayoutParams(layoutParams);
          break;

        case MotionEvent.ACTION_DOWN:

          bylMoove = false;

          lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
          _xDelta = X - lParams.leftMargin;
          _yDelta = Y - lParams.topMargin;

          //sledzenie:
          //Pokazanie szerokosci kontrolki:
          //tvInfo.setText(Integer.toString(view.getWidth()));

          ((MojTV) view).setTextColor(RED); //zmiana koloru przeciaganej litery - kosmetyka

          //action_down wykonuje sie (chyba) ZAWSZE, wiec zakladam:
          ((MojTV) view).setInArea(false);
          //ileWObszarze(); -> sledzenie
          //a potem sie to ww. zmodyfikuje w action up....

          //Toast.makeText(MainActivity.this, ((MojTV) view).getOrigSyl(), Toast.LENGTH_SHORT).show();
          break;

        case MotionEvent.ACTION_UP:

          /**** Odgrywanie sylaby (ewentualne) **************************/
          boolean byloGranieSylaby = false; //zeby dluzej utrzymac czerwony kolor; zeby nie dac sie zagluszyc przez ding/brr
          boolean grac = true;
          if (grac) {
            if (!bylMoove) { //zeby Sylaba nie odegrala sie po zakonczenu ciagniecia (tak jest najsensowniej...)
              String syl = ((MojTV) view).getOrigSyl();
              syl = syl.toLowerCase(Locale.getDefault());
              grajSylabe(syl);
              byloGranieSylaby = true;
            }
          }
          //przywroceni koloru przeciaganej sylaby - kosmetyka
          if (byloGranieSylaby)
            przywrocKolor(view,1000);  //zeby kliknieta(=odgrywana) sylaba 'świeciła' dłużej
          else
            przywrocKolor(view,0);
          /*******************************/

          //sledzenie:
          //int Xstop = X;
          //tvInfo.setText("xKontrolki=" + Integer.toString(layoutParams.leftMargin));
          //tvInfo1.setText("xPalca=" + Integer.toString(Xstop));

          /* Sprawdzenie, czy srodek etykiety jest w Obszarze; Jezeli tak - dosuniecie do lTrim. : */
          //1.Policzenie wspolrzednych srodka Sylaby: (zakladam, ze srodek sylaby jest w srodku kontrolki o szer w i wys. h)
          layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
          int w = view.getWidth();
          int lm = layoutParams.leftMargin;
          int h = view.getHeight();
          int tm = layoutParams.topMargin;

          //srodek litery:
          int xLit = lm + (int) (w / 2.0);
          int yLit = tm + (int) (h / 2.0);

          //2.Dosunirecie Litery na poziomy srodek Obszaru (linia yLtrim); srodek
          // etykiety ma wypasc na yLtrim:
          if ((yLit > yLg && yLit < yLd) && (xLit > xLl && xLit < xLp)) {
            layoutParams.topMargin = yLtrim - (int) (h / 2.0);  //odejmowanie zeby srodek etykiety wypadl na lTrim
            view.setLayoutParams(layoutParams);

            //Bylo 'trimowanie' a wiec na pewno jestesmy w Obszarze- dajemy znac i
            // badanie ewentualnego ZWYCIESTWA :
            ((MojTV) view).setInArea(true);

            if (ileWObszarze() == sylaby.getlSylab()) {  //wszystkie sylaby wyrazu znalazly sie w Obszarze
              if (poprawnieUlozono()) {
                if (mGlob.LETTER_HOPP_EF)  //ostatnio polozona litera podskoczy z 'radosci' - efekciarstwo:
                  view.startAnimation(animShakeLong);
                Zwyciestwo();
              } else {
                if (!byloGranieSylaby)   //ten if zapobiega 'wygluszeniu' odegrania sylaby przez inny efekt dzwiekowy
                  reakcjaNaBledneUlozenie();
              }
            }
            //Wyraz jeszcze nie dokonczony, badamy, czy poprawna kolejnosc sylab:
            else {
              String whatSeen = coWidacInObszarLiterowo();
              String mCurrWord = currWord;
              mCurrWord = mCurrWord.replace("-","");
              if (inUp) {
                mCurrWord = mCurrWord.toUpperCase(Locale.getDefault());
              }

              if (!mCurrWord.contains(whatSeen)) {
                if (!byloGranieSylaby)   //ten if zapobiega 'wygluszeniu' odegrania sylaby przez inny efekt dzwiekowy
                  reakcjaNaBledneUlozenie();
              } else {//polozona (poprawnie) litera 'bujnie' się; odegrany zostanie
                // 'plusk' :
                if (mGlob.SND_LETTER_OK_EF) {
                  if (!byloGranieSylaby)  //ten if zapobiega 'wygluszeniu' odegrania sylaby przez inny efekt dzwiekowy (tak samo j.w.)
                    odegrajZAssets(PLUSK_SND, 0);
                }
                if (mGlob.LETTER_HOPP_EF) {
                  view.startAnimation(animShakeLong);
                }
              }
            }

          }
          //3.Jesli srodek litery zostala wyciagnieta za bande - dosuwam z powrotem:
          if (xLit <= xLl) {   //dosuniecie w prawo
            //Toast.makeText(MainActivity.this, "Wyszedl za bande...", Toast.LENGTH_SHORT).show();
            layoutParams.leftMargin = xLl - view.getPaddingLeft() + 2; //dosuniecie w prawo
            rootLayout.invalidate();
            //Ponowne wywolanie eventa - spowoduje, ze wykona sie onTouch na tym
            // samym view z zastanym (=ACTION_UP) eventem/parametrem, ale na innym polozeniu litery,
            //litera bedzie w Obszarze i zostanie 'dotrimowana'"
            view.dispatchTouchEvent(event); // Dispatch touch event to view
          }

          //Dosuniecie w lewo; ale dotyczy TYLKO etykiety ze "swiatła" Obszaru
          // ("swiatla", czyli rowniez za Prawym końcem Obszaru):
          if ((xLit >= xLp) && (yLit > yLg && yLit < yLd)) { //polozyl litere ZA prawa krawedzia (patrz Marcin ;) )

            //Bedziemy przesuwac w lewo lit. z Obszaru; dzieki temu mniej problemów, np. znika problem IE-->EI:
            ((MojTV) view).setInArea(true);  //zeby ostatnia litera tez 'zalapala' sie na przesuniecie funkcją przesunWLewo()

            //Jak sie nie uda przesunac w lewo (bo za blisko brzegu), to bedziemy probowac sciesnic:
            if (dajLeftmostX() > w) //if -> zeby nie przesunął za lewa bandę
            {
              przesunWLewo(w);
            } else {
              likwidujBiggestGap();
            }

            view.dispatchTouchEvent(event);

          }

          //Dosuniecie w LEWO, ale dotyczy etykiet SPOZA "światła" Obszaru:
          if ((xLit >= xLp) && !(yLit > yLg && yLit < yLd)) {
            layoutParams.leftMargin = xLp - view.getWidth();
            rootLayout.invalidate();
            view.dispatchTouchEvent(event); // Dispatch touch event to view
          }

          //3.Jezeli srodek litery za górnym lub dolnym brzegiem ekranu - dosuwam z powrotem:
          if (yLit < 0) {
            //layoutParams.topMargin += Math.abs(layoutParams.topMargin);
            layoutParams.topMargin = 0;
          }
          if (yLit > sizeH) {
            layoutParams.topMargin = sizeH - (int) (0.7 * h);
          }
          //sledzenie:
          //tvInfo2.setText("xLit="+Integer.toString(xLit)+" yLit="+Integer.toString(yLit));
          break;
      }
      rootLayout.invalidate();
      return true;
    } //koniec Metody()

    private void reakcjaNaBledneUlozenie() {
      /* ************************************************************** */
      /* Zalozenie wejsciowe: Ulozony ciag iter jest bledny:            */
      /* Ewentualne 'brrr...' + animacja 'shaking_short' na calym ciagu */
      /* ************************************************************** */

      //Potrzasniecie blednie ulozonymi literami:
      if (mGlob.WORD_SHAKE_EF) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            for (MojTV lb : lbs) {
              if (lb.isInArea()) {
                lb.startAnimation(animShakeShort);
              }
            }
          }
        }, 150);
      }

      if (mGlob.SND_ERROR_EF) {
        odegrajZAssets(BRR_SND, 20); //dzwiek 'brrrrr'
      }

      if (ileWObszarze() == sylaby.getlSylab()) { //jezeli wszystkie litery polozone, ale źle (patrz zalozenie wejsciowe), to glos dezaprobaty:
        if (mGlob.DEZAP) {
          odegrajZAssets(DEZAP_SND, 320);  //"y-y" męski glos dezaprobaty
        }
      }
    }//koniec Metody()
  } //koniec Klasy ChoceTouchListener


  /*Klasa do sprawdzania czy podczas zmiany ustawien uzytkownik zmienil (klikaniem) zrodlo
  obrazkow */
  /* (lub wykonal dzialanie rownowazne zmianie zrodla) */
  /* dotyczy (na razie) tylko zródła i ścieżki */
  class KombinacjaOpcji {

    private boolean ZRODLEM_JEST_KATALOG;

    private String WYBRANY_KATALOG;

    private int POZIOM;

    private boolean JOBCY;

    //czy jezyk obcy:
    private boolean jAng;

    private boolean jNiem;

    private boolean jFranc;


    KombinacjaOpcji() {
      pobierzZeZmiennychGlobalnych();
    }

    void pobierzZeZmiennychGlobalnych() {
      ZRODLEM_JEST_KATALOG = mGlob.ZRODLEM_JEST_KATALOG;
      WYBRANY_KATALOG = mGlob.WYBRANY_KATALOG;
      POZIOM = mGlob.POZIOM;
    }


    boolean takaSamaJak(KombinacjaOpcji nowaKombinacja) {
      /* ****************************************************** */
      /* Sprawdza, czy kombinacje wybranych opcji sa takie same */
      /* ****************************************************** */
      if (this.ZRODLEM_JEST_KATALOG != nowaKombinacja.ZRODLEM_JEST_KATALOG) {
        return false;
      }
      if (!this.WYBRANY_KATALOG.equals(nowaKombinacja.WYBRANY_KATALOG)) {
        return false;
      }
      if (!(this.POZIOM == nowaKombinacja.POZIOM)) {
        return false;
      }
      if (!(this.JOBCY == nowaKombinacja.JOBCY)) {
        return false;
      }
      if (!(this.jAng == nowaKombinacja.jAng)) {
        return false;
      }
      if (!(this.jNiem == nowaKombinacja.jNiem)) {
        return false;
      }
      if (!(this.jFranc == nowaKombinacja.jFranc)) {
        return false;
      }

      return true;

    } //koniec Metody()

    boolean isReturnToPolish(KombinacjaOpcji nowaKombinacja) {
      //Okresla, czy nowe opcje to powrot z jezya obcego do polskiego
      //w starych opcjach nie bylo j. obcego, wiec to nie moze byc powrot do j.pol:
      if (!(this.jAng || this.jNiem || this.jFranc)) {
        return false;
      }

      //Sterowanie doszlo tutaj, wiec w starych opcjach byl j.obcy:
      if (!(nowaKombinacja.jAng || nowaKombinacja.jNiem || nowaKombinacja.jFranc)) {
        return true;
      }

      return false;

    } //koniec Metody()

  } //class wewnetrzna


}

