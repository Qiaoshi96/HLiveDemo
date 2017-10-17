package jingou.jo.com.danmu;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    private boolean mIsShowDanmaku;
    private DanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;


    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    //    设置播放
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoView videoView = (VideoView) findViewById(R.id.video_view);
        Uri uri = Uri.parse("http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4");
        videoView.setVideoURI(uri);
        videoView.start();
//        尝试添加弹幕
//        获取id
        mDanmakuView = (DanmakuView) findViewById(R.id.danmaku_view);

//        getDanmu();
        /**
         * 加入自定义弹幕
         */

//        获取id
        final LinearLayout operationLayout = (LinearLayout) findViewById(R.id.operation_layout);
        final Button send = (Button) findViewById(R.id.send);
        final EditText editText = (EditText) findViewById(R.id.edit_text);
        final Button change = (Button) findViewById(R.id.postion);
//        进入默认不是全屏所以设置为不可见
        editText.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);

//        给横竖屏切换设置监听
//如果是全屏的话就显示弹幕如果不是全屏的话就不显示弹幕
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    editText.setVisibility(View.INVISIBLE);
                    send.setVisibility(View.INVISIBLE);
//                    同时让弹幕对用户不可见
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                        mDanmakuView.stop();
                    }
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                    并且让弹幕显示
                    editText.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
//                    显示弹幕
                    getDanmu();
                }
            }
        });


//        设置监听（通过判断来设置键盘是否显示）当点击弹幕时显示再次点击时隐藏
        mDanmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (operationLayout.getVisibility() == View.GONE) {
                    operationLayout.setVisibility(View.VISIBLE);
                } else {
                    operationLayout.setVisibility(View.GONE);
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editText.getText().toString();
//                通过一个工具类判断如果不问空的话添加弹幕
                if (!TextUtils.isEmpty(content)) {
//                    设置true加边框
                    addDanmaku(content, true);
//                    添加完后清空键盘
                    editText.setText("");
                }
            }
        });
//    设置底部虚拟键的方法
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });

    }

    //    添加测试的弹幕
    private void addDanmaku(String content, boolean withBorder) {
//    设置弹幕显示的方式
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.FLAG_REQUEST_REMEASURE);
        danmaku.text = content;
        danmaku.padding = 6;
//    设置字体大小吧sp转成ps
        danmaku.textSize = sp2px(18);
        danmaku.textColor = Color.parseColor("#ae67aa");
        danmaku.setTime(mDanmakuView.getCurrentTime());
        danmaku.textShadowColor = Color.WHITE;

//    设置边框
        if (withBorder) {
            danmaku.borderColor = Color.parseColor("#ff6677");
        }
        mDanmakuView.addDanmaku(danmaku);
    }

    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsShowDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "天下一枝梅" + time;
//                    开启线程添加弹幕  设置false 不加边框
                    addDanmaku(content, false);
                    try {
//                        线程休眠的时间保持一直有弹幕
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //暂停时
    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    //重新启动时
    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    //  销毁时
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsShowDanmaku = false;
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    //创建一个执行弹幕的方法
    public void getDanmu() {
        mDanmakuView.enableDanmakuDrawingCache(true);//设置缓存
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            //            在准备的时候添加这么多弹幕
            @Override
            public void prepared() {
                mIsShowDanmaku = true;
                mDanmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
//        创建弹幕库
        mDanmakuContext = DanmakuContext.create();
        mDanmakuView.prepare(parser, mDanmakuContext);
    }


    //该方法是让屏幕变得横屏
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
}
}
