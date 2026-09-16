package com.harmonia.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;\nimport android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;\nimport android.speech.tts.Voice;
import android.view.Gravity;\nimport android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;\nimport android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;\nimport java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends Activity {
    private static final int AUDIO_PERMISSION_REQUEST = 3002;
    private static final String AI_ENDPOINT =
            "https://yvjonczwlaxoxmkwneon.supabase.co/functions/v1/harmonia-chat";
    private static final String SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl2am9uY3p3bGF4b3hta3duZW9uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODk1Mzk4NDYsImV4cCI6MjEwNTExNTg0Nn0.qkPofmY7pL8CHmrCqOeAX1UdSEO5UpYVOteb5XGH3kk";

    private LinearLayout messages;
    private ScrollView scroll;
    private EditText input;
    private Button mic;
    private TextView languageStatus;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private boolean ttsReady = false;
    private boolean requestInFlight = false;
    private String detectedLanguageTag = Locale.getDefault().toLanguageTag();

    private final ConversationHistory history = new ConversationHistory(12);
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        voicePrefs=getSharedPreferences("harmonia_voice",MODE_PRIVATE);
        VoiceSettings.Preset sensual=VoiceSettings.sensual();
        voiceVolume=voicePrefs.getFloat("volume",sensual.volume);
        voiceRate=voicePrefs.getFloat("rate",sensual.rate);
        voicePitch=voicePrefs.getFloat("pitch",sensual.pitch);
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
                chooseBestVoice(Locale.getDefault());
                applyVoiceSettings();
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
                "Harmonia utilise maintenant une vraie IA conversationnelle et garde le contexte des derniers messages.",
                12,Ui.MUTED,false));

        languageStatus=Ui.text(this,"Langue : détection automatique",12,Ui.MINT,true);
        Ui.padding(languageStatus,this,0,7,0,0);
        head.addView(languageStatus);

        Button voiceControls=new Button(this);
        voiceControls.setText("🎚  Voix et volume");
        voiceControls.setAllCaps(false);
        voiceControls.setTextColor(Ui.TEXT);
        voiceControls.setTextSize(13);
        voiceControls.setBackground(Ui.rounded(Ui.SURFACE2,16,this));
        LinearLayout.LayoutParams voiceButtonLp=new LinearLayout.LayoutParams(-1,Ui.dp(this,46));
        voiceButtonLp.topMargin=Ui.dp(this,10);
        head.addView(voiceControls,voiceButtonLp);

        voicePanel=createVoicePanel();
        voicePanel.setVisibility(View.GONE);
        LinearLayout.LayoutParams voicePanelLp=new LinearLayout.LayoutParams(-1,-2);
        voicePanelLp.topMargin=Ui.dp(this,8);
        head.addView(voicePanel,voicePanelLp);

        voiceControls.setOnClickListener(v ->
                voicePanel.setVisibility(voicePanel.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE));

        root.addView(head);

        scroll=new ScrollView(this);
        messages=new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        Ui.padding(messages,this,16,8,16,12);
        scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        addBubble("Touchez 🎙 ou écrivez votre message. Je garderai le fil de la conversation.",false);

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

        mic=new Button(this);
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
        if (requestInFlight) return;

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
        if (!VoiceConversation.hasText(q) || requestInFlight) return;

        requestInFlight=true;
        setComposerEnabled(false);

        addBubble(q,true);
        history.add("user",q);
        input.setText("");

        TextView thinking=addBubble("Harmonia réfléchit…",false);
        scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN));

        networkExecutor.execute(() -> requestAiReply(fromVoice,thinking));
    }

    private void requestAiReply(boolean fromVoice,TextView thinking) {
        String reply=null;
        String error=null;

        HttpURLConnection connection=null;
        try {
            JSONObject payload=new JSONObject();
            payload.put("language",detectedLanguageTag);

            JSONArray historyJson=new JSONArray();
            for (ConversationHistory.Message item : history.all()) {
                JSONObject message=new JSONObject();
                message.put("role",item.role);
                message.put("content",item.content);
                historyJson.put(message);
            }
            payload.put("messages",historyJson);

            URL url=new URL(AI_ENDPOINT);
            connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(45000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
            connection.setRequestProperty("apikey",SUPABASE_ANON_KEY);
            connection.setRequestProperty("Authorization","Bearer " + SUPABASE_ANON_KEY);

            byte[] body=payload.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try(OutputStream out=connection.getOutputStream()) {
                out.write(body);
            }

            int status=connection.getResponseCode();
            InputStream stream=status>=200 && status<300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String response=readStream(stream);

            if (status>=200 && status<300) {
                JSONObject json=new JSONObject(response);
                reply=VoiceConversation.clean(json.optString("reply",""));
                if (!VoiceConversation.hasText(reply)) {
                    error="Réponse IA vide.";
                }
            } else {
                String serverError="";
                String serverMessage="";
                try {
                    JSONObject serverJson=new JSONObject(response);
                    serverError=serverJson.optString("error","");
                    serverMessage=VoiceConversation.clean(serverJson.optString("message",""));
                } catch(Exception ignored) {}
                if ("ai_not_configured".equals(serverError)) {
                    error="Le serveur Harmonia est prêt, mais la clé Gemini doit encore être ajoutée dans Supabase.";
                } else if (VoiceConversation.hasText(serverMessage)) {
                    error=serverMessage;
                } else {
                    error="Impossible de joindre l’IA pour le moment. Réessayez.";
                }
            }
        } catch(Exception e) {
            error="Connexion à l’IA impossible. Vérifiez votre connexion Internet puis réessayez.";
        } finally {
            if (connection!=null) connection.disconnect();
        }

        final String finalReply=reply;
        final String finalError=error;

        mainHandler.post(() -> {
            messages.removeView(thinking);

            if (VoiceConversation.hasText(finalReply)) {
                history.add("assistant",finalReply);
                addBubble(finalReply,false);
                if (fromVoice) speak(finalReply);
            } else {
                addBubble(finalError==null?"Une erreur est survenue.":finalError,false);
            }

            requestInFlight=false;
            setComposerEnabled(true);
            scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream==null) return "";
        StringBuilder out=new StringBuilder();
        try(BufferedReader reader=new BufferedReader(
                new InputStreamReader(stream,StandardCharsets.UTF_8))) {
            String line;
            while((line=reader.readLine())!=null) out.append(line);
        }
        return out.toString();
    }

    private void setComposerEnabled(boolean enabled) {
        input.setEnabled(enabled);
        mic.setEnabled(enabled);
    }

    private void speak(String text) {
        if (!ttsReady || tts==null || !VoiceConversation.hasText(text)) return;

        Locale spokenLocale=Locale.forLanguageTag(detectedLanguageTag);
        int result=tts.setLanguage(spokenLocale);
        if (result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED) {
            spokenLocale=Locale.getDefault();
            tts.setLanguage(spokenLocale);
        }

        chooseBestVoice(spokenLocale);
        applyVoiceSettings();

        Bundle params=new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,VoiceSettings.clampVolume(voiceVolume));
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,params,"harmonia_reply");
    }

    private LinearLayout createVoicePanel() {
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(Ui.rounded(Ui.SURFACE,18,this));
        Ui.padding(panel,this,14,12,14,12);

        panel.addView(Ui.text(this,"Style de voix",13,Ui.TEXT,true));

        LinearLayout presets=new LinearLayout(this);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams presetsLp=new LinearLayout.LayoutParams(-1,-2);
        presetsLp.topMargin=Ui.dp(this,7);
        panel.addView(presets,presetsLp);

        addPresetButton(presets,"Naturelle",VoiceSettings.natural());
        addPresetButton(presets,"Douce",VoiceSettings.soft());
        addPresetButton(presets,"Sensuelle",VoiceSettings.sensual());

        voiceValues=Ui.text(this,"",12,Ui.MUTED,false);
        Ui.padding(voiceValues,this,0,10,0,2);
        panel.addView(voiceValues);

        panel.addView(Ui.text(this,"Volume",12,Ui.TEXT,true));
        volumeSeek=new SeekBar(this);
        volumeSeek.setMax(100);
        volumeSeek.setProgress(VoiceSettings.volumeToProgress(voiceVolume));
        panel.addView(volumeSeek,new LinearLayout.LayoutParams(-1,Ui.dp(this,38)));

        panel.addView(Ui.text(this,"Vitesse",12,Ui.TEXT,true));
        rateSeek=new SeekBar(this);
        rateSeek.setMax(100);
        rateSeek.setProgress(VoiceSettings.rateToProgress(voiceRate));
        panel.addView(rateSeek,new LinearLayout.LayoutParams(-1,Ui.dp(this,38)));

        panel.addView(Ui.text(this,"Ton",12,Ui.TEXT,true));
        pitchSeek=new SeekBar(this);
        pitchSeek.setMax(100);
        pitchSeek.setProgress(VoiceSettings.pitchToProgress(voicePitch));
        panel.addView(pitchSeek,new LinearLayout.LayoutParams(-1,Ui.dp(this,38)));

        SeekBar.OnSeekBarChangeListener listener=new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                if (volumeSeek==null || rateSeek==null || pitchSeek==null) return;
                voiceVolume=VoiceSettings.progressToVolume(volumeSeek.getProgress());
                voiceRate=VoiceSettings.progressToRate(rateSeek.getProgress());
                voicePitch=VoiceSettings.progressToPitch(pitchSeek.getProgress());
                applyVoiceSettings();
                updateVoiceValues();
                if (fromUser) saveVoiceSettings();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        volumeSeek.setOnSeekBarChangeListener(listener);
        rateSeek.setOnSeekBarChangeListener(listener);
        pitchSeek.setOnSeekBarChangeListener(listener);

        Button preview=new Button(this);
        preview.setText("▶  Tester la voix");
        preview.setAllCaps(false);
        preview.setTextColor(Ui.TEXT);
        preview.setBackground(Ui.rounded(Ui.VIOLET,14,this));
        preview.setOnClickListener(v ->
                speak("Bonjour, je suis Harmonia. Vous pouvez régler ma voix exactement comme vous le souhaitez."));
        LinearLayout.LayoutParams previewLp=new LinearLayout.LayoutParams(-1,Ui.dp(this,44));
        previewLp.topMargin=Ui.dp(this,8);
        panel.addView(preview,previewLp);

        updateVoiceValues();
        return panel;
    }

    private void addPresetButton(LinearLayout row,String label,VoiceSettings.Preset preset) {
        Button button=new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setTextColor(Ui.TEXT);
        button.setBackground(Ui.rounded(Ui.SURFACE2,14,this));
        button.setOnClickListener(v->applyPreset(preset));

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,Ui.dp(this,42),1f);
        lp.rightMargin=Ui.dp(this,5);
        row.addView(button,lp);
    }

    private void applyPreset(VoiceSettings.Preset preset) {
        voiceVolume=preset.volume;
        voiceRate=preset.rate;
        voicePitch=preset.pitch;

        if (volumeSeek!=null) volumeSeek.setProgress(VoiceSettings.volumeToProgress(voiceVolume));
        if (rateSeek!=null) rateSeek.setProgress(VoiceSettings.rateToProgress(voiceRate));
        if (pitchSeek!=null) pitchSeek.setProgress(VoiceSettings.pitchToProgress(voicePitch));

        applyVoiceSettings();
        updateVoiceValues();
        saveVoiceSettings();
    }

    private void applyVoiceSettings() {
        if (tts==null) return;
        tts.setSpeechRate(VoiceSettings.clampRate(voiceRate));
        tts.setPitch(VoiceSettings.clampPitch(voicePitch));
    }

    private void saveVoiceSettings() {
        if (voicePrefs==null) return;
        voicePrefs.edit()
                .putFloat("volume",VoiceSettings.clampVolume(voiceVolume))
                .putFloat("rate",VoiceSettings.clampRate(voiceRate))
                .putFloat("pitch",VoiceSettings.clampPitch(voicePitch))
                .apply();
    }

    private void updateVoiceValues() {
        if (voiceValues==null) return;
        voiceValues.setText(
                "Volume " + Math.round(voiceVolume*100f) + "%  ·  Vitesse "
                        + String.format(Locale.ROOT,"%.2fx",voiceRate)
                        + "  ·  Ton " + String.format(Locale.ROOT,"%.2fx",voicePitch));
    }

    private void chooseBestVoice(Locale locale) {
        if (tts==null || locale==null) return;
        try {
            Set<Voice> voices=tts.getVoices();
            if (voices==null || voices.isEmpty()) return;

            Voice best=null;
            int bestScore=Integer.MIN_VALUE;
            String language=locale.getLanguage();

            for (Voice voice : voices) {
                if (voice==null || voice.getLocale()==null) continue;
                if (!language.equalsIgnoreCase(voice.getLocale().getLanguage())) continue;

                int score=voice.getQuality()*10-voice.getLatency();
                String name=voice.getName()==null?"":voice.getName().toLowerCase(Locale.ROOT);
                if (name.contains("natural") || name.contains("neural")) score+=1500;
                if (name.contains("female") || name.contains("woman") || name.contains("femme")) score+=700;
                if (voice.isNetworkConnectionRequired()) score+=100;

                if (score>bestScore) {
                    bestScore=score;
                    best=voice;
                }
            }

            if (best!=null) tts.setVoice(best);
        } catch(Exception ignored) {}
    }

    private TextView addBubble(String text,boolean mine) {
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
        return b;
    }

    @Override protected void onDestroy() {
        networkExecutor.shutdownNow();
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
