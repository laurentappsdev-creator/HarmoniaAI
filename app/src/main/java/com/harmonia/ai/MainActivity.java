package com.harmonia.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private LinearLayout content;
    private Profile[] profiles;
    private int index = 0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences p = getSharedPreferences("harmonia", MODE_PRIVATE);
        if (!p.getBoolean("onboarded", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        profiles = MockData.profiles();
        buildShell();
        showDiscover();
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        Ui.padding(header, this, 20, 14, 20, 10);
        TextView logo = Ui.text(this, "Harmonia", 24, Ui.TEXT, true);
        TextView ai = Ui.text(this, "  AI", 12, Ui.PINK, true);
        header.addView(logo);
        header.addView(ai);
        root.addView(header, new LinearLayout.LayoutParams(-1, Ui.dp(this, 64)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        Ui.padding(content, this, 18, 10, 18, 14);
        scroll.addView(content, new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Ui.SURFACE);
        String[] names = {"Découvrir", "Matchs", "IA", "Profil"};
        for (int i=0;i<names.length;i++) {
            final int tab = i;
            Button b = new Button(this);
            b.setText(names[i]);
            b.setTextColor(i==0 ? Ui.PINK : Ui.MUTED);
            b.setTextSize(12);
            b.setAllCaps(false);
            b.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            b.setOnClickListener(v -> {
                if (tab==0) showDiscover();
                else if (tab==1) showMatches();
                else if (tab==2) startActivity(new Intent(this, ChatActivity.class));
                else showProfile();
            });
            nav.addView(b, new LinearLayout.LayoutParams(0, Ui.dp(this,64),1));
        }
        root.addView(nav);
        setContentView(root);
    }

    private void clear() { content.removeAllViews(); }

    private void showDiscover() {
        clear();
        Profile p = profiles[index % profiles.length];

        TextView title = Ui.text(this, "Sélection IA du jour", 22, Ui.TEXT, true);
        content.addView(title);
        TextView sub = Ui.text(this, "Des profils choisis selon vos préférences et votre compatibilité.", 14, Ui.MUTED, false);
        Ui.padding(sub,this,0,5,0,16);
        content.addView(sub);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.rounded(Ui.SURFACE, 24, this));
        Ui.padding(card,this,18,18,18,18);

        TextView avatar = Ui.text(this, p.name.substring(0,1), 54, Ui.TEXT, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(Ui.rounded(Ui.VIOLET, 24, this));
        card.addView(avatar, new LinearLayout.LayoutParams(-1, Ui.dp(this,260)));

        TextView name = Ui.text(this, p.name + ", " + p.age, 27, Ui.TEXT, true);
        Ui.padding(name,this,0,16,0,2);
        card.addView(name);
        card.addView(Ui.text(this, p.city, 14, Ui.MUTED, false));

        TextView score = Ui.text(this, p.score() + "% compatible · " + MatchEngine.verdict(p.score()), 15, Ui.MINT, true);
        Ui.padding(score,this,0,12,0,12);
        card.addView(score);

        TextView bio = Ui.text(this, p.bio, 15, Ui.TEXT, false);
        bio.setLineSpacing(0,1.15f);
        card.addView(bio);

        TextView interests = Ui.text(this, joinInterests(p.interests), 14, Ui.MUTED, false);
        Ui.padding(interests,this,0,14,0,12);
        card.addView(interests);

        Button detail = new Button(this);
        detail.setText("Pourquoi ce profil ?");
        detail.setAllCaps(false);
        detail.setTextColor(Ui.TEXT);
        detail.setBackground(Ui.stroke(Ui.SURFACE2, Ui.VIOLET, 1, 18, this));
        detail.setOnClickListener(v -> {
            Intent i = new Intent(this, CompatibilityActivity.class);
            i.putExtra("idx", index % profiles.length);
            startActivity(i);
        });
        card.addView(detail, new LinearLayout.LayoutParams(-1, Ui.dp(this,52)));
        content.addView(card);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        Ui.padding(actions,this,0,16,0,0);

        Button pass = actionButton("Passer", Ui.SURFACE2);
        pass.setOnClickListener(v -> nextProfile());
        Button like = actionButton("♥ J’aime", Ui.PINK);
        like.setOnClickListener(v -> likeProfile(p));
        actions.addView(pass, new LinearLayout.LayoutParams(0,Ui.dp(this,58),1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,Ui.dp(this,58),1);
        lp.leftMargin = Ui.dp(this,12);
        actions.addView(like, lp);
        content.addView(actions);
    }

    private String joinInterests(String[] a) {
        StringBuilder s = new StringBuilder();
        for (int i=0;i<a.length;i++) {
            if (i>0) s.append("   •   ");
            s.append(a[i]);
        }
        return s.toString();
    }

    private Button actionButton(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTextColor(Ui.TEXT);
        b.setBackground(Ui.rounded(color,20,this));
        return b;
    }

    private void nextProfile() { index = (index + 1) % profiles.length; showDiscover(); }

    private void likeProfile(Profile p) {
        new AlertDialog.Builder(this)
                .setTitle("Like enregistré")
                .setMessage("Votre préférence aide Harmonia AI à mieux comprendre les profils qui vous correspondent.\n\nDémo : " + p.name + " a été ajoutée à vos likes.")
                .setPositiveButton("Continuer", (d,w) -> nextProfile())
                .setNeutralButton("Ouvrir l’assistant", (d,w) -> startActivity(new Intent(this, ChatActivity.class)))
                .show();
    }

    private void showMatches() {
        clear();
        content.addView(Ui.text(this,"Vos meilleurs matchs",22,Ui.TEXT,true));
        TextView t = Ui.text(this,"Les profils ci-dessous dépassent 85 % de compatibilité IA.",14,Ui.MUTED,false);
        Ui.padding(t,this,0,5,0,14); content.addView(t);
        for (int i=0;i<profiles.length;i++) {
            Profile p = profiles[i];
            if (p.score()<85) continue;
            LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.rounded(Ui.SURFACE,18,this)); Ui.padding(row,this,14,14,14,14);
            TextView av = Ui.text(this,p.name.substring(0,1),22,Ui.TEXT,true); av.setGravity(Gravity.CENTER);
            av.setBackground(Ui.rounded(Ui.VIOLET,30,this)); row.addView(av,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));
            LinearLayout texts = new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL); Ui.padding(texts,this,12,0,0,0);
            texts.addView(Ui.text(this,p.name+", "+p.age,17,Ui.TEXT,true));
            texts.addView(Ui.text(this,p.score()+"% · "+p.city,13,Ui.MINT,false));
            row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
            final int pos=i; row.setOnClickListener(v->{ Intent in=new Intent(this,CompatibilityActivity.class); in.putExtra("idx",pos); startActivity(in);});
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1,-2); rp.bottomMargin=Ui.dp(this,10); content.addView(row,rp);
        }
    }

    private void showProfile() {
        clear();
        content.addView(Ui.text(this,"Votre profil IA",22,Ui.TEXT,true));
        TextView intro=Ui.text(this,"Harmonia apprend vos préférences progressivement. Vous restez maître des critères utilisés.",14,Ui.MUTED,false);
        Ui.padding(intro,this,0,6,0,18); content.addView(intro);
        infoCard("Préférences visuelles", "Actives · personnalisées à partir de vos likes et passes");
        infoCard("Relation recherchée", "Sérieuse");
        infoCard("Distance maximale", "50 km");
        infoCard("Centres d’intérêt", "Nature · sorties · animaux · voyages");
        infoCard("Données sensibles", "Non analysées : origine, santé, religion et autres attributs sensibles");
    }

    private void infoCard(String title,String text) {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setBackground(Ui.rounded(Ui.SURFACE,18,this)); Ui.padding(c,this,15,13,15,13);
        c.addView(Ui.text(this,title,15,Ui.TEXT,true));
        TextView v=Ui.text(this,text,13,Ui.MUTED,false); Ui.padding(v,this,0,4,0,0); c.addView(v);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=Ui.dp(this,10); content.addView(c,lp);
    }
}
