package sidebyside3.david.com.sidebyside5.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

import sidebyside3.david.com.sidebyside5.offline.Main;
import sidebyside3.david.com.sidebyside5.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Create notification object when this class receives the pending intent
 */


public class AlarmReceiver extends BroadcastReceiver {
    int ID=999;
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent){
        mContext=context;
        SharedPreferences pref = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        int hour = pref.getInt("Hour", 0);
        int minute = pref.getInt("Minute", 0);
        boolean switchState=pref.getBoolean("switchState",true);

        if(switchState) {
            try {
                if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction())) {
                    scheduleNotification(hour, minute);
                }
            } catch (Exception e) {
                Log.e("BOOT_error", "Error" + e);
            }
        }

        //Setting the clicking behavior of notification
        Intent notifIntent = new Intent(context, Main.class);
        notifIntent.setAction("openCamera");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, 0);

        if("SCHEDULE_IT".equals(intent.getAction())) {
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mNotifiyBuilder = new NotificationCompat.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setLargeIcon(largeIcon)
                    .setContentTitle("Take a photo, now!")
                    .setContentText("Take a photo now, and you will see huge results.")
                    .setSound(alarmSound);
            notificationManager.notify(ID, mNotifiyBuilder.build());
        }
    }

    /**
     *Building Intent for AlarmReceiver and PendingIntent.
     * Give PendingIntent to AlarmManager
     * @param hour user-inputted hour of day
     * @param minute user-inputted minute of day
     */
    public void scheduleNotification(int hour, int minute){
        Intent intent1=new Intent(mContext,AlarmReceiver.class);
        intent1.setAction("SCHEDULE_IT");
        //FLAG_UPDATE_CURRENT: if the described PendingIntent already exists,
        //keep it but replace its extra data with what is in this new Intent.
        PendingIntent pendingIntent=PendingIntent.getBroadcast(mContext
                ,0
                ,intent1
                ,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am=(AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,hour);//this 'hour' and minute' is picked by user
        calendar.set(Calendar.MINUTE,minute);

        long _alarm=0;
        Calendar now=Calendar.getInstance();
        if(calendar.getTimeInMillis()<=now.getTimeInMillis()){
            _alarm=calendar.getTimeInMillis()+(AlarmManager.INTERVAL_DAY+1);
        }else{
            _alarm=calendar.getTimeInMillis();
        }
        //RTC_WAKEUP: wake up the device when it goes off.
        am.setRepeating(AlarmManager.RTC_WAKEUP
                , _alarm//time of day you want the notification
                ,AlarmManager.INTERVAL_DAY
                ,pendingIntent);
    }
}
