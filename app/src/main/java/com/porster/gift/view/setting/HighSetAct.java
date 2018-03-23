package com.porster.gift.view.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;

import com.porster.gift.R;
import com.porster.gift.core.BaseActivity;
import com.porster.gift.core.SessionData;
import com.porster.gift.core.ThemeCore;
import com.porster.gift.widget.ShSwitchView;

/**
 * 高级设置
 * Created by Porster on 17/5/25.
 */

public class HighSetAct extends BaseActivity{
    /**自动清空答案时间,单位秒*/
    public static final String SP_QUERY_AUTO_EMPTY="SP_QUERY_AUTO_EMPTY";
    /**错题，答对几次移除*/
    public static final String SP_REMOVE_COUNT="SP_REMOVE_COUNT";

    private int cancelCount;
    private int errorCount;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_high_set);
        addActionBar("高级设置");
        initUI();
    }

    private void initUI() {
        final SeekBar seekBar_auto_cancel=$(R.id.seekBar_auto_cancel);

        ShSwitchView h_switch=$(R.id.h_switch);
        h_switch.setTintColor(ThemeCore.getThemeColor(this));
        h_switch.setOnSwitchStateChangeListener(new ShSwitchView.OnSwitchStateChangeListener() {
            @Override
            public void onSwitchStateChange(boolean isOn) {

                if(!isOn){
                    cancelCount=0;
                }else{
                    seekBar_auto_cancel.setProgress(cancelCount-2);
                }

                $(R.id.seekBar_auto_cancel_layout).setVisibility(isOn? View.VISIBLE:View.GONE);
            }
        });
        seekBar_auto_cancel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress+=2;
                cancelCount=progress;
                setText(R.id.seekBar_auto_cancel_txt,progress+"秒");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cancelCount=(Integer) SessionData.getObject(mContext,SP_QUERY_AUTO_EMPTY,0);
        if(cancelCount>0){
            h_switch.setOn(true);
        }

        SeekBar seekBar_failed=$(R.id.seekBar_failed);
        seekBar_failed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress+=1;
                errorCount=progress;
                setText(R.id.seekBar_failed_txt,progress+"次");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int failCount=(Integer) SessionData.getObject(mContext,SP_REMOVE_COUNT,2);
        seekBar_failed.setProgress(failCount-1);
    }

    @Override
    protected void onDestroy() {
        SessionData.setObject(mContext,SP_QUERY_AUTO_EMPTY,cancelCount);
        SessionData.setObject(mContext,SP_REMOVE_COUNT,errorCount);
        super.onDestroy();
    }
}
