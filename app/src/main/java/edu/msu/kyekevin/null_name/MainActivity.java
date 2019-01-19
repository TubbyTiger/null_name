package edu.msu.kyekevin.null_name;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.Service;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.smartnsoft.directlinechatbot.DirectLineChatbot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity { 

    private static final int Record_Limit = 100;
    private TextView mVoiceRecorded; // what system recorded
    final DirectLineChatbot chatbot = new DirectLineChatbot("FuzJTZSqblE.cwA.D-E.aUCCKhA6bIREW1JtzhsG2AgYQvdsckg0hgJlFp4UkXU");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceRecorded = (TextView) findViewById(R.id.voiceRecorded);
        ImageButton mSpeakBtn = findViewById(R.id.btnRecord);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceRecord();
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
            }
        });
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
                    mVoiceRecorded.setText(TextRecorded);
                    chatbot.send(TextRecorded);

                }
                break;
            }

        }
    }
}