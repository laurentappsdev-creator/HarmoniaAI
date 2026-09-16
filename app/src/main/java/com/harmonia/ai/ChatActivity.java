package com.harmonia.ai;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ChatActivity extends Activity {
    private static final int SPEECH_REQUEST = 3001;

    private LinearLayout messages;
    private ScrollView scroll;
    private EditText input;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        initTextToSpeech();
        build();
    }

    private void initTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.FRENCH);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
                tts.setSpeechRate(0.95f);
            }
        });
    }

    private void build() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout head=new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        Ui.padding(head,this,18,18,18,12);

        TextView back=Ui.text(this,"‹ Retour",14,Ui.MUTED,true);
        back.setOnClickListener(v->finish());
        head.addView(back);

        TextView title=Ui.text(this,"Assistant Harmonia",22,Ui.TEXT,true);
        Ui.padding(title,this,0,8,0,2);
        head.addView(title);

        head.addView(Ui.text(this,
                "Écrivez ou touchez le micro pour parler à Harmonia.",
                12,Ui.MUTED,false));
        root.addView(head);

        scroll=new ScrollView(this);
        messages=new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        Ui.padding(messages,this,16,8,16,12);
        scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        addBubble("J’ai trouvé 4 profils très compatibles aujourd’hui. Emma partage plusieurs de vos centres d’intérêt et votre score de compatibilité atteint 91 %.",false);
        addBubble("Vous pouvez maintenant me parler : touchez 🎙, dites votre question et je vous répondrai à voix haute.",false);

        LinearLayout composer=new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        Ui.padding(composer,this,14,6,14,14);

        input=new EditText(this);
        input.setSingleLine(true);
        input.setHint("Écrivez à l’IA…");
        input.setHintTextColor(Ui.MUTED);
        input.setTextColor(Ui.TEXT);
        input.setBackground(Ui.stroke(Ui.SURFACE,Ui.SURFACE2,1,18,this));
        Ui.padding(input,this,16,0,16,0);
        composer.addView(input,new LinearLayout.LayoutParams(0,Ui.dp(this,58),1));

        Button mic=new Button(this);
        mic.setText("🎙");
        mic.setTextSize(22);
        mic.setAllCaps(false);
        mic.setContentDescription("Parler à Harmonia");
        mic.setTextColor(Ui.TEXT);
        mic.setBackground(Ui.rounded(Ui.PINK,18,this));
        mic.setOnClickListener(v->startVoiceRecognition());

        LinearLayout.LayoutParams micLp=new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,58));
        micLp.leftMargin=Ui.dp(this,10);
        composer.addView(mic,micLp);

        root.addView(composer);

        input.setOnEditorActionListener((v,action,event)->{
            sendMessage(input.getText().toString(), false);
            return true;
        });

        setContentView(root);
    }

    private void startVoiceRecognition() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.FRENCH.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,Locale.FRENCH.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Parlez à Harmonia");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);

        try {
            startActivityForResult(intent,SPEECH_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                    "La reconnaissance vocale n’est pas disponible sur ce téléphone.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode!=SPEECH_REQUEST || resultCode!=RESULT_OK || data==null) return;

        ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results==null || results.isEmpty()) return;

        String transcript=VoiceConversation.clean(results.get(0));
        if (!VoiceConversation.hasText(transcript)) return;

        input.setText(transcript);
        input.setSelection(input.length());
        sendMessage(transcript,true);
    }

    private void sendMessage(String raw, boolean fromVoice) {
        String q=VoiceConversation.clean(raw);
        if (!VoiceConversation.hasText(q)) return;

        addBubble(q,true);
        input.setText("");

        String answer=reply(q);
        addBubble(answer,false);

        scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN));

        if (fromVoice) speak(answer);
    }

    private void speak(String text) {
        if (!ttsReady || tts==null || !VoiceConversation.hasText(text)) return;
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"harmonia_reply");
    }

    private void addBubble(String text,boolean mine) {
        TextView b=Ui.text(this,text,14,Ui.TEXT,false);
        b.setLineSpacing(0,1.15f);
        Ui.padding(b,this,14,11,14,11);
        b.setBackground(Ui.rounded(mine?Ui.VIOLET:Ui.SURFACE,18,this));

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.gravity=mine?Gravity.RIGHT:Gravity.LEFT;
        lp.bottomMargin=Ui.dp(this,9);
        lp.leftMargin=mine?Ui.dp(this,44):0;
        lp.rightMargin=mine?0:Ui.dp(this,44);
        messages.addView(b,lp);
    }

    private String reply(String q) {
        String l=q.toLowerCase(Locale.FRENCH);

        if(l.contains("bonjour") || l.contains("salut"))
            return "Bonjour. Je vous écoute. Que souhaitez-vous savoir sur vos affinités ou vos matchs ?";

        if(l.contains("nature") || l.contains("30 km"))
            return "Sarah semble la plus proche de ce filtre : 8 kilomètres, forte affinité de mode de vie et intérêt marqué pour la montagne. Lina est également pertinente à 21 kilomètres.";

        if(l.contains("emma"))
            return "Avec Emma, les points les plus forts sont l’affinité visuelle personnalisée, les intentions compatibles et les centres d’intérêt liés aux animaux et aux sorties.";

        if(l.contains("message") || l.contains("parler"))
            return "Vous pourriez partir d’un point commun visible sur son profil. Par exemple, lui demander quelle balade ou quel voyage l’a le plus marquée récemment.";

        if(l.contains("qui") && l.contains("compatible"))
            return "Parmi la sélection actuelle, Emma, Lina, Chloé et Sarah ont les meilleurs scores de compatibilité. Je peux vous expliquer les différences entre leurs profils.";

        return "Je peux vous aider à filtrer les profils selon la distance, les intentions, les centres d’intérêt et la compatibilité. Dites-moi simplement ce que vous recherchez.";
    }

    @Override protected void onDestroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
