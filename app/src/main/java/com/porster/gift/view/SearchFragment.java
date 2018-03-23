package com.porster.gift.view;

import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.porster.gift.R;
import com.porster.gift.core.BaseRecyclerFragment;
import com.porster.gift.core.DataManager;
import com.porster.gift.core.SessionData;
import com.porster.gift.model.GiftModel;
import com.porster.gift.utils.AppConstants;
import com.porster.gift.utils.LogCat;
import com.porster.gift.utils.ViewHolder;
import com.porster.gift.view.setting.HighSetAct;
import com.porster.gift.widget.SearchBarLayout;
import com.porster.gift.widget.pulltorefresh.PullToRefreshBase;
import com.porster.gift.widget.recycler.BaseAdapter;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * 查询
 * Created by Porster on 17/2/23.
 */

public class SearchFragment extends BaseRecyclerFragment<GiftModel>{

    static final int THEME_COLOR=0XFFFD4831;
    /**自动消失时间 单位秒*/
    int delayDismiss;

    EditText mSearchEdit;

    @Override
    protected void initUI() {
        addActionBar("查询").getLeft().setVisibility(View.GONE);

        getRecyclerView().setEmptyView(showEmptyView("输入文字、拼音就能搜索答案哦"));

        getRecyclerView().setMode(PullToRefreshBase.Mode.DISABLED);
        setDividerShow();

        delayDismiss= (int) SessionData.getObject(mContext, HighSetAct.SP_QUERY_AUTO_EMPTY,0);


        final SearchBarLayout search_bar=$(R.id.search_bar);
        search_bar.setCancelSearchLayout(new SearchBarLayout.OnCancelSearchLayout() {
            @Override
            public void OnCancel() {
                getRecyclerView().setEmptyView(showEmptyView("输入文字、拼音就能搜索答案哦"));
            }
        });

        getRecyclerView().getRefreshableView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        search_bar.hiddenKeybord();
                        if(delayDismiss>0){
                            mainView.removeCallbacks(run);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(delayDismiss>0){
                            mainView.postDelayed(run,delayDismiss*1000);
                        }
                        break;
                }
                return false;
            }
        });

        /*输入栏*/
        mSearchEdit= search_bar.getEditText();
        mSearchEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(mContext,"GiftSearch");
            }
        });
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(delayDismiss>0){
                    mainView.removeCallbacks(run);
                    mainView.postDelayed(run,delayDismiss*1000);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0){
                    getAdapter().clear();
                    getRecyclerView().setEmptyView(showEmptyView("输入文字、拼音就能搜索答案哦"));
                    return;
                }
                ArrayList<GiftModel> mFilter=new ArrayList<GiftModel>();
                String pinyin= s.toString().toLowerCase();

                LogCat.i(AppConstants.TAG,"检索"+pinyin);

                for (GiftModel giftModel : DataManager.getInstance().getGiftModels()) {
                    String searchKey=giftModel.pinyin.replace("_","")+giftModel.content;
                    if(searchKey.contains(pinyin)){
                        //优先查找汉字
                        int index=giftModel.content.indexOf(pinyin);
                        if(index!=-1){
                            giftModel.subIndexStart=index;
                            giftModel.subIndexEnd=index+pinyin.length();
                        }else{//没有汉字则可以能为拼音
                            index=giftModel.pinyin.indexOf(pinyin);
                            if(index!=-1){
                                giftModel.subIndexStart=index;
                                giftModel.subIndexEnd=index+pinyin.length();
                            }
                        }
//                        Log.w(AppConstants.TAG,"结果="+giftModel.pinyin+"("+giftModel.subIndexStart+","+
//                                giftModel.subIndexEnd+")");
                        mFilter.add(giftModel);
                    }
                }

                LogCat.i(AppConstants.TAG,"找到"+mFilter.size()+"条");

                getRecyclerView().hideEmptyView();
                getAdapter().clear();
                getAdapter().addItems(mFilter);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private Runnable run=new Runnable() {
        @Override
        public void run() {
            mSearchEdit.setText("");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainView.removeCallbacks(run);

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected boolean autoRefresh() {
        return false;
    }

    @Override
    protected BaseAdapter<GiftModel> initAdapter() {
        return new BaseAdapter<GiftModel>(mContext) {
            @Override
            public int getLayoutId() {
                return R.layout.fragment_search_item;
            }

            @Override
            public void bindView(View view, int position, GiftModel bean) {

                TextView t= ViewHolder.get(view,R.id.anser_content);
                if(bean.subIndexEnd>0){
                    SpannableString ss=new SpannableString(bean.content);
                    try {
                        ss.setSpan(new ForegroundColorSpan(THEME_COLOR),bean.subIndexStart,bean.subIndexEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    t.setText(ss);
                }else{
                    t.setText(bean.content);
                }
                setText(view,R.id.anser,bean.rightAnswer);
            }
        };
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {

    }

    @Override
    public void onListItemPartClick(View view, Object obj, int state) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            delayDismiss= (int) SessionData.getObject(mContext, HighSetAct.SP_QUERY_AUTO_EMPTY,2);
        }
    }
}
