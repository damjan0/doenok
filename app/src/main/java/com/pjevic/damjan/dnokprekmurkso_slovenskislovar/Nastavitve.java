package com.pjevic.damjan.dnokprekmurkso_slovenskislovar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class Nastavitve extends AppCompatActivity {

    Button steviloPrikazanih;
    int kaPiseNaTipki;

    TextView testKeriJezik;
    ImageButton sloZastava;
    ImageButton engZastava;
    ImageButton prekZastava;
    Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nastavitve);


        //MAX STEVILO PRIKAZANIH PREVODOV
        steviloPrikazanih = (Button) findViewById(R.id.spinner);
        //tolko kolko je trenutno naj se gor napiše
        steviloPrikazanih.setText(Integer.toString(MainActivity.HOW_MANY_RESULTS));

        steviloPrikazanih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kaPiseNaTipki = Integer.parseInt(steviloPrikazanih.getText().toString());
                kaPiseNaTipki++;

                //dolocimo min in max kolko bo možnih
                if (kaPiseNaTipki > 6) kaPiseNaTipki = 2;

                MainActivity.HOW_MANY_RESULTS = kaPiseNaTipki;
                steviloPrikazanih.setText(Integer.toString(kaPiseNaTipki));

                //shraniti za naslednjo rabo
                SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("your_int_key", kaPiseNaTipki);
                editor.commit();

            }
        });


        //ZASTAVA SPREMENI JEZIK
        testKeriJezik = (TextView) findViewById(R.id.steviloPrevodovText);
        sloZastava = (ImageButton) findViewById(R.id.prviJezik);
        engZastava = (ImageButton) findViewById(R.id.drugiJezik);
        prekZastava = (ImageButton) findViewById(R.id.tretjiJezik);

        engZastava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //preverici je ang ze zbran jezik
                if(!(testKeriJezik.getText().toString().equals("Maximum number of Results"))){
                    setLocale("af");
                    //locale = new Locale("af-rZA");
                     //spremejniJezik();
                }else{
                    Toast.makeText(Nastavitve.this, getString(R.string.jezikZE), Toast.LENGTH_SHORT).show();
                }
            }
        });

        sloZastava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //preverici je slo ze zbran jezik
                if(!(testKeriJezik.getText().toString().equals("Število rezultatov"))){
                    setLocale("sl");
                    //locale = new Locale("sl");
                    //spremejniJezik();
                }else{
                    Toast.makeText(Nastavitve.this, getString(R.string.jezikZE), Toast.LENGTH_SHORT).show();
                }
            }
        });


        prekZastava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //preverici je prek ze zbran jezik
                if(!(testKeriJezik.getText().toString().equals("Kelko rezultatov naj pokaže"))){
                    setLocale("so");
                    //locale = new Locale("so");
                    //spremejniJezik();
                }else{
                    Toast.makeText(Nastavitve.this, getString(R.string.jezikZE), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
/*
    public void spremejniJezik(){
        //nastavi jezik
        Locale.setDefault(locale);

        //shrani jezik za naslednjic
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(
                config,
                getResources().getDisplayMetrics()
        );

        //za optimalno delovanje trbej restartati app
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }
    */
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        //za optimalno delovanje trbej restartati app
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

/*
    @Override
    public void onBackPressed() {
        //shrani nastavitve
    }*/
}