package pl.autyzmsoft.sylabowiec;
/**
 * Timer wzorowany na Lazarusie + mozna zaplanowac liczbe iteracji(wykonań/ticknięć)
 * 2019.05.28
 */

import android.os.Handler;


public class MojTimer  {

    private Handler handler;

    //Runnable - rocedura do wykonania na OnTimer:
    //tego nie inicjujemy tutaj, inicjowane bedzie na 'zewnątrz' i przekazywane do w/w obiektu
    private Runnable mRunnable = null;

    private int interval;    //jak często ma być odpalana procedura siedząca w mRunnable
    private int iter;        //liczba iteracji/cykli do wykonania


    public MojTimer(final int interval, int iter) {

        this.interval = interval;
        this.iter     = iter;
        handler  = new Handler();

    }  //koniec Konstruktora()


    public void setOnTimer (Runnable procedurka) {
    /**
     "procedur'ka", ktora ma byc wykonana na OnTimer
     Procedur'ke ustala sie w kodzie gdzie indziej, np. w MainActivity, przypisując
     ją do obiektu Runnable i przekazując go do obiektu MojTimer poprzez w/w setOnTimer(...)
    */
        this.mRunnable = procedurka;
    }


    public void start() {
    /**
    Wykonanie tego, co siedzi (zostło wstawione przy pomocy setOnTimer()) w składowej mRunnable
    */
        for (int i = 0; i < iter; i++) {
            handler.postDelayed(mRunnable,i*interval); //mnożnik powoduje, że będzie się wykonywało (=kolejkowalo) w odpowiednich odstepach
        }
    }  //koniec Metody()




}
