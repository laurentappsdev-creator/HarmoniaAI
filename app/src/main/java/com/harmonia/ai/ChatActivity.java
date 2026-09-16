package com.harmonia.ai;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChatActivity extends Activity {
    private LinearLayout messages;
    private ScrollView scroll;

    @Override public void onCreate(Bundle b) { super.onCreate(b); build(); }

    private void build() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Ui.BG);
        LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.VERTICAL); Ui.padding(head,this,18,18,18,12);
        TextView back=Ui.text(this,"‹ Retour",14,Ui.MUTED,true); back.setOnClickListener(v->finish()); head.addView(back);
        TextView title=Ui.text(this,"Assistant Harmonia",22,Ui.TEXT,true); Ui.padding(title,this,0,8,0,2); head.addView(title); head.addView(Ui.text(this,"Il vous aide à découvrir vos affinités sans parler à votre place.",12,Ui.MUTED,false)); root.addView(head);
        scroll=new ScrollView(this); messages=new LinearLayout(this); messages.setOrientation(LinearLayout.VERTICAL); Ui.padding(messages,this,16,8,16,12); scroll.addView(messages); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addBubble("J’ai trouvé 4 profils très compatibles aujourd’hui. Emma partage plusieurs de vos centres d’intérêt et votre score de compatibilité atteint 91 %.",false);
        addBubble("Demandez-moi par exemple : « trouve-moi quelqu’un qui aime la nature et habite à moins de 30 km ».",false);
        EditText input=new EditText(this); input.setSingleLine(true); input.setHint("Écrivez à l’IA…"); input.setHintTextColor(Ui.MUTED); input.setTextColor(Ui.TEXT); input.setBackground(Ui.stroke(Ui.SURFACE,Ui.SURFACE2,1,18,this)); Ui.padding(input,this,16,0,16,0);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,Ui.dp(this,58)); ip.setMargins(Ui.dp(this,14),Ui.dp(this,6),Ui.dp(this,14),Ui.dp(this,14)); root.addView(input,ip);
        input.setOnEditorActionListener((v,action,event)->{ String q=input.getText().toString().trim(); if(q.isEmpty()) return false; addBubble(q,true); input.setText(""); addBubble(reply(q),false); scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN)); return true;});
        input.setOnKeyListener((v,key,event)->{ if(key==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_UP){ v.performClick(); return false;} return false;});
        setContentView(root);
    }

    private void addBubble(String text,boolean mine) {
        TextView b=Ui.text(this,text,14,Ui.TEXT,false); b.setLineSpacing(0,1.15f); Ui.padding(b,this,14,11,14,11); b.setBackground(Ui.rounded(mine?Ui.VIOLET:Ui.SURFACE,18,this));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2); lp.gravity=mine?Gravity.RIGHT:Gravity.LEFT; lp.bottomMargin=Ui.dp(this,9); lp.leftMargin=mine?Ui.dp(this,44):0; lp.rightMargin=mine?0:Ui.dp(this,44); messages.addView(b,lp);
    }

    private String reply(String q) {
        String l=q.toLowerCase();
        if(l.contains("nature") || l.contains("30 km")) return "Sarah semble la plus proche de ce filtre : 8 km, forte affinité mode de vie et intérêt marqué pour la montagne. Lina est également pertinente à 21 km.";
        if(l.contains("emma")) return "Avec Emma, les points les plus forts sont l’affinité visuelle personnalisée, les intentions compatibles et les centres d’intérêt liés aux animaux et aux sorties.";
        if(l.contains("message") || l.contains("parler")) return "Vous pourriez partir d’un point commun visible sur son profil. Par exemple, lui demander quelle balade ou quel voyage l’a le plus marquée récemment.";
        return "Je peux filtrer les profils selon la distance, les intentions, les centres d’intérêt et la compatibilité apprise. Dans cette version, les réponses sont simulées localement ; le prochain jalon sera de connecter le vrai moteur IA.";
    }
}
