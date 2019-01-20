package edu.msu.kyekevin.null_name;
import java.util.Arrays;
import java.util.Locale;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import android.support.annotation.NonNull;
import android.speech.tts.TextToSpeech;
import com.smartnsoft.directlinechatbot.DirectLineChatbot;

import org.jetbrains.annotations.NotNull;
import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class MainActivity extends AppCompatActivity {
    static final int r_location =1;
    LocationManager locationManager;
    private Clinic chosenClinic = null;
    private static final int Record_Limit = 100;
    private ChatView mChatView;
    private Clinic[] nearByClinics = new Clinic[3];
    private Location currentLoc = null;
    TextToSpeech t1;
    final DirectLineChatbot chatbot = new DirectLineChatbot("TbclawlN7Lk.cwA.Zvo.7LygN-lSxpQeiLhZNldc2nt8_TJ5Eq4g2k5t7ykh_Gg");
    private boolean waitingForChoice = false;
    public String thisglobalvabb = "";

    public class Clinic {
        private String provider_name;
        private String network_identifier;
        private String specialty;
        private String address;
        private String city;
        private String county;
        private String state;
        private String zip;
        private String phoneNumber;
        private String lon;
        private String lat;
        private String nearest;
        Clinic(String provider_name, String network_identifier, String specialty, String address, String city,String county,String state, String zip, String phoneNumber, String lon,
               String lat,String nearest){
            this.provider_name = provider_name;
            this.network_identifier = network_identifier;
            this.specialty=specialty;
            this.address=address;
            this.city=city;
            this.county=county;
            this.state = state;
            this.zip=zip;
            this.phoneNumber=phoneNumber;
            this.lon=lon;
            this.lat=lat;
            this.nearest= nearest+"miles";
        }
        public String getProvider_name() {
            return provider_name;
        }

        public String getLon() {
            return lon;
        }

        public String getLat() {
            return lat;
        }

        public String getNetwork_identifier() {
            return network_identifier;
        }


        public String getSpecialty() {
            return specialty;
        }



        public String getAddress() {
            return address;
        }



        public String getCity() {
            return city;
        }



        public String getCounty() {
            return county;
        }


        public String getState() {
            return state;
        }



        public String getZip() {
            return zip;
        }



        public String getPhoneNumber() {
            return phoneNumber;
        }
    }
    public static boolean isInteger(String s) {
        boolean isValidInteger = false;
        try
        {
            Integer.parseInt(s);

            // s is a valid integer

            isValidInteger = true;
        }
        catch (NumberFormatException ex)
        {
            // s is not an integer
        }

        return isValidInteger;
    }
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
            }
        });




        mChatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                if(waitingForChoice ){
                            if( isInteger(chatMessage.getMessage()) ){
                                if(Integer.parseInt(chatMessage.getMessage())>=1 && (Integer.parseInt(chatMessage.getMessage())<=3)){
                                    waitingForChoice = false;
                                    chosenClinic = nearByClinics[Integer.parseInt(chatMessage.getMessage())];
                                    double currentla = currentLoc.getLatitude();
                                    double currentlon = currentLoc.getLongitude();
                                    double destla = Double.valueOf(chosenClinic.getLat());
                                    double destlon = Double.valueOf(chosenClinic.getLon());
                                    String ChosenName = chosenClinic.getProvider_name();
                                    String chosenAdd= chosenClinic.getAddress();
                                    String chosenNum = chosenClinic.getPhoneNumber();
                                    new AlertDialog.Builder(mChatView.getContext())
                                            .setMessage("You chose "+ChosenName+"." + "Located at:" +chosenAdd+",")
                                            .setPositiveButton("Direction", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + currentla + "," +currentlon  + "&daddr=" + destla + "," + destlon));
                                                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton("Call", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String phone = chosenNum;
                                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                                    startActivity(intent);
                                                }
                                            })
                                            .show();

                                }
                    }

                }
                else{
                    chatbot.send(chatMessage.getMessage());
                }
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
                chatbot.send("welcome");
                Log.d("CHATBOT", "Started");

            }

            @Override
            public void onMessageReceived(@NotNull String message)
            {
                Log.d("CHATBOT", message);
                //mChatView.addMessage(new ChatMessage(message, System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
                String msg = message;
                final Semaphore mutex = new Semaphore(0);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String a = msg;
                        thisglobalvabb = msg;
                        if(msg.equals("You do not have a pre-set clinic, we will suggest clinics near you based on your GPS location.")){
                            getLocation();
                        }
                        else if(msg.indexOf('|')>=0){
                            //Kye, Kevin|BCC|Software Engineer|591 N Shaw Ln|East Lansing|Ingham County|MI|48825|(616) 308 6951|-84.47529109999999|42.7267794
                            a = "";
                            String line = null;
                            BufferedReader bufReader = new BufferedReader(new StringReader(msg));
                            try {
                                int cnt = 0;
                                int choice = 0;
                                while ((line = bufReader.readLine()) != null) {
                                    String[] values = line.split("\\|");
                                    nearByClinics[cnt] =new Clinic(values[0],values[1],values[2],values[3],values[4],values[5],values[6],values[7],values[8],values[9],values[10],values[11]);
                                    cnt++;
                                    choice ++;
                                    double dist = Math.round(Float.parseFloat(values[11]) * 100.0)/100.0;
                                    Log.i("zzzz",values[0]);
                                    Log.i("zzzz",Double.toString(dist));
                                    a += Integer.toString(choice)+".  Name: " + values[0] + "\nDistance: "+Double.toString(dist)+"miles\n\n";
                                }
                            }

                            catch (java.io.IOException e){
                                Log.e("NEARBY EXCEPTION","COULDNT FIND NEARBY 3 "+e.getMessage());
                            }
                            a += "Choose a clinic by number.";
                            thisglobalvabb = a;
                            waitingForChoice = true;
                        }
                        if(!msg.isEmpty()) {
                            ChatMessage chatMessage = new ChatMessage(a, System.currentTimeMillis(), ChatMessage.Type.RECEIVED);
                            mChatView.addMessage(chatMessage);
                        }
                        mutex.release();
                    }

                });

                try {
                    mutex.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        t1.speak(thisglobalvabb,TextToSpeech.QUEUE_FLUSH,null,null);
                        boolean speakingEnd = t1.isSpeaking();
                        do{
                            speakingEnd = t1.isSpeaking();
                        } while (speakingEnd);
                    }
                });
                t.start();
                try {
                    t.join(300000);
                    thisglobalvabb = "";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



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
            chatbot.send(location.getLongitude() + "|" + location.getLatitude());
            currentLoc = location;
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
                    if(waitingForChoice ){
                        if( isInteger(TextRecorded) ){
                            if(Integer.parseInt(TextRecorded)>=1 && (Integer.parseInt(TextRecorded)<=3)){
                                waitingForChoice = false;
                                chosenClinic = nearByClinics[Integer.parseInt(TextRecorded)];
                                double currentla = currentLoc.getLatitude();
                                double currentlon = currentLoc.getLongitude();
                                double destla = Double.valueOf(chosenClinic.getLat());
                                double destlon = Double.valueOf(chosenClinic.getLon());
                                String ChosenName = chosenClinic.getProvider_name();
                                String chosenAdd= chosenClinic.getAddress();
                                String chosenNum = chosenClinic.getPhoneNumber();
                                new AlertDialog.Builder(mChatView.getContext())
                                        .setMessage("You chose "+ChosenName+"." + "Located at:" +chosenAdd+",")
                                        .setPositiveButton("Direction", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + currentla + "," +currentlon  + "&daddr=" + destla + "," + destlon));
                                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton("Call", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String phone = chosenNum;
                                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                                startActivity(intent);
                                            }
                                        })
                                        .show();

                            }
                        }

                    }
                    mChatView.addMessage(new ChatMessage(TextRecorded, System.currentTimeMillis(), ChatMessage.Type.SENT));


                }
                break;
            }

        }
    }



}


