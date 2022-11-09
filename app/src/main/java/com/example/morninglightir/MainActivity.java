package com.example.morninglightir;

import android.hardware.ConsumerIrManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.morninglightir.databinding.ActivityMainBinding;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static WeakReference<MainActivity> weakActivity;

    public static MainActivity getmInstanceActivity() {
        return weakActivity.get();
    }

    public static long RTCMillis = 0;
    static AlarmManager am = null;
    ConsumerIrManager infraRed = null;

    //TextView Reached100At;

    Button wakeTimeButton, OffButton;
    int hour, minute;

    FloatingActionButton fab, fab1, fab2, fab3;

    Animation toBottom, fromBottom, closeAnim, openAnim;

    boolean isOpen = false; //FAB ANIM

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    int frequency = 32222; //32KHz
    int[] offIR = {9000, 4500, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
            562, 562, 562, 562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 562, 562, 1688, 562,
            1688, 562, 1688, 562, 562, 562, 1688, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
            562, 562, 562, 1688, 562, 562, 562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 1688, 562,
            1688,  45000};
    int[] upIR = {9000, 4500, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
            562, 562, 562, 562,  562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 562, 562,
            1688, 562, 1688, 562, 1688,  562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
            562, 562, 562, 562, 562,  562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 1688, 562,
            1688, 562, 1688, 562, 1688, 45000};
    int[] downIR = {9000, 4500, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
            562, 562, 562, 562,  562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 562, 562,
            1688, 562, 1688, 562, 1688,  562, 1688, 562, 562, 562, 562, 562, 562, 562, 562,
            562, 562, 562, 562, 562, 562,  562, 562, 562, 1688, 562, 1688, 562, 1688, 562,
            1688, 562, 1688, 562, 1688, 562, 1688, 45000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        infraRed = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        weakActivity = new WeakReference<>(MainActivity.this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        OffButton = findViewById(R.id.OffButton);

        OffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), WakeAlarm.class);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
                if (am == null)
                {
                    am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Toast.makeText(getApplicationContext(), "Alarm already cancelled", Toast.LENGTH_SHORT).show();
                    am.cancel(pi);
                    am = null;
                }
                else
                {
                    am.cancel(pi);
                    am = null;
                    Toast.makeText(getApplicationContext(), "Alarm cancelled", Toast.LENGTH_SHORT).show();
                }
                TextView Reached100At = findViewById(R.id.textView3);
                Reached100At.setText("starting light at: --:--");
                WakeAlarm.alarmTimes = 0;
                WakeAlarm.intervalMil = 1000 * 60 * 6;
            }
        });

        wakeTimeButton = findViewById(R.id.wakeTimeButton);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.offButton);
        fab2 = (FloatingActionButton) findViewById(R.id.downArrow);
        fab3 = (FloatingActionButton) findViewById(R.id.upArrow);

        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        closeAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        openAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);

        final MediaPlayer swish = MediaPlayer.create(this, R.raw.swish);
        final MediaPlayer blast = MediaPlayer.create(this, R.raw.blast);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
                swish.start();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infraRed.transmit(frequency, offIR);
                blast.start();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infraRed.transmit(frequency, downIR);
                blast.start();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infraRed.transmit(frequency, upIR);
                blast.start();
            }
        });
    }

    public ConsumerIrManager getInfraRed(){
        return infraRed; }

    public void popTimePicker(View view)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                final MediaPlayer lightSaber = MediaPlayer.create(MainActivity.this, R.raw.lightsaber_sound);
                lightSaber.start();
                hour = selectedHour;
                minute = selectedMinute;
                wakeTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d",hour, minute));
                if ((minute - 32) < 0)
                {
                    hour -= 1;
                    minute += 28;
                }
                else
                {
                    minute -= 32;
                }
                Calendar calendar = Calendar.getInstance();

                //If it's a morning alarm check to add +1 to day of month MIGHT NOT WORK ON END OF MONTHS!!!
                if (LocalTime.now().getHour() > hour) {
                    Toast.makeText(MainActivity.this, String.format("Morning alarm!"), Toast.LENGTH_LONG).show();
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)+1,
                            hour, minute, 0);
                }
                else{
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                            hour, minute, 0);
                }
                RTCMillis = calendar.getTimeInMillis();
                TextView Reached100At = findViewById(R.id.textView3);

                Reached100At.setText(String.format(Locale.getDefault(), "starting light at: %02d:%02d",hour, minute));

                setAlarm(calendar.getTimeInMillis());

            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, /*style,*/ onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();

    }
    public void setAlarm(long time) {
        //getting the alarm manager
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, WakeAlarm.class);

        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        //setting the alarm
        AlarmManager.AlarmClockInfo aci = new AlarmManager.AlarmClockInfo(time, pi);
        am.setAlarmClock(aci, pi);
        //OLD METHOD
        //am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);

        Toast.makeText(this, String.format("Alarm is set [#%d]", WakeAlarm.alarmTimes), Toast.LENGTH_SHORT).show();
    }
    private void animateFab(){
        if (isOpen){
            fab.startAnimation(closeAnim);
            fab1.startAnimation(toBottom);
            fab2.startAnimation(toBottom);
            fab3.startAnimation(toBottom);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab1.setVisibility(View.INVISIBLE);
            fab2.setVisibility(View.INVISIBLE);
            fab3.setVisibility(View.INVISIBLE);
            isOpen = false;
        }
        else {
            fab.startAnimation(openAnim);
            fab1.startAnimation(fromBottom);
            fab2.startAnimation(fromBottom);
            fab3.startAnimation(fromBottom);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            fab1.setVisibility(View.VISIBLE);
            fab2.setVisibility(View.VISIBLE);
            fab3.setVisibility(View.VISIBLE);
            isOpen = true;
        }
    }
}

