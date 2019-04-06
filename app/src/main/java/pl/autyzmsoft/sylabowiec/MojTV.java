package pl.autyzmsoft.sylabowiec;


import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

/**
 * Klasa do etykiet-sylab do ukladania
*/



public class MojTV extends android.support.v7.widget.AppCompatTextView implements View.OnClickListener{

  private boolean inArea   = false; //czy jest w Obszarze
  private String  origSyl  = "*";   //Sylaba z oryginalu; rozwiazuje problem Ola->OLA->Ola
  private int mBlink = 0;         //do mrugania, zeby mozna bylo odwolac sie z klasy wewnetrznej Runnable

  public MojTV(Context context) { super(context);
  setOnClickListener(this);
  }

  //Potrzebny w xml'u:
  public MojTV(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOnClickListener(this);
  }

  public boolean isInArea() {
    return inArea;
  }

  @Override
  public void onClick(final View view) {
    Toast.makeText(getContext(), "Kliknieto sylabe...2", Toast.LENGTH_SHORT).show();
  }

  public void setInArea(boolean inArea) {
    this.inArea = inArea;
  }

  public String getOrigSyl() {return origSyl; }

  public void setOrigSyl(String origSyl) {
    this.origSyl = origSyl;
  }


  public void blink(int ileRazy) {
    /* Mruganie etykietą */

    mBlink = 2* ileRazy -1;  //nieparzysta, zeby pozostal na RED
    final Handler h = new Handler();
    h.postDelayed(new Runnable() {
      @Override
      public void run() {
        int currColor = MojTV.this.getCurrentTextColor();

        if (currColor== Color.BLACK)
          currColor = Color.RED;
        else
          currColor = Color.BLACK;

        MojTV.this.setTextColor(currColor);
        mBlink--;
        if (mBlink>0) {
          h.postDelayed(this,200);
        }
      }
    }, 500);
  } //koniec metody()



  /**
   * Make a View Blink for a desired duration
   *
   * @param duration for how long in ms will it blink
   * @param offset   start offset of the animation
   * @param ileRazy  ile razy ma mrugnac
   * @param kolor    jakim kolorem ma mrugac
   * zrodlo: https://gist.github.com/cesarferreira/4fcae632b18904035d3b
   */
  public void makeMeBlink(int duration, int offset, int ileRazy, int kolor) {

    final int savedColor = this.getCurrentTextColor();

    MojTV.this.setTextColor(kolor);
    Animation anim = new AlphaAnimation(0.0f, 1.0f);
    anim.setDuration(duration);
    anim.setStartOffset(offset);
    anim.setRepeatMode(Animation.REVERSE);
    //anim.setRepeatCount(Animation.INFINITE);
    anim.setRepeatCount(ileRazy);
    this.startAnimation(anim);

    //Przywrocenie pierwotnego koloru etykiecie po skonczonej animacji:
    Handler h = new Handler();
    h.postDelayed(new Runnable() {
      @Override
      public void run() {
        MojTV.this.setTextColor(savedColor);
      }
    },duration*(ileRazy+2)+offset);  //wyr. arytm. - doswiadczalnie....

  } //koniec Metody()


}  //koniec Klasy



