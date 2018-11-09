package sidebyside3.david.com.sidebyside4;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Gongwei (David) Chen on 11/2/2018.
 */

public class BootReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        //  execute when Boot Completed
        /**Schedule your Alarm Here**/
        SharedPreferences pref=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        int hour=pref.getInt("Hour",0);//0 is default if nothing was stored
        int minute=pref.getInt("Minute",0);

        Intent intent1=new Intent(context,AlarmReceiver.class);
        //FLAG_UPDATE_CURRENT: if the described PendingIntent already exists,
        //keep it but replace its extra data with what is in this new Intent.
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context
                ,0
                ,intent1
                ,0);
        AlarmManager am=(AlarmManager)context.getSystemService(context.ALARM_SERVICE);



        //RTC_WAKEUP: wake up the device when it goes off.
        am.setRepeating(AlarmManager.RTC_WAKEUP
                , System.currentTimeMillis()+1000//time of day you want the notification
                ,AlarmManager.INTERVAL_DAY
                ,pendingIntent);
        Toast.makeText(context, "Booting Completed", Toast.LENGTH_LONG).show();
    }

}
