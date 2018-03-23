package com.porster.gift.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.porster.gift.R;
import com.porster.gift.core.BaseFragment;
import com.porster.gift.widget.XDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * 科举
 * Created by Porster on 2017/7/28.
 */

public class GiftFragment extends BaseFragment implements View.OnClickListener{
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        mainView=inflater.inflate(R.layout.fragment_gift,container,false);
        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initUI();
    }

    private void initUI() {
        addActionBar("科举").getLeft().setVisibility(View.GONE);
        $(R.id.g_start,this);
        $(R.id.g_history,this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.g_start://开始科举
                MobclickAgent.onEvent(mContext,"GiftClick");
                XDialog.showRadioDialog(mContext,"后续开放，敬请期待",null);
                break;
            case R.id.g_history://科举历史
                XDialog.showRadioDialog(mContext,"后续开放，敬请期待",null);
                break;
        }
    }
}
