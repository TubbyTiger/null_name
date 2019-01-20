package edu.msu.kyekevin.null_name;
import java.util.Arrays;
import java.util.Locale;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.Service;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;
import android.support.annotation.NonNull;
import android.speech.tts.TextToSpeech;
import com.smartnsoft.directlinechatbot.DirectLineChatbot;

import org.jetbrains.annotations.NotNull;
import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class MainActivity extends AppCompatActivity {
    static final int r_location =1;
    LocationManager locationManager;

    private static final int Record_Limit = 100;
    private ChatView mChatView;

    TextToSpeech t1;
    final DirectLineChatbot chatbot = new DirectLineChatbot("TbclawlN7Lk.cwA.Zvo.7LygN-lSxpQeiLhZNldc2nt8_TJ5Eq4g2k5t7ykh_Gg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mChatView =  findViewById(R.id.chat_view);

        ImageButton mSpeakBtn = findViewById(R.id.btnRecord);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceRecord();//start recording
                getLocation(); //function to get lat and long
            }
        });




        mChatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                chatbot.send(chatMessage.getMessage());

                return true;
            }
        });
        mChatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {
                mSpeakBtn.setVisibility(View.GONE);
            }

            @Override
            public void userStoppedTyping() {
                mSpeakBtn.setVisibility(View.VISIBLE);
            }
        });

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }

            }
        });



        chatbot.start(new DirectLineChatbot.Callback()
        {
            @Override
            public void onStarted()
            {
                Log.d("CHATBOT", "Started");

            }

            @Override
            public void onMessageReceived(@NotNull String message)
            {
                Log.d("CHATBOT", message);
                //mChatView.addMessage(new ChatMessage(message, System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
                final String msg = message;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ChatMessage chatMessage = new ChatMessage(msg, System.currentTimeMillis(), ChatMessage.Type.RECEIVED);
                        mChatView.addMessage(chatMessage);


                    }
                });
                t1.speak(msg,TextToSpeech.QUEUE_FLUSH,null,null);
                boolean speakingEnd = t1.isSpeaking();
                do{
                    speakingEnd = t1.isSpeaking();
                } while (speakingEnd);
            }

        });


    }


    public void onDestroy(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }


    void getLocation(){
        // check if we have permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},r_location);
        }
        else{
            //ask for permission
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //location data that can be used to calculate distance
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permission,@NonNull int[]grantResults){
        super.onRequestPermissionsResult(requestCode,permission,grantResults);
        switch (requestCode){
            case r_location:
                getLocation();
                break;

        }

    }
    private void startVoiceRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "recording voice");
        try {
            startActivityForResult(intent, Record_Limit);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Record_Limit: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String TextRecorded = result.get(0); // command user input
                    chatbot.send(TextRecorded);

                    mChatView.addMessage(new ChatMessage(TextRecorded, System.currentTimeMillis(), ChatMessage.Type.SENT));


                }
                break;
            }

        }
    }


}


