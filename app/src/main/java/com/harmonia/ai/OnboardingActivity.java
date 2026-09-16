package com.harmonia.ai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class OnboardingActivity extends Activity {
    @Override public void onCreate(Bundle b) { super.onCreate(b); build(); }

    private void build() {
        ScrollView scroll=new ScrollView(this); scroll.setBackgroundColor(Ui.BG);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); Ui.padding(root,this,22,36,22,28); scroll.addView(root);
        TextView mark=Ui.text(this,"♥",38,Ui.PINK,true); root.addView(mark);
        TextView h=Ui.text(this,"Rencontrez moins.\nRencontrez mieux.",31,Ui.TEXT,true); h.setLineSpacing(0,1.08f); Ui.padding(h,this,0,12,0,8); root.addView(h);
        TextView p=Ui.text(this,"Harmonia AI combine vos préférences, vos intentions et votre mode de vie pour sélectionner les profils qui ont réellement du sens pour vous.",15,Ui.MUTED,false); p.setLineSpacing(0,1.2f); root.addView(p);

        EditText name=input("Votre prénom"); root.addView(name,lpTop(22));
        EditText age=input("Votre âge"); age.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); root.addView(age,lpTop(10));
        EditText intent=input("Ce que vous recherchez (ex. relation sérieuse)"); root.addView(intent,lpTop(10));
        EditText interests=input("Vos centres d’intérêt"); root.addView(interests,lpTop(10));

        TextView consent=Ui.text(this,"La préférence visuelle est personnalisée selon vos choix. L’application ne calcule pas de beauté universelle et n’analyse pas d’attributs sensibles.",12,Ui.MUTED,false); consent.setLineSpacing(0,1.2f); Ui.padding(consent,this,2,16,2,12); root.addView(consent);

        Button go=new Button(this); go.setText("Créer mon profil IA"); go.setAllCaps(false); go.setTextSize(16); go.setTextColor(Ui.TEXT); go.setBackground(Ui.rounded(Ui.PINK,20,this));
        go.setOnClickListener(v->{ getSharedPreferences("harmonia",MODE_PRIVATE).edit().putBoolean("onboarded",true).putString("name",name.getText().toString()).apply(); startActivity(new Intent(this,MainActivity.class)); finish(); });
        root.addView(go,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));
        setContentView(scroll);
    }

    private EditText input(String hint) {
        EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Ui.MUTED); e.setTextColor(Ui.TEXT); e.setTextSize(15); e.setSingleLine(true); e.setBackground(Ui.stroke(Ui.SURFACE,Ui.SURFACE2,1,16,this)); Ui.padding(e,this,15,0,15,0); return e;
    }

    private LinearLayout.LayoutParams lpTop(int top) { LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,56)); lp.topMargin=Ui.dp(this,top); return lp; }
}
