package com.harmonia.ai;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class CompatibilityActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        build();
    }

    private void build() {
        Profile[] ps=MockData.profiles();
        int idx=getIntent().getIntExtra("idx",0);
        if(idx<0 || idx>=ps.length) idx=0;
        Profile p=ps[idx];

        SharedPreferences prefs=getSharedPreferences("harmonia",MODE_PRIVATE);
        SharedPreferences learning=getSharedPreferences("harmonia_learning",MODE_PRIVATE);
        UserPreferences userPrefs=UserPreferences.from(prefs);
        MatchResult result=MatchEngine.evaluate(
                p,userPrefs,PreferenceLearner.adjustment(learning,p));

        ScrollView s=new ScrollView(this);
        s.setBackgroundColor(Ui.BG);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        Ui.padding(root,this,20,24,20,24);
        s.addView(root);

        TextView back=Ui.text(this,"‹ Retour",15,Ui.MUTED,true);
        back.setOnClickListener(v->finish());
        root.addView(back);

        String title=result.eligible
                ? p.name+" · "+result.score+"%"
                : p.name+" · profil filtré";
        TextView t=Ui.text(this,title,29,Ui.TEXT,true);
        Ui.padding(t,this,0,22,0,4);
        root.addView(t);

        if(!result.eligible) {
            root.addView(Ui.text(this,"Critère éliminatoire détecté",15,Ui.PINK,true));
            TextView intro=Ui.text(this,
                    "Harmonia exclut ce profil de vos recommandations tant qu’un critère indispensable n’est pas respecté.",
                    14,Ui.MUTED,false);
            Ui.padding(intro,this,0,10,0,16);
            root.addView(intro);

            for(String blocker:result.blockers()) {
                TextView b=Ui.text(this,"• "+blocker,14,Ui.TEXT,false);
                Ui.padding(b,this,0,5,0,5);
                root.addView(b);
            }
            setContentView(s);
            return;
        }

        root.addView(Ui.text(this,MatchEngine.verdict(result.score),15,Ui.MINT,true));

        TextView explain=Ui.text(this,
                "Le score est calculé à partir de vos préférences explicites et de vos choix précédents. L’affinité visuelle reste personnelle : elle ne classe jamais les personnes selon une beauté universelle.",
                14,Ui.MUTED,false);
        explain.setLineSpacing(0,1.2f);
        Ui.padding(explain,this,0,10,0,20);
        root.addView(explain);

        metric(root,"Affinité visuelle",result.visual,"30 % du score");
        metric(root,"Personnalité",result.personality,"25 % du score");
        metric(root,"Intentions",result.intent,"20 % du score");
        metric(root,"Mode de vie & intérêts",result.lifestyle,"15 % du score");
        metric(root,"Distance",result.distance,"10 % du score");

        if(result.learnedAdjustment!=0) {
            String sign=result.learnedAdjustment>0?"+":"";
            TextView learned=Ui.text(this,
                    "Ajustement appris : "+sign+result.learnedAdjustment+" points",
                    13,result.learnedAdjustment>0?Ui.MINT:Ui.MUTED,true);
            Ui.padding(learned,this,0,4,0,12);
            root.addView(learned);
        }

        TextView note=Ui.text(this,"Pourquoi ce profil pour vous ?",18,Ui.TEXT,true);
        Ui.padding(note,this,0,18,0,8);
        root.addView(note);

        for(String reason:result.reasons()) {
            TextView line=Ui.text(this,"• "+reason,14,Ui.TEXT,false);
            line.setLineSpacing(0,1.15f);
            Ui.padding(line,this,0,5,0,5);
            root.addView(line);
        }

        setContentView(s);
    }

    private void metric(LinearLayout root,String label,int value,String weight) {
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.rounded(Ui.SURFACE,17,this));
        Ui.padding(card,this,14,12,14,12);

        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(Ui.text(this,label,15,Ui.TEXT,true),
                new LinearLayout.LayoutParams(0,-2,1));
        row.addView(Ui.text(this,value+"%",15,Ui.MINT,true));
        card.addView(row);

        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(value);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,Ui.dp(this,7));
        pp.topMargin=Ui.dp(this,9);
        card.addView(pb,pp);

        TextView w=Ui.text(this,weight,11,Ui.MUTED,false);
        Ui.padding(w,this,0,6,0,0);
        card.addView(w);

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=Ui.dp(this,10);
        root.addView(card,lp);
    }
}
