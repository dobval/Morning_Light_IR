package com.example.morninglightir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.widget.Toast;

public class WakeAlarm extends BroadcastReceiver{

    static byte alarmTimes = 0;
    //FOR TESTING: 1 min (SHOULD BE 6 min)
    static long intervalMil = 1000 * 60 * 6; ////1000ms * 60(sec) * 6(min), *0.85 ~32.12502...min ; 9 times *0.85^time

    @Override
    public void onReceive(Context context, Intent intent) {

        int frequency = 32222; //32KHz
        int[] tenIR = {9000, 4500, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
                562, 562, 562, 562, 562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 562, 562, 1688,
                562, 1688, 562, 1688, 562, 1688, 562, 562, 562, 1688, 562, 562, 562, 1688, 562, 562,
                562, 562, 562, 562, 562, 562, 562, 1688, 562, 562, 562, 1688, 562, 562, 562, 1688,
                562, 1688, 562, 1688, 45000};
        int[] upIR = {9000, 4500, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
                562, 562, 562, 562,  562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 562, 562, 1688,
                562, 1688, 562, 1688,  562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562, 562,
                562, 562, 562, 562,  562, 1688, 562, 1688, 562, 1688, 562, 1688, 562, 1688, 562,
                1688, 562, 1688, 562, 1688, 45000};
        if (alarmTimes == 0) {
            MainActivity.getInstance().getInfraRed().transmit(frequency, tenIR);
            alarmTimes = 1;
            intervalMil*=0.85;
            MainActivity.getInstance().setAlarm(intervalMil);
        }
        else if (alarmTimes < 10){
            MainActivity.getInstance().getInfraRed().transmit(frequency, upIR);
            alarmTimes++;
            intervalMil*=0.85;
            MainActivity.getInstance().setAlarm(intervalMil);
        }
        else {
            //getting the alarm manager
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //creating a new intent specifying the broadcast receiver
            Intent i = new Intent(context, WakeAlarm.class);
            //creating a pending intent using the intent
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);

            Toast.makeText(context, "Alarm finished", Toast.LENGTH_SHORT).show();

            //resetting
            alarmTimes = 0;
            intervalMil = 1000 * 60 * 6;

        }
    }
}
