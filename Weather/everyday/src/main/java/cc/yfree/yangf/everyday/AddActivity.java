package cc.yfree.yangf.everyday;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


public class AddActivity extends AppCompatActivity {

    private TextView date,time,notice;
    private Calendar cal;
    private int year,month,day,hour,minute;
    private String week;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        /*设置toolbar*/
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.save:
                        onBackPressed();
                        break;
                }
                return true;
            }
        });

        //获取当前日期
        getDate();
        date = (TextView)findViewById(R.id.datepick);
        date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker arg0, int year_n, int month_n, int day_n) {
//                        Toast.makeText(getApplicationContext(), "click" + year + month + day, Toast.LENGTH_SHORT).show();
                        year = year_n;
                        month = month_n;
                        day = day_n;
                        cal.set(year, month, day);
                        week = getWeekDay(cal);
                        date.setText(year+"年"+(++month)+"月"+day+"日" +"  "+week);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                    }
                };
                DatePickerDialog dialog = new DatePickerDialog(AddActivity.this, 0,listener,year,month-1,day);//后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
                dialog.show();
//                Toast.makeText(getApplicationContext(), "click" + year + month + day, Toast.LENGTH_SHORT).show();
            }           //getDate
        });

        /*打开开关显示*/
        time = (TextView)findViewById(R.id.timepick);
        Switch sw = (Switch)super.findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    time.setVisibility(View.GONE);
                else
                    time.setVisibility(View.VISIBLE);
            }
        });

        time.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                TimePickerDialog.OnTimeSetListener time_callback=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker arg0, int arg1, int arg2) {//arg1表示小时，arg2表示分钟
                        time.setText(String.format("%s:%s", arg1, format_conver(arg2)));//格式输出
                    }
                };
                TimePickerDialog timePicker=new TimePickerDialog(AddActivity.this, 0, time_callback, hour, minute, true);
                timePicker.show();
            }
        });

        /* 添加提醒*/
        notice = (TextView)findViewById(R.id.notice);
        notice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showSingleChoice();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    //获取当前日期
    private void getDate() {
        cal=Calendar.getInstance();
        year=cal.get(Calendar.YEAR);       //获取年月日时分秒
//        Log.i("wxy","year"+year);
        month=cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        day=cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        week = getWeekDay(cal);

    }

    /*获取星期几*/
    private String getWeekDay(Calendar c)
    {
//        Toast.makeText(getApplicationContext(), "getWeekDay", Toast.LENGTH_SHORT).show();
        if (c == null){
            return "周一";
        }
        if (Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周一";
        }
        if (Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周二";
        }
        if (Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周三";
        }
        if (Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周四";
        }
        if (Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周五";
        }
        if (Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周六";
        }
        if (Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)){
            return "周日";
        }
        return "周一";
    }

    public String format_conver(int s){//该方法为了输出一位数时保证前面加一个0，使之与实现十位数对齐，比如时间是12：5，使用该方法后输出为12：05
        return s>=10?""+s:"0"+s;
    }


    /*弹出提醒*/
    public void showSingleChoice() {
        final String[] items = new String[]{"无通知","在活动开始时","提前 10 分钟","提前 30 分钟","自定义"};
        new AlertDialog.Builder(this)
//            .setTitle("列表框")
            .setItems(new String[] {"无通知","在活动开始时","提前 10 分钟","提前 30 分钟","自定义"}, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    switch (which) {
                        case 0:
                            notice.setText(items[0]);
                            break;
                        case 1:
                            notice.setText(items[1]);
                            break;
                        case 2:
                            notice.setText(items[2]);
                            break;
                        case 3:
                            notice.setText(items[3]);
                            break;
                        case 4:
                            showCustom();
                            break;
                    }
                }
            })
            .show();
    }

    /*弹出自定义框*/
    public void showCustom(){
        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker arg0, int year, int month, int day) {
                TimePickerDialog.OnTimeSetListener time_callback=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker arg0, int arg1, int arg2) {//arg1表示小时，arg2表示分钟
                        notice.setText(notice.getText() + String.format("%s:%s", arg1, format_conver(arg2)));
                    }
                };
                TimePickerDialog timePicker=new TimePickerDialog(AddActivity.this, 0, time_callback, hour, minute, true);
                notice.setText(year+"年"+(++month)+"月"+day+"日"+"  ");      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                timePicker.show();
            }
        };
        DatePickerDialog dialog=new DatePickerDialog(AddActivity.this, 0,listener,year,month,day);//后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
        dialog.show();
    }
}
