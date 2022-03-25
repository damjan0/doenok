package com.pjevic.damjan.dnokprekmurkso_slovenskislovar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Priljubljene extends AppCompatActivity {

   // private String[][] tabelaPriljubljenih;
    private String filename ="a txt";   //ka koli notri
    String seznamPriljubljenih;

    TextView errorNiBesed; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priljubljene);

        TextView errorNiBesed = (TextView)findViewById(R.id.errorSporocilo);    //sporocilo da ni priljubljenih besed

        seznamPriljubljenih = beriIzDatoteke();

        //aaa.setText(seznamPriljubljenih);
        String[] textArray = seznamPriljubljenih.split("\n");   //ustvari tabelo priljubljenih

        LinearLayout ln = (LinearLayout) this.findViewById(R.id.layoutLinearen);
        for( int i = 0; i < textArray.length; i++ )
        {
            errorNiBesed.setVisibility(View.INVISIBLE);     //po difolti skirj sporocilo
            if(textArray.length==1 && textArray[i].equals("")){ errorNiBesed.setVisibility(View.VISIBLE); break;}       //ci nemamo nobene beseda se naj to ne izvede ker po difolti napravi eno prazno vrstico
            final TextView textView = new TextView(this);
            //EDIT THIS EDITTEXT
            //android:layout_marginTop="5dp"
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, 10, 0, 0);
            textView.setLayoutParams(params);
            //android:gravity="center|left"
            textView.setGravity(Gravity.CENTER | Gravity.LEFT);
            //padding ka do vecje
            textView.setPaddingRelative(0,10,0,10);
            //android:textColor="@color/temno_siva"
            textView.setTextColor(getResources().getColor(R.color.temno_siva));
            //android:drawableLeft="@drawable/empty_star"
            textView.setCompoundDrawablesWithIntrinsicBounds( getResources().getDrawable(R.drawable.full_star), null, null, null);
            //android:background="@drawable/custom_edittext"
            textView.setBackground(getResources().getDrawable(R.drawable.custom_edittext));
            //setid?  -- prvi 0 in tak naprej
            textView.setId(i);
            //settext
            textView.setText(textArray[i]);


            //na klik preveri ali se beseda vsebuje ce ja odstrani, ce ne dodaj  (star away + remove from list)
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nasabeseda = (String)textView.getText();
                    if (preveriAliBesedaObstaja(nasabeseda)) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.empty_star), null, null, null);
                        zbrisiBesedo(nasabeseda);
                        Toast.makeText(Priljubljene.this, getString(R.string.minFav), Toast.LENGTH_SHORT).show();
                    }else{
                        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.full_star), null, null, null);
                        vpisiVDatoteko(nasabeseda+"\n");   //vzemi besedo in jo napiši na v datoteko
                        Toast.makeText(Priljubljene.this, getString(R.string.dodFav), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //on dugi klik deli z prijatelji  (SHARE OPTION)
            textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            String nasabeseda = (String)textView.getText();
            String prekmurskiDel = nasabeseda.split(" --> ")[0];

            //Toast.makeText(Priljubljene.this, "Dugi klik besede: " + nasabeseda, Toast.LENGTH_SHORT).show();

            // ustvari okno za delitev besede
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String shareBody = getString(R.string.shareBody) + " " + prekmurskiDel + "?  #donok" ;
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
            return true;
        }
        });

            ln.addView(textView);
        }

    }


    //metodi prekoperani iz mainactivitya
    private void vpisiVDatoteko(String vsebina){
        try {
            //ustvarimo izhodni tok
            FileOutputStream os = openFileOutput(filename, Context.MODE_PRIVATE | Context.MODE_APPEND); //apend da se shrani kcoj in ne overwrita
            //zapisemo posredovano vsebino v datoteko
            os.write(vsebina.getBytes());
            //sprostimo izhodni tok
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String beriIzDatoteke(){
        // ustvarimo vhodni podatkovni tok
        FileInputStream inputStream;

        //ugotovimo, koliko je velika datoteka
        File file = new File(getFilesDir(), filename);
        int length = (int) file.length();

        //pripravimo spremenljivko, v katero se bodo prebrali podatki
        byte[] bytes = new byte[length];

        //preberemo podatke
        try {
            inputStream = openFileInput(filename);
            inputStream.read(bytes);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorNiBesed.setText(getString(R.string.niBranja));
            errorNiBesed.setVisibility(View.VISIBLE);
        }

        //podatke pretvorimo iz polja bajtov v znakovni niz
        String vsebina = new String(bytes);

        return vsebina;
    }
    private void zbrisiBesedo(String beseda){
        String celotenString = beriIzDatoteke();
        String celotenStringBrezBesede = "";

        celotenStringBrezBesede = celotenString.replace(beseda + "\n", "");

        //overwrite gor novi string brez one besede
        try {
            //ustvarimo izhodni tok
            FileOutputStream os = openFileOutput(filename, Context.MODE_PRIVATE); //overwrite datoteko
            os.write(celotenStringBrezBesede.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean preveriAliBesedaObstaja(String beseda){
        String vsebina = beriIzDatoteke();

        if (vsebina.contains(beseda)) return true;
        return false;
    }

}
