package com.harmonia.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int PHOTO_REQUEST = 2001;

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

        int eligibleIndex=findEligibleIndex(index);
        if(eligibleIndex<0) {
            content.addView(Ui.text(this,"Aucun profil compatible pour le moment",22,Ui.TEXT,true));
            TextView empty=Ui.text(this,
                    "Tous les profils disponibles sont exclus par vos critères indispensables. Modifiez vos préférences pour élargir la recherche.",
                    14,Ui.MUTED,false);
            Ui.padding(empty,this,0,8,0,0);
            content.addView(empty);
            return;
        }

        index=eligibleIndex;
        Profile p=profiles[index];
        MatchResult result=resultFor(p);

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

        TextView score = Ui.text(this, result.score + "% compatible · " + MatchEngine.verdict(result.score), 15, Ui.MINT, true);
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
            i.putExtra("idx", index);
            startActivity(i);
        });
        card.addView(detail, new LinearLayout.LayoutParams(-1, Ui.dp(this,52)));
        content.addView(card);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        Ui.padding(actions,this,0,16,0,0);

        Button pass = actionButton("Passer", Ui.SURFACE2);
        pass.setOnClickListener(v -> passProfile(p));
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

    private void nextProfile() {
        index=(index+1)%profiles.length;
        showDiscover();
    }

    private void passProfile(Profile p) {
        PreferenceLearner.record(
                getSharedPreferences("harmonia_learning",MODE_PRIVATE),p,false);
        nextProfile();
    }

    private void likeProfile(Profile p) {
        PreferenceLearner.record(
                getSharedPreferences("harmonia_learning",MODE_PRIVATE),p,true);
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
        Ui.padding(t,this,0,5,0,14);
        content.addView(t);

        int[] order=new int[profiles.length];
        for(int i=0;i<profiles.length;i++) order[i]=i;

        for(int i=0;i<order.length;i++) {
            for(int j=i+1;j<order.length;j++) {
                MatchResult a=resultFor(profiles[order[i]]);
                MatchResult b=resultFor(profiles[order[j]]);
                int as=a.eligible?a.score:-1;
                int bs=b.eligible?b.score:-1;
                if(bs>as) {
                    int tmp=order[i];
                    order[i]=order[j];
                    order[j]=tmp;
                }
            }
        }

        for(int k=0;k<order.length;k++) {
            final int pos=order[k];
            Profile p=profiles[pos];
            MatchResult result=resultFor(p);
            if(!result.eligible || result.score<70) continue;

            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.rounded(Ui.SURFACE,18,this));
            Ui.padding(row,this,14,14,14,14);

            TextView av=Ui.text(this,p.name.substring(0,1),22,Ui.TEXT,true);
            av.setGravity(Gravity.CENTER);
            av.setBackground(Ui.rounded(Ui.VIOLET,30,this));
            row.addView(av,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));

            LinearLayout texts=new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            Ui.padding(texts,this,12,0,0,0);
            texts.addView(Ui.text(this,p.name+", "+p.age,17,Ui.TEXT,true));
            texts.addView(Ui.text(this,result.score+"% · "+p.city,13,Ui.MINT,false));
            if(!result.reasons().isEmpty()) {
                TextView reason=Ui.text(this,result.reasons().get(0),12,Ui.MUTED,false);
                Ui.padding(reason,this,0,3,0,0);
                texts.addView(reason);
            }
            row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));

            row.setOnClickListener(v->{
                Intent in=new Intent(this,CompatibilityActivity.class);
                in.putExtra("idx",pos);
                startActivity(in);
            });

            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);
            rp.bottomMargin=Ui.dp(this,10);
            content.addView(row,rp);
        }
    }

    private void showProfile() {
        clear();

        SharedPreferences prefs=getSharedPreferences("harmonia",MODE_PRIVATE);
        String name=PhotoUriState.clean(prefs.getString("name",""));
        String photoUri=PhotoUriState.clean(prefs.getString("profile_photo_uri",""));

        content.addView(Ui.text(this,"Votre profil",22,Ui.TEXT,true));

        TextView intro=Ui.text(this,"Ajoutez une photo claire de vous. Vous pourrez la remplacer à tout moment.",14,Ui.MUTED,false);
        Ui.padding(intro,this,0,6,0,16);
        content.addView(intro);

        ImageView photo=new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setBackground(Ui.rounded(Ui.SURFACE2,24,this));
        photo.setClipToOutline(true);

        if (PhotoUriState.hasPhoto(photoUri)) {
            try {
                photo.setImageURI(Uri.parse(photoUri));
            } catch (Exception ignored) {
                photo.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            photo.setImageResource(android.R.drawable.ic_menu_camera);
        }
        content.addView(photo,new LinearLayout.LayoutParams(-1,Ui.dp(this,280)));

        if (PhotoUriState.hasPhoto(name)) {
            TextView displayName=Ui.text(this,name,20,Ui.TEXT,true);
            displayName.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.padding(displayName,this,0,12,0,0);
            content.addView(displayName);
        }

        Button changePhoto=new Button(this);
        changePhoto.setText(PhotoUriState.hasPhoto(photoUri) ? "Changer ma photo" : "Ajouter une photo");
        changePhoto.setAllCaps(false);
        changePhoto.setTextSize(15);
        changePhoto.setTextColor(Ui.TEXT);
        changePhoto.setBackground(Ui.rounded(Ui.PINK,18,this));
        changePhoto.setOnClickListener(v->openPhotoPicker());

        LinearLayout.LayoutParams buttonLp=new LinearLayout.LayoutParams(-1,Ui.dp(this,54));
        buttonLp.topMargin=Ui.dp(this,12);
        buttonLp.bottomMargin=Ui.dp(this,18);
        content.addView(changePhoto,buttonLp);

        TextView aiTitle=Ui.text(this,"Profil IA",19,Ui.TEXT,true);
        Ui.padding(aiTitle,this,0,2,0,10);
        content.addView(aiTitle);

        UserPreferences up=currentPreferences();
        infoCard("Préférences visuelles","Personnalisées · ajustées progressivement à partir de vos likes et passes");
        infoCard("Relation recherchée",up.relationshipIntent.isEmpty()?"Non définie":"Sérieuse");
        infoCard("Âge recherché",up.minAge+" à "+up.maxAge+" ans");
        infoCard("Distance maximale",up.maxDistanceKm+" km");
        infoCard("Critères éliminatoires",up.rejectSmokers?"Distance · âge · intention · non-fumeur":"Distance · âge · intention");
        infoCard("Apprentissage IA","Vos likes et passes peuvent ajuster le score de ±6 points maximum");
        infoCard("Données sensibles","Non analysées : origine, santé, religion et autres attributs sensibles");
    }

    private void openPhotoPicker() {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent,PHOTO_REQUEST);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode!=PHOTO_REQUEST || resultCode!=RESULT_OK || data==null || data.getData()==null) return;

        Uri uri=data.getData();
        int flags=data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(uri,flags);
        } catch (SecurityException ignored) {}

        String saved=PhotoUriState.clean(uri.toString());
        getSharedPreferences("harmonia",MODE_PRIVATE).edit().putString("profile_photo_uri",saved).apply();
        showProfile();
    }


    private UserPreferences currentPreferences() {
        return UserPreferences.from(getSharedPreferences("harmonia",MODE_PRIVATE));
    }

    private MatchResult resultFor(Profile p) {
        SharedPreferences learning=getSharedPreferences("harmonia_learning",MODE_PRIVATE);
        return MatchEngine.evaluate(
                p,currentPreferences(),PreferenceLearner.adjustment(learning,p));
    }

    private int findEligibleIndex(int start) {
        if(profiles==null || profiles.length==0) return -1;
        for(int offset=0;offset<profiles.length;offset++) {
            int candidate=(start+offset)%profiles.length;
            if(resultFor(profiles[candidate]).eligible) return candidate;
        }
        return -1;
    }

    private void infoCard(String title,String text) {
        LinearLayout c=new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(Ui.rounded(Ui.SURFACE,18,this));
        Ui.padding(c,this,15,13,15,13);
        c.addView(Ui.text(this,title,15,Ui.TEXT,true));

        TextView v=Ui.text(this,text,13,Ui.MUTED,false);
        Ui.padding(v,this,0,4,0,0);
        c.addView(v);

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=Ui.dp(this,10);
        content.addView(c,lp);
    }
}
