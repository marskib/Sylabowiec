package pl.autyzmsoft.sylabowiec;
/**
 * Timer wzorowany na Lazarusie
 */

import android.os.Handler;


public class MojTimer  {

    private Handler handler;
    private Runnable runnable = null;
    private int interval;
    private int iter;  //liczba iteracji/cykli do wykonania; 0-nieskonczonosc


    //Konstruktor:
    public MojTimer(final int interval, int iter) {

        this.interval = interval;
        this.iter     = iter;
        handler  = new Handler();

    }  //koniec Konstruktora()


    public void setOnTimer (Runnable procedurka) {
        this.runnable = procedurka;
    }

    public void start() {
        for (int i = 0; i < iter; i++) {
            handler.postDelayed(runnable,i*interval);
        }
    }


}
