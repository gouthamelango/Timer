package com.example.timer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class MainActivity extends AppCompatActivity {

    int myProgress = 0;
    ProgressBar progressBarView;
    Button btn_start;
    TextView tv_time;
    static public EditText et_timer;
    static int progress;
    CountDownTimer countDownTimer;
    int endTime = 250;

    ImageView pauseBtn;
    Boolean isStop = false;
    Boolean isPausable = false;
    Boolean isRunning  = false;
    ImageView reset;

    String timeRemaining = "";

    NotificationManager manager;
    public static final String CHANNEL_1_ID = "channel1";
    public static final String KEY_REPLY = "NotificationReply";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declaration
        reset = (ImageView)findViewById(R.id.reset);
        progressBarView = (ProgressBar)findViewById(R.id.view_progress_bar);

        tv_time= (TextView)findViewById(R.id.timerRunning);
        et_timer = (EditText)findViewById(R.id.userEdit);


        /*Animation*/
        RotateAnimation makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setProgress(100);

        //Button To Start
        btn_start = (Button)findViewById(R.id.btn);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
            @Override
            public void onClick(View v) {
                startTimer(et_timer.getText().toString());

            }
        });

        //Reset Button
        reset = (ImageView)findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCountDown();
            }
        });

        //Plus Button;
        pauseBtn = (ImageView)findViewById(R.id.plusTimer);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
            }
        });

        //Notification
        createNotificationChannels();

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //
        // Toast.makeText(this,"Hey",Toast.LENGTH_SHORT).show();
        processInlineReply(intent);

    }
    public void startTimer(String time){
        if(!isRunning){
            isPausable = true;
            myProgress = 0;
            timeRemaining = time;
            fn_countdown();
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Timer Already Running",Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    private void processInlineReply(Intent intent) {

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
       // Toast.makeText(this,remoteInput.getCharSequence(KEY_REPLY).toString(),Toast.LENGTH_SHORT).show();
        if (remoteInput != null) {
            String reply = remoteInput.getCharSequence(
                    KEY_REPLY).toString();

            //Set the inline reply text in the TextView
           Toast.makeText(this,reply,Toast.LENGTH_LONG).show();
           resetCountDown();
           et_timer.setText(reply);
           isRunning = false;
          // startTimer(et_timer.getText().toString());



            //Update the notification to show that the reply was received.
            NotificationCompat.Builder repliedNotification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(
                            android.R.drawable.stat_notify_chat)
                    .setContentText("Timer has been updated");

            NotificationManager notificationManager =
                    (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1,
                    repliedNotification.build());

        }

    }
    public void sendNotification(){
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setContentTitle("Timer Has been set")
                .setContentText("We'll Notify after "+et_timer.getText().toString()+" Seconds")
                .setSmallIcon(R.drawable.ic_baseline_timer_24)

                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        String replyLabel = "Enter your reply here";
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY)
                .setLabel(replyLabel)
                .build();

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Notification Action with RemoteInput instance added.
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, "Reset Timer", resultPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();
        notifyBuilder.addAction(replyAction);
        manager.notify(1, notifyBuilder.build());
    }
    public void timeUpNotification(){
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setContentTitle("Time Up")
                .setContentText(et_timer.getText().toString()+" Seconds Over")
                .setSmallIcon(R.drawable.ic_baseline_timer_off_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        manager.notify(1, notifyBuilder.build());
    }





    public void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");
            channel1.enableVibration(true);
            channel1.getSound();
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }


    public  void  pauseTimer(){

      if(isPausable){
          if(isRunning){
              isStop = true;
              isRunning = false;
              pauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
          }
          else {
              isStop = false;
              isRunning = true;
              pauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
              fn_countdown();
          }

      }
      else {
          Toast.makeText(this,"Timer is not Running",Toast.LENGTH_LONG).show();
      }


    }

    public  void resetCountDown(){
        isStop=true;
        isRunning = false;
        tv_time.setText("00:00:00");
        isPausable = false;
        progressBarView.setProgress(100);
        pauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);

    }
    public void fn_countdown() {

        if (et_timer.getText().toString().length()>0) {

            isStop = false;
            isRunning = true;
            sendNotification();

            String timeInterval = timeRemaining;
            progress = 1;
            endTime = Integer.parseInt(timeInterval); // up to finish time

            countDownTimer = new CountDownTimer(endTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(isStop){
                        cancel();
                        isRunning = false;


                    }
                    else {
                        setProgress(progress, endTime);
                        progress = progress + 1;
                        int seconds = (int) (millisUntilFinished / 1000) % 60;
                        int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                        int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                        String newtime = hours + ":" + minutes + ":" + seconds;
                        timeRemaining = String.valueOf(millisUntilFinished / 1000);
                        myProgress = progress;
                        if (newtime.equals("0:0:0")) {
                            tv_time.setText("00:00:00");
                        } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                            tv_time.setText("0" + hours + ":0" + minutes + ":0" + seconds);
                        } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1)) {
                            tv_time.setText("0" + hours + ":0" + minutes + ":" + seconds);
                        } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                            tv_time.setText("0" + hours + ":" + minutes + ":0" + seconds);
                        } else if ((String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                            tv_time.setText(hours + ":0" + minutes + ":0" + seconds);
                        } else if (String.valueOf(hours).length() == 1) {
                            tv_time.setText("0" + hours + ":" + minutes + ":" + seconds);
                        } else if (String.valueOf(minutes).length() == 1) {
                            tv_time.setText(hours + ":0" + minutes + ":" + seconds);
                        } else if (String.valueOf(seconds).length() == 1) {
                            tv_time.setText(hours + ":" + minutes + ":0" + seconds);
                        } else {
                            tv_time.setText(hours + ":" + minutes + ":" + seconds);
                        }
                    }

                }

                @Override
                public void onFinish() {
                    setProgress(progress, endTime);
                    isRunning = false;
                    isPausable = false;
                    /*
                    NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
                    notifyBuilder.setContentTitle("Time Up");
                    notifyBuilder.setContentText(et_timer.getText().toString()+" Seconds Over");
                    mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());*/
                    timeUpNotification();
                }
            };
            countDownTimer.start();
        }else {
            Toast.makeText(getApplicationContext(),"Please enter the value",Toast.LENGTH_LONG).show();
        }

    }

    public void setProgress(int startTime, int endTime) {
        progressBarView.setMax(endTime);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(startTime);

    }
}