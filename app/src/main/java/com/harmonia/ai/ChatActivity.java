package com.harmonia.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
    private static final int AUDIO_PERMISSION_REQUEST = 3002;

    private LinearLayout messages;
    private ScrollView scroll;
    private EditText input;
    private TextView languageStatus;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private boolean ttsReady = false;
    private String detectedLanguageTag = Locale.getDefault().toLanguageTag();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        initTextToSpeech();
        build();
        initSpeechRecognizer();
    }

    private void initTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.getDefault());
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
                "Parlez naturellement : Harmonia détecte automatiquement la langue quand le moteur vocal du téléphone le permet.",
                12,Ui.MUTED,false));

        languageStatus=Ui.text(this,"Langue : détection automatique",12,Ui.MINT,true);
        Ui.padding(languageStatus,this,0,7,0,0);
        head.addView(languageStatus);
        root.addView(head);

        scroll=new ScrollView(this);
        messages=new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        Ui.padding(messages,this,16,8,16,12);
        scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        addBubble("Touchez 🎙 puis parlez dans la langue de votre choix.",false);

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

    private void initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            languageStatus.setText("Reconnaissance vocale indisponible");
            return;
        }

        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                languageStatus.setText("Écoute… langue automatique");
            }

            @Override public void onBeginningOfSpeech() {
                languageStatus.setText("Parlez…");
            }

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                languageStatus.setText("Analyse de la langue…");
            }

            @Override public void onError(int error) {
                languageStatus.setText("Langue : détection automatique");
                if (error != SpeechRecognizer.ERROR_NO_MATCH
                        && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    Toast.makeText(ChatActivity.this,
                            "Je n’ai pas pu comprendre. Réessayez.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onResults(Bundle results) {
                ArrayList<String> spoken=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (spoken==null || spoken.isEmpty()) return;

                String transcript=VoiceConversation.clean(spoken.get(0));
                if (!VoiceConversation.hasText(transcript)) return;

                input.setText(transcript);
                input.setSelection(input.length());
                sendMessage(transcript,true);
            }

            @Override public void onPartialResults(Bundle partialResults) {}

            @Override public void onEvent(int eventType, Bundle params) {}

            public void onLanguageDetection(Bundle results) {
                if (results==null) return;
                String tag=results.getString("detected_language");
                if (!VoiceConversation.hasText(tag)) return;

                detectedLanguageTag=tag;
                Locale locale=Locale.forLanguageTag(tag);
                String name=locale.getDisplayLanguage(locale);
                if (!VoiceConversation.hasText(name)) name=tag;
                languageStatus.setText("Langue détectée : " + name);
            }
        });
    }

    private void startVoiceRecognition() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},AUDIO_PERMISSION_REQUEST);
            return;
        }
        beginListening();
    }

    private void beginListening() {
        if (speechRecognizer==null) {
            Toast.makeText(this,"Reconnaissance vocale indisponible.",Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);

        // API 34+ : activation par clés littérales pour rester compilable avec l'ancien SDK du projet.
        intent.putExtra("android.speech.extra.ENABLE_LANGUAGE_DETECTION",true);
        intent.putExtra("android.speech.extra.ENABLE_LANGUAGE_SWITCH","balanced");

        languageStatus.setText("Écoute… langue automatique");
        speechRecognizer.startListening(intent);
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode==AUDIO_PERMISSION_REQUEST
                && grantResults.length>0
                && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            beginListening();
        } else if (requestCode==AUDIO_PERMISSION_REQUEST) {
            Toast.makeText(this,"Autorisez le microphone pour parler à Harmonia.",Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage(String raw, boolean fromVoice) {
        String q=VoiceConversation.clean(raw);
        if (!VoiceConversation.hasText(q)) return;

        addBubble(q,true);
        input.setText("");

        String language=VoiceConversation.languageCode(detectedLanguageTag);
        String answer=reply(q,language);
        addBubble(answer,false);
        scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN));

        if (fromVoice) speak(answer);
    }

    private void speak(String text) {
        if (!ttsReady || tts==null || !VoiceConversation.hasText(text)) return;

        Locale spokenLocale=Locale.forLanguageTag(detectedLanguageTag);
        int result=tts.setLanguage(spokenLocale);
        if (result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.getDefault());
        }
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"harmonia_reply");
    }

    private String reply(String q,String language) {
        String l=q.toLowerCase(Locale.ROOT);

        if ("en".equals(language)) {
            if (l.contains("emma")) return "With Emma, your strongest points are personalized visual affinity, compatible intentions, and shared interests.";
            return "I can help you filter profiles by distance, intentions, interests and compatibility. Tell me what you are looking for.";
        }

        if ("de".equals(language)) {
            if (l.contains("emma")) return "Bei Emma sind eure stärksten Punkte die persönliche visuelle Affinität, passende Absichten und gemeinsame Interessen.";
            return "Ich kann Profile nach Entfernung, Absichten, Interessen und Kompatibilität filtern. Sag mir einfach, wonach du suchst.";
        }

        if ("es".equals(language)) {
            if (l.contains("emma")) return "Con Emma, los puntos más fuertes son la afinidad visual personalizada, las intenciones compatibles y los intereses comunes.";
            return "Puedo ayudarte a filtrar perfiles por distancia, intenciones, intereses y compatibilidad. Dime qué estás buscando.";
        }

        if ("it".equals(language)) {
            if (l.contains("emma")) return "Con Emma, i punti più forti sono l’affinità visiva personalizzata, le intenzioni compatibili e gli interessi comuni.";
            return "Posso aiutarti a filtrare i profili per distanza, intenzioni, interessi e compatibilità. Dimmi cosa stai cercando.";
        }

        if(l.contains("emma"))
            return "Avec Emma, les points les plus forts sont l’affinité visuelle personnalisée, les intentions compatibles et les centres d’intérêt communs.";

        return "Je peux vous aider à filtrer les profils selon la distance, les intentions, les centres d’intérêt et la compatibilité. Dites-moi simplement ce que vous recherchez.";
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

    @Override protected void onDestroy() {
        if (speechRecognizer!=null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
