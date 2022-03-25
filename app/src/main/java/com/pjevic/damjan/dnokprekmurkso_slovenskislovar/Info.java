package com.pjevic.damjan.dnokprekmurkso_slovenskislovar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Info extends AppCompatActivity {
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ImageView sendEmail = (ImageView)findViewById(R.id.slikaPoste);
        ImageView easterEgg = (ImageView)findViewById(R.id.slikaAppa);


        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //send email
                email = "damjan@pjevic.com";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
                try {
                    startActivity(Intent.createChooser(intent, "Pošlji sporočilo"));
                } catch (android.content.ActivityNotFoundException ex) {}
            }
        });

        //easter egg if long press image
        easterEgg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(Info.this, "Easter Egg", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
