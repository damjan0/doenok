package com.pjevic.damjan.dnokprekmurkso_slovenskislovar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //grafični elementi
    private EditText vnosno;
    //private TextView izpisno;

    private TextView errorSporocilo;

    LinearLayout ln;

    //spremenljivke
    private String vsebina;         //vsebina onoga polja
    private String firstLetter;
    private String prevfirstLetter = "X"; //prejsnja prva crka - skrbi za to , da ne nalagamo vsakic ponovno ko tipkamo besede
    private String html;

    private StringBuilder bilder;
    public String[][] tabelaPrevodov;            //za primerjavo - brez sumnikov pa brez onih crtic na samoglasnikaj
    public String[][] tabelaPrevodovSumniki;     //za prikaz

    private String filename ="a txt";

    List prevodiZaIzpisat;   //novi list kama shranjujemo elemente ki se matchajo v ze konci obliki

    public static int HOW_MANY_RESULTS = 3;
    //public static SharedPreferences prefForHowMany;
    public int counter;

    //drawables
    Drawable empty;
    Drawable full;
    Drawable iks;
    Drawable searchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //custom title bar: prva vrstica odstrani napis, druga doda drug layout gor -- lejko se tudi da slika - check overflow
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.poravnaj_actionbar); //naloži xml layout ki poravn
        setContentView(R.layout.activity_main);

        //check if app firt time loads
        Boolean isFirst = getSharedPreferences("FirstTime", MODE_PRIVATE).getBoolean("isfirstrun",true);
        if(isFirst==true){
            //alertbox kot zivjo
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getString(R.string.welcome));
            alertDialog.setMessage(getString(R.string.welcomeSporocilo));
            alertDialog.show();

            //nalozi testni primer med priljubljene -- kot neke vrste tutorial za prvo uporabo appa
            vpisiVDatoteko(getString(R.string.testnaBeseda) +"\n");

            getSharedPreferences("FirstTime", MODE_PRIVATE).edit().putBoolean("isfirstrun",false).commit();
        }


        vnosno = (EditText) findViewById(R.id.iskanaBeseda);
        //izpisno = (TextView)findViewById(R.id.prevod);

        errorSporocilo = (TextView) findViewById(R.id.errSporocilo);

        empty = getResources().getDrawable( R.drawable.empty_star );
        full = getResources().getDrawable( R.drawable.full_star );
        iks = getResources().getDrawable( R.drawable.ic_x );
        searchIcon = getResources().getDrawable( R.drawable.search );

        ln = (LinearLayout)findViewById(R.id.layoutVsehBesed);

        //HOW_MANY_RESULTS posodobi iz sharred preferenc
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        //ci shared prazen je 3 difolt
         HOW_MANY_RESULTS = sp.getInt("your_int_key", 3);


        vnosno.addTextChangedListener(new TextWatcher() {
            // sproti kak pišeš se bo to delalo
            public void afterTextChanged(Editable s) {
                vsebina = vnosno.getText().toString().toLowerCase();;   //poberi to kar je v iskalnon nizi pa daj vse v lowercase
                //iks ka zbrise vsebino:
                if(!(vnosno.getText().toString().matches(""))){
                    //zbrise ono ikonco -- ci nej daj tan pri vnosnon left searchicon pa pri paddingi start na 0 pa zbrisati naslednji dve vrstici
                    int test = vnosno.getHeight();
                    vnosno.getLayoutParams().height = test;
                    vnosno.setCompoundDrawablesWithIntrinsicBounds( null, null, iks, null);
                    vnosno.setPaddingRelative(50,0,25,0);


                    //iks zbrise vsebino  (StackOverflow):
                    vnosno.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(event.getAction() == MotionEvent.ACTION_UP) {
                                try {
                                    if (event.getRawX() >= (vnosno.getRight() - vnosno.getCompoundDrawables()[2].getBounds().width())) {
                                        vnosno.getText().clear();
                                        return true;
                                    }
                                }catch (NullPointerException bb) { System.err.println("IndexOutOfBoundsException: " + bb.getMessage());}
                            }return false;
                        }
                    });

                }else{
                    vnosno.setCompoundDrawablesWithIntrinsicBounds( searchIcon, null, null, null);
                    vnosno.setPadding(0,0,0,0);
                    ln.removeAllViews(); //zbrisi prikaze
                    vsebina = "X"; //ce vsebina prazna daj X ker stran ne obstaja in zbriši rezultate
                    errorSporocilo.setVisibility(View.INVISIBLE);
                    return;
                }


                //prvo crko besedila ka lejko te ono spletno stran nalozimo
                firstLetter = vsebina.substring(0,1);   //za iskanje po websajti
                if (firstLetter.equals("š")) firstLetter= "s-s";
                if (firstLetter.equals("ž")) firstLetter= "z-z";
                if (firstLetter.equals("č")) firstLetter= "c-c";

                html = "http://www.pomurski-muzej.si/izobrazevanje/gradiva-pomurja/slovar/" + firstLetter + "/";
                vsebina = normalize(vsebina);   //odstrani vse sumnike pa crtice na crkaj


                //pojdi na internet iskat samo ko mamo novo prvo crko in moremo dol potegnit vse iz one crke
                if (!prevfirstLetter.equals(firstLetter)) {
                    //check if internet connection
                    if (checkNet()) {
                        prevfirstLetter=firstLetter;
                        new pojdiNaNet().execute(); //vklopi jsoup
                        errorSporocilo.setVisibility(View.INVISIBLE);
                    } else {
                        errorSporocilo.setText(getString(R.string.niInterneta));
                        errorSporocilo.setVisibility(View.VISIBLE);
                    }
                    //izpisno.setText(html);  //izpisi html
                }else{
                    najdiUjemanje();        //ce smo to crko meli ze prej je seznam prevodov ze ustvarjen in lahko preskocimo iskanje na internetu
                }

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        //BOTTOM BAR  -- ka de delalo či klikneš gor
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                   case R.id.bottom_four:
                       startActivityRand();
                       vnosno.setText("");
                       break;
                    case R.id.bottom_two:
                        //Toast.makeText(MainActivity.this, "PRILJUBLJENE", Toast.LENGTH_SHORT).show();
                        startActivityPril();
                        vnosno.setText(""); //pobrisi vnosno ker drugače ci v priljubljenih odtranin zvezdico de še tu izdak kazalo ker se nej na novo nalozilo
                        break;
                }
                return true;
            }
        });


        //tu naprej ci kaj trbej se oncreate
    }

    //OPTIONS BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    //odzivi na gumbe v menuji
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            //Predlagaj prevod
            case R.id.predlagajPrevod:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://goo.gl/forms/qJuQ7eIhsRmuD5a03"));
                startActivity(intent);
                vnosno.setText("");
                return true;
            //Nastavitve
            case R.id.nastavivee:
                startActivityNastavitve();
                vnosno.setText("");
                return true;
            //Več informacij
            case R.id.vecInformacij:
                startActivityInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class pojdiNaNet extends AsyncTask<Void,Void,Void>{

        //ka ven pobere
        @Override
        protected Void doInBackground(Void... voids) {
            //nalozi seznam prevodov za ono crko
            dolPotegniSeznamPrevodov();
            return null;
        }

        //PREVERI ali med prevodi se najde nasa beseda
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //preveri ujemannje
            najdiUjemanje();
        }
    }

    public void dolPotegniSeznamPrevodov(){
        try {
            Document doc = Jsoup.connect(html).get();   //pobere html
            Elements prevodi = doc.select("p"); //pobere samo prevode dol
            bilder = new StringBuilder();               //stringbilder keroga urejam

            for (Element prevod : prevodi){             //idi skozi vse prevode
                bilder.append(prevod.text()).append("\n");      //za on string napravi enter za vsakin prevodon
                counter++;
            }
            tabelaPrevodov = new String[counter+1][2];      //samo za primerjati  - nucamo samo prekmurski del
            tabelaPrevodovSumniki = new String[counter+1][2];   //za ispis na koncci - nucamo oboje slo pa prek

            //vstavi vse elements v tabelo stringov namesto elements:
            int i=0;
            for (Element prevod : prevodi){
                if (i>=counter) break;
                String vrsticaBesede = prevod.text();       //npr. "ka - kaj"  -- trbej še razdeliti na prekmurski in slovenski del
                tabelaPrevodov[i][0] = vrsticaBesede.split(" - ")[0];   //shrani prekmursko
                tabelaPrevodovSumniki[i][0] = vrsticaBesede.split(" - ")[0];
                try {
                    //tabelaPrevodov[i][1] = vrsticaBesede.split(" - ")[1];      //shrani prevod
                    tabelaPrevodovSumniki[i][1] = vrsticaBesede.split(" - ")[1];
                }catch (IndexOutOfBoundsException bb) { System.err.println("IndexOutOfBoundsException: " + bb.getMessage());}

                tabelaPrevodov[i][0] = normalize(tabelaPrevodov[i][0]);   //za primerjavo odstranimo sumnike pa crtice
                //tabelaPrevodov[i][1] = normalize(tabelaPrevodov[i][1]);
                i++;
            }

        } catch (IOException e) {e.printStackTrace();}

    }
    public void najdiUjemanje(){
        prevodiZaIzpisat = new ArrayList();    //novi list kama shranjujemo elemente ki se matchajo v ze konci obliki

        for(int i=0;i<counter;i++){     //preveri ali tabela vsebuje nas string
        try{
            if(tabelaPrevodov[i][0].startsWith(vsebina)){
                //prevodiZaIzpisat.add("A: " + tabelaPrevodovSumniki[i][0] + "  B: " + tabelaPrevodovSumniki[i][1] + "\n"); //doda najden prevod na seznam -- za izpis vseh
                //ce vsebuje keri koli element nas niz ga dodaj na novi list v ze obliki kredik za izpis
                if (tabelaPrevodovSumniki[i][1]!=null) {  //ce je null drugi del, pomeni da je nekaj drugo iz splene strani in ne prevod.. skip it
                    prevodiZaIzpisat.add(tabelaPrevodovSumniki[i][0] + "  -->  " + tabelaPrevodovSumniki[i][1]);
                }
                //pri crki k se prevodi ponavlajo -- ohrani samo uniques ker se ponavlajo
                if (firstLetter.equals("k")){
                    prevodiZaIzpisat = new ArrayList(new LinkedHashSet<String>(prevodiZaIzpisat));       //linked hash bo ohranil vrstni red
                }

            }
        } catch (NullPointerException e) {}
        }

        //pretvori arraylist v navaden string array
        Object[] objectList = prevodiZaIzpisat.toArray();
        String[] tabelaUjemanj =  Arrays.copyOf(objectList,objectList.length,String[].class);

        //NALOŽI SEZNAM BESED2
        naloziSeznamUjemanj(tabelaUjemanj);


        //izpisno.setText(prevodiZaIzpisat.toString());

        //ERROR MESEĐ ci ni prevoda
        if(prevodiZaIzpisat.size()==0 && !(vnosno.getText().toString().matches(""))) {        //prevod ne obstaja - javi uporabniki
            errorSporocilo.setText(getString(R.string.niPrevoda));  //prevod ne obstaja
            errorSporocilo.setVisibility(View.VISIBLE);
        }else{
            errorSporocilo.setVisibility(View.INVISIBLE);
        }

    }

    public void naloziSeznamUjemanj(String[] seznamVsehNajdenih){
        //zbrisi to vse ka je ze notri
        ln.removeAllViews();

        //nalozi samo tri prevode (zbrisati ci sces vse + dodati v xml se en scrollview):
        if(seznamVsehNajdenih.length>HOW_MANY_RESULTS)
            seznamVsehNajdenih = Arrays.copyOfRange(seznamVsehNajdenih,0,HOW_MANY_RESULTS);

        for( int i = 0; i < seznamVsehNajdenih.length; i++ )
        {
            errorSporocilo.setVisibility(View.INVISIBLE);     //po difolti skirj sporocilo
            if(seznamVsehNajdenih.length==1 && seznamVsehNajdenih[i].equals("")){ errorSporocilo.setVisibility(View.VISIBLE); break;}       //ci nemamo nobene beseda se naj to ne izvede ker po difolti napravi eno prazno vrstico
            final TextView textView = new TextView(this);
            //EDIT THIS EDITTEXT
            //margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(255, 15, 90, 0);  //for some reason *3
            textView.setLayoutParams(params);
            //gravity
            textView.setGravity(Gravity.CENTER | Gravity.LEFT);
            //textColor
            textView.setTextColor(getResources().getColor(R.color.temno_siva));
            //drawableLeft="@drawable/empty_star"
            if (preveriAliBesedaObstaja(seznamVsehNajdenih[i])) {
                textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.full_star), null, null, null);
            }else{
                textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.empty_star), null, null, null);
            }
            //android:background="@drawable/custom_edittext"
            textView.setBackground(getResources().getDrawable(R.drawable.custom_edittext));
            //setid
            textView.setId(i);
            //settext
            textView.setText(seznamVsehNajdenih[i]);


            //na klik preveri ali se beseda vsebuje ce ja odstrani, ce ne dodaj  (star away + remove from list)
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nasabeseda = (String)textView.getText();
                    if (preveriAliBesedaObstaja(nasabeseda)) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.empty_star), null, null, null);
                        zbrisiBesedo(nasabeseda);
                        Toast.makeText(MainActivity.this, getString(R.string.minFav), Toast.LENGTH_SHORT).show();
                    }else{
                        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.full_star), null, null, null);
                        vpisiVDatoteko(nasabeseda+"\n");   //vzemi besedo in jo napiši na v datoteko
                        Toast.makeText(MainActivity.this, getString(R.string.dodFav), Toast.LENGTH_SHORT).show();
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

    public void startActivityPril(){
        Intent intent = new Intent(this, Priljubljene.class);
        //intent.putExtra(EXTRA_MESSAGE, vnos);
        startActivity(intent);
    }
    public void startActivityInfo(){
        Intent intent = new Intent(this, Info.class);
        //intent.putExtra(EXTRA_MESSAGE, vnos);
        startActivity(intent);
    }
    public void startActivityNastavitve(){
        Intent intent = new Intent(this, Nastavitve.class);
        //intent.putExtra(EXTRA_MESSAGE, vnos);
        startActivity(intent);
    }
    public void startActivityRand(){
        Intent intent = new Intent(this, NakljucnaBeseda.class);
        //intent.putExtra(EXTRA_MESSAGE, vnos);
        startActivity(intent);
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
    //get rid od sumniki pa crtice na samoglasnikaj
    public static String normalize(String theString) {          //Project: GT-FHIR-Master  File: QueryUtilities.Java   (https://www.javatips.net/api/java.text.normalizer)
        char[] out = new char[theString.length()];
        theString = Normalizer.normalize(theString, Normalizer.Form.NFD);
        int j = 0;
        for (int i = 0, n = theString.length(); i < n; ++i) {
            char c = theString.charAt(i);
            if (c <= '') {
                out[j++] = c;
            }
        }
        //		return new String(out).toUpperCase();
        return new String(out);
    }
}
