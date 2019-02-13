package sidebyside3.david.com.sidebyside4;

import android.app.Activity;
import android.app.NotificationManager;
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
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_LOCKED_BOOT_COMPLETED;

/**
 * Create notification object when this class receives the pending intent
 */


public class AlarmReceiver extends BroadcastReceiver {
    int ID=999;

    @Override
    public void onReceive(Context context, Intent intent){
        SharedPreferences pref = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        int hour = pref.getInt("Hour", 0);
        int minute = pref.getInt("Minute", 0);
        boolean switchState=pref.getBoolean("switchState",true);

        if("android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction()))
        {
            Setting setting=new Setting();
            setting.scheduleNotification(hour,minute);
            Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
        }

        if("SCHEDULE_IT".equals(intent.getAction())) {
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mNotifiyBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setLargeIcon(largeIcon)
                    .setContentTitle("Take a photo, now!")
                    .setContentText("Take a photo now, and you will see huge results.")
                    .setSound(alarmSound);
            notificationManager.notify(ID, mNotifiyBuilder.build());
        }
    }
}
