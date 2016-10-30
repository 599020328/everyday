package cc.yfree.yangf.everyday;


import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaxiFragment extends Fragment {
    private FloatingActionButton fab1,fab2;
    private View view,mcardView;
    private ObjectAnimator animator;
    private TextView cannel;
    private boolean isHujiao;

    public TaxiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isHujiao =false;
        view = inflater.inflate(R.layout.fragment_taxi, null, false);
        mcardView = view.findViewById(R.id.distance_time);
        fab1 = (FloatingActionButton)view.findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)view.findViewById(R.id.fab2);
        cannel = (TextView)view.findViewById(R.id.cannel);
        /*fab1的监听（呼叫）*/
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHujiao){
                    dialog3();
                }
                else{
                    dialog1();
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("fab2","fab2");
            }
        });

        /*取消*/
        cannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cannel();
            }
        });


        return view;
    }

    /*呼叫司机？*/
    private void dialog1(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("是否呼叫离你最近的的士?"); //设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                dis_time();
                isHujiao = true;
                Toast.makeText(getActivity(), "确认" + which, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "取消" + which, Toast.LENGTH_SHORT).show();
            }
        });

        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    /*确认取消？*/
    private void dialog2(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("确认取消?"); //设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                isHujiao = false;
                animator = ObjectAnimator.ofFloat(mcardView, "translationY", 0);
                animator.setDuration(400);
                animator.start();
                Toast.makeText(getActivity(), "确认" + which, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "取消" + which, Toast.LENGTH_SHORT).show();
            }
        });

        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    /*不要重复呼叫*/
    private void dialog3(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("请不要重复呼叫！"); //设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                dis_time();
                Toast.makeText(getActivity(), "确认" + which, Toast.LENGTH_SHORT).show();
            }
        });

        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    /*显示路程和计时*/
    private void dis_time(){
        WindowManager wm = getActivity().getWindowManager();
        int height = wm.getDefaultDisplay().getHeight();
        float heightChange = height * 0.08f;
        animator = ObjectAnimator.ofFloat(mcardView, "translationY", heightChange);
        animator.setDuration(400);
        animator.start();
    }

    /*显示路程和计时*/
    private void cannel(){
        dialog2();
    }

}
