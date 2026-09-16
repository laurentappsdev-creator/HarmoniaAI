package com.harmonia.ai;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class CompatibilityActivity extends Activity {
    @Override public void onCreate(Bundle b) { super.onCreate(b); build(); }

    private void build() {
        Profile[] ps=MockData.profiles(); int idx=getIntent().getIntExtra("idx",0); if(idx<0||idx>=ps.length)idx=0; Profile p=ps[idx];
        ScrollView s=new ScrollView(this); s.setBackgroundColor(Ui.BG); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); Ui.padding(root,this,20,24,20,24); s.addView(root);
        TextView back=Ui.text(this,"‹ Retour",15,Ui.MUTED,true); back.setOnClickListener(v->finish()); root.addView(back);
        TextView t=Ui.text(this,p.name+" · "+p.score()+"%",29,Ui.TEXT,true); Ui.padding(t,this,0,22,0,4); root.addView(t);
        root.addView(Ui.text(this,MatchEngine.verdict(p.score()),15,Ui.MINT,true));
        TextView explain=Ui.text(this,"Ce score combine plusieurs dimensions. L’affinité visuelle correspond uniquement à vos préférences personnelles apprises par l’application.",14,Ui.MUTED,false); explain.setLineSpacing(0,1.2f); Ui.padding(explain,this,0,10,0,20); root.addView(explain);
        metric(root,"Affinité visuelle",p.visual,"30 % du score");
        metric(root,"Personnalité",p.personality,"25 % du score");
        metric(root,"Intentions",p.intent,"20 % du score");
        metric(root,"Mode de vie",p.lifestyle,"15 % du score");
        metric(root,"Distance",p.distance,"10 % du score");
        TextView note=Ui.text(this,"Pourquoi l’IA vous rapproche",18,Ui.TEXT,true); Ui.padding(note,this,0,18,0,8); root.addView(note);
        root.addView(Ui.text(this,"Vous partagez plusieurs centres d’intérêt, recherchez une relation compatible et votre historique de choix indique une forte affinité avec ce type de profil.",14,Ui.MUTED,false));
        setContentView(s);
    }

    private void metric(LinearLayout root,String label,int value,String weight) {
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setBackground(Ui.rounded(Ui.SURFACE,17,this)); Ui.padding(card,this,14,12,14,12);
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.addView(Ui.text(this,label,15,Ui.TEXT,true),new LinearLayout.LayoutParams(0,-2,1)); row.addView(Ui.text(this,value+"%",15,Ui.MINT,true)); card.addView(row);
        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); pb.setMax(100); pb.setProgress(value); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,Ui.dp(this,7)); pp.topMargin=Ui.dp(this,9); card.addView(pb,pp);
        TextView w=Ui.text(this,weight,11,Ui.MUTED,false); Ui.padding(w,this,0,6,0,0); card.addView(w);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=Ui.dp(this,10); root.addView(card,lp);
    }
}
