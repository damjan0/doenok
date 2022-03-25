package com.pjevic.damjan.dnokprekmurkso_slovenskislovar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;

public class NakljucnaBeseda extends AppCompatActivity {
    String seznamPriljubljenih;

    TextView errorNiNeta;
    private String filename ="a txt";
    LinearLayout ln;
    String nasaFirstLetter;
    String nasaBeseda;

    private String html;
    int counter;
    public static int HOW_MANY_WORDS = 1;
    private StringBuilder bilder;
    public String[][] tabelaPrevodov;
    char[] abeceda = "abcdefghijklmnoprstuvzčšž".toCharArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nakljucna_beseda);
        //check netzs
        errorNiNeta = (TextView) findViewById(R.id.errorNoNet);
        //kam se bo beseda notri naložila
        ln = (LinearLayout) findViewById(R.id.lnRand);
        ln.removeAllViews();

        if (checkNet() == false) {
            errorNiNeta.setVisibility(View.VISIBLE);
        } else {
            errorNiNeta.setVisibility(View.INVISIBLE);
            //najdi random crko
            //ci bi sceli meti vec beesed
            for(int i=0; i<HOW_MANY_WORDS; i++) {
                int rnd = new Random().nextInt(abeceda.length);
                nasaFirstLetter = Character.toString(abeceda[rnd]);
                if (nasaFirstLetter.equals("š")) nasaFirstLetter= "s-s";
                if (nasaFirstLetter.equals("ž")) nasaFirstLetter= "z-z";
                if (nasaFirstLetter.equals("č")) nasaFirstLetter= "c-c";

                html = "http://www.pomurski-muzej.si/izobrazevanje/gradiva-pomurja/slovar/" + nasaFirstLetter + "/";

                new pojdiNaNetTwo().execute();
            }
        }
    }

    public void naloziBesedo(){
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
        if (preveriAliBesedaObstaja(nasaBeseda)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.full_star), null, null, null);
        }else{
            textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.empty_star), null, null, null);
        }
        //android:background="@drawable/custom_edittext"
        textView.setBackground(getResources().getDrawable(R.drawable.custom_edittext));
        //setid?  -- prvi 0 in tak naprej
        textView.setId(0);
        //settext
        textView.setText(nasaBeseda);


        //na klik preveri ali se beseda vsebuje ce ja odstrani, ce ne dodaj  (star away + remove from list)
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nasabeseda = (String)textView.getText();
                if (preveriAliBesedaObstaja(nasabeseda)) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.empty_star), null, null, null);
                    zbrisiBesedo(nasabeseda);
                    Toast.makeText(NakljucnaBeseda.this, getString(R.string.minFav), Toast.LENGTH_SHORT).show();
                }else{
                    textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.full_star), null, null, null);
                    vpisiVDatoteko(nasabeseda+"\n");   //vzemi besedo in jo napiši na v datoteko
                    Toast.makeText(NakljucnaBeseda.this, getString(R.string.dodFav), Toast.LENGTH_SHORT).show();
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

        ln.addView(textView);}

    public class pojdiNaNetTwo extends AsyncTask<Void,Void,Void> {

        //ka ven pobere
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(html).get();   //pobere html
                Elements prevodi = doc.select("p"); //pobere samo prevode dol
                bilder = new StringBuilder();               //stringbilder keroga urejam

                for (Element prevod : prevodi){             //idi skozi vse prevode
                    bilder.append(prevod.text()).append("\n");      //za on string napravi enter za vsakin prevodon
                    counter++;
                }
                tabelaPrevodov = new String[counter+1][2];      //samo za primerjati  - nucamo samo prekmurski del

                //vstavi vse elements v tabelo stringov namesto elements:
                int i=0;
                for (Element prevod : prevodi){
                    if (i>=counter) break;
                    String vrsticaBesede = prevod.text();       //npr. "ka - kaj"  -- trbej še razdeliti na prekmurski in slovenski del
                    tabelaPrevodov[i][0] = vrsticaBesede.split(" - ")[0];   //shrani prekmursko
                    try {
                        tabelaPrevodov[i][1] = vrsticaBesede.split(" - ")[1];      //shrani prevod
                    }catch (IndexOutOfBoundsException bb) { System.err.println("IndexOutOfBoundsException: " + bb.getMessage());}
                    i++;
                }
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //random beseda iz nalozene tabele
            int rndTwo = new Random().nextInt(tabelaPrevodov.length);
            String prekDel = tabelaPrevodov[rndTwo][0];
            String sloDel = tabelaPrevodov[rndTwo][1];

            //zna bit en ali dva prevoda ki je en del null drugi pa kar nekaj  -- to odstrani te te del
            if (!(sloDel==null)){
                nasaBeseda = prekDel +  "  -->  " + sloDel ;
                naloziBesedo();
            }else{
                new pojdiNaNetTwo().execute();
            }
        }
    }

    //Shrani oz. preberi datoteko -- TNUV vaja5_part2
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
        }

        //podatke pretvorimo iz polja bajtov v znakovni niz
        String vsebina = new String(bytes);

        return vsebina;
    }
    //preveri ali beseda ka jo ščemo dati na seznam priljubljeni že obstaja v našen seznami
    private boolean preveriAliBesedaObstaja(String beseda){
        //preberi celotno datoteko priljubljenih
        String vsebina = beriIzDatoteke();

        if (vsebina.contains(beseda)) return true;
        return false;
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

    //check if there is netz
    public boolean checkNet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else {
            return false;
        }
    }
}
