package sidebyside3.david.com.sidebyside4;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by Gongwei (David) Chen on 10/22/2018.
 */

public class BootService extends IntentService {

    public BootService() {
        super("BootService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Your code to reset alarms.
        // All of these will be done in Background and will not affect
        // on phone's performance
        SharedPreferences pref=getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        int userHour=pref.getInt("Hour",0);//0 is default if nothing was stored
        int userMin=pref.getInt("Minute",0);
        scheduleNotification(userHour, userMin);
    }

    /**
     * For re-scheduling notification AlarmManager after rebooting
     * @param hour
     * @param minute
     */
    public void scheduleNotification(int hour, int minute){
        Intent intent1=new Intent(this,AlarmReceiver.class);
        //FLAG_UPDATE_CURRENT: if the described PendingIntent already exists,
        //keep it but replace its extra data with what is in this new Intent.
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this
                ,0
                ,intent1
                ,0);
        AlarmManager am=(AlarmManager)this.getSystemService(this.ALARM_SERVICE);

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,hour);
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
