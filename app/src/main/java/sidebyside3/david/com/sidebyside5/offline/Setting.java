package sidebyside3.david.com.sidebyside5.offline;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import sidebyside3.david.com.sidebyside5.R;
import sidebyside3.david.com.sidebyside5.utils.AlarmReceiver;

public class Setting extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    LinearLayout timePickerButton;
    int mHour,mMinute;
    TextView timeSet,labelTime;
    Switch s;

    SharedPreferences pref;
    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        pref=getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=pref.edit();

        timePickerButton=(LinearLayout)findViewById(R.id.btn_time);
        timePickerButton.setOnClickListener(this);

        labelTime=(TextView)findViewById(R.id.labelTime);
        timeSet=(TextView)findViewById(R.id.in_time);
        int userHour=pref.getInt("Hour",0);//0 is default if nothing was stored
        int userMin=pref.getInt("Minute",0);
        timeSet.setText(String.format("%02d:%02d", userHour, userMin));

        s=(Switch)findViewById(R.id.SwitchID);
        s.setOnCheckedChangeListener(this);
        boolean currentSwitchState=pref.getBoolean("switchState",true);
        s.setChecked(currentSwitchState);
    }

    /**
     * When 'Set time' is pressed, launch time picker
     * @param v
     */
    @Override
    public void onClick(View v){
        Calendar c=Calendar.getInstance();
        mHour=c.get(Calendar.HOUR_OF_DAY);
        mMinute=c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog=new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        timeSet.setText(String.format("%02d:%02d", hour, minute));
                        //storing the inputted hour and time, so they reappear when app reopens
                        editor.putInt("Hour",hour);
                        editor.putInt("Minute",minute);
                        editor.apply();

                        scheduleNotification(hour,minute);
                    }
                },mHour,mMinute,true);
        timePickerDialog.show();
    }

    /**
     *Building Intent for AlarmReceiver and PendingIntent.
     * Give PendingIntent to AlarmManager
     * @param hour user-inputted hour of day
     * @param minute user-inputted minute of day
     */
    public void scheduleNotification(int hour, int minute){
        Intent intent1=new Intent(this,AlarmReceiver.class);
        intent1.setAction("SCHEDULE_IT");
        //FLAG_UPDATE_CURRENT: if the described PendingIntent already exists,
        //keep it but replace its extra data with what is in this new Intent.
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this
                ,0
                ,intent1
                ,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am=(AlarmManager)this.getSystemService(this.ALARM_SERVICE);

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
    /**
     * Switch Listener for scheduling notification reminder
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        if(isChecked){
            timePickerButton.setEnabled(true);
            labelTime.setTextColor(getResources().getColor(R.color.white));
            timeSet.setTextColor(getResources().getColor(R.color.blue));
        }else{//cancel notification if the switch is off
            AlarmManager am =  (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent1=new Intent(this,AlarmReceiver.class);
            intent1.setAction("SCHEDULE_IT");
            //FLAG_UPDATE_CURRENT: if the described PendingIntent already exists,
            //keep it but replace its extra data with what is in this new Intent.
            PendingIntent pendingIntent=PendingIntent.getBroadcast(this
                    ,0
                    ,intent1
                    ,PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(pendingIntent);
            timePickerButton.setEnabled(false);
            labelTime.setTextColor(getResources().getColor(R.color.grey));
            timeSet.setTextColor(getResources().getColor(R.color.grey));
        }
        editor.putBoolean("switchState",isChecked);
        editor.apply();
    }
}
