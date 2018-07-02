package com.jd.jrapp.bm.message.controller;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jd.jrapp.bm.message.R;
import com.jd.jrapp.bm.message.adapter.IMMessageAdapter;
import com.jd.jrapp.bm.message.adapter.delegate.LeftTalkerDelegate;
import com.jd.jrapp.bm.message.adapter.delegate.LoadingDelegate;
import com.jd.jrapp.bm.message.adapter.delegate.RightTalkerDelegate;
import com.jd.jrapp.bm.message.bean.MessageRequestInfor;
import com.jd.jrapp.bm.message.bean.Talker;
import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;
import com.jd.jrapp.bm.message.utils.CommonUtils;
import com.jd.jrapp.bm.message.utils.TimeOffSetUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static com.jd.jrapp.bm.message.constant.Constant.PEER_TALKER;


/**
 * ==================================================
 * All Right Reserved
 * Date:2018/02/27
 * Author:Sire
 * Description:
 * ==================================================
 */
public class IMMessageController extends FragmentActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher, LoadingDelegate.ICallBack,LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    public static final int STEP = 15;
    public static final int FIRST_POSITION = 0;
    public static final int DELAY_MILLIS = 500;
    private static final int STATE_FIRST_TIME = 1;
    private static final int STATE_ACTION_SEND = 2;
    private static final int STATE_ACTION_CHANGE = 3;

    private int totalPageSize = 0;

    private RecyclerView recyclerView;
    private IMMessageAdapter immessageAdapter;
    private ImageButton ibPraise;
    private TextView tvSend;
    private IMMessageModel model;
    private ImageView ivImMe;
    private EditText etMessage;
    private int focuseSate = STATE_FIRST_TIME;
    private AtomicBoolean firstTime = new AtomicBoolean(true);
    private AtomicBoolean loadMore = new AtomicBoolean(false);
    private RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == SCROLL_STATE_IDLE) {
                if (recyclerView != null) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (IMMessageController.this.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && IMMessageController.this.isDestroyed())) {
                                return;
                            }
                            requestMessageData();
                        }
                    }, DELAY_MILLIS);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new IMMessageModel();
        initView();
        initData();
    }





    private void initData() {
        model.getMessageData().observe(this, new Observer<List<IMMessage>>() {
            @Override
            public void onChanged(@Nullable List<IMMessage> imMessages) {
                refreshUI(imMessages);
            }
        });
        requestMessageData();
    }

    public void requestMessageData() {
        MessageRequestInfor messageRequestInfor = new MessageRequestInfor(TimeOffSetUtils.tommorrow(), totalPageSize += STEP);
        if (model != null) {
            model.setRequest(messageRequestInfor);
        }
    }

    private void refreshUI(List imMessages) {
        if (recyclerView == null) return;
        Collections.reverse(imMessages);
        if (immessageAdapter == null) {
            immessageAdapter = IMMessageAdapter.create()
                    .addItemViewDelegate(new LeftTalkerDelegate(model, R.layout.view_componnent_im_talker_left))
                    .addItemViewDelegate(new RightTalkerDelegate(model, R.layout.view_componnent_im_talker_right))
                    .addItemViewDelegate(new LoadingDelegate(R.layout.loading_state_item, this));
            recyclerView.setAdapter(immessageAdapter);
        }
        imMessages.add(FIRST_POSITION, LoadingDelegate.createLoadingMessage(imMessages.size() == totalPageSize));
        immessageAdapter.refreshData(imMessages);
        changeFocus();
    }

    /**
     * 内容焦点处理
     */
    private void changeFocus() {
        if (loadMore.compareAndSet(true, false)) {
            recyclerView.removeOnScrollListener(listener);
        } else {
            switch (focuseSate){
                case STATE_FIRST_TIME:
                    focuseSate = STATE_ACTION_CHANGE;
                    recyclerView.scrollToPosition(immessageAdapter.getItemCount() - 1);
                    break;
                case STATE_ACTION_SEND:
                    focuseSate = STATE_ACTION_CHANGE;
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(recyclerView!=null){
                                recyclerView.smoothScrollToPosition(immessageAdapter.getItemCount() - 1);
                            }
                        }
                    },300);
                    break;
                case STATE_ACTION_CHANGE:
                    //do nothing
                    break;
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Talker talker = model.getTalker();
        if(talker!=null){
            outState.putParcelable(PEER_TALKER,talker);
        }
    }



    private void initView() {
        setContentView(R.layout.controller_immessage);
        ivImMe = (ImageView) findViewById(R.id.iv_im_me);
        ivImMe.setOnClickListener(this);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        Intent intent = getIntent();
        Talker peerTalker = intent.getParcelableExtra(PEER_TALKER);
        model.setTalker(peerTalker);
        toolBar.setTitle(peerTalker.getUserName());
//        setActionBarEnabled(toolBar);
        //Recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.rv_immessage);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setSupportsChangeAnimations(false);
        recyclerView.setItemAnimator(defaultItemAnimator);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_tranluncent);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        dividerItemDecoration.setDrawable(drawable);
//        recyclerView.addItemDecoration(dividerItemDecoration);

        //comment write
        ibPraise = (ImageButton) findViewById(R.id.ib_praise);
        ibPraise.setOnClickListener(this);
        tvSend = (TextView) findViewById(R.id.tv_send);
        etMessage = (EditText) findViewById(R.id.et_comment);
        etMessage.setOnClickListener(this);
        etMessage.setOnFocusChangeListener(this);
        tvSend.setOnClickListener(this);

        etMessage.addTextChangedListener(this);
    }



//    private void setActionBarEnabled(@NonNull Toolbar toolbar) {
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//    }

    /**
     * 设置发送文字按钮状态
     *
     * @param s
     */
    private void setSendState(CharSequence s) {
        tvSend.setEnabled(!checkEmpty(s));
        if (checkEmpty(s)) {
            tvSend.setEnabled(false);
//            tvSend.setTextColor(getResources().getColor(R.color.white_bg_3_text
//            ));
        } else {
            tvSend.setEnabled(true);
//            tvSend.setTextColor(getResources().getColor(R.color.colorPrimaryDark
//            ));
        }
    }

    private boolean checkEmpty(CharSequence s) {
        return s == null || TextUtils.isEmpty(s.toString().trim());
    }

    @Override
    public void onClick(View view) {
        if(CommonUtils.isFastClick(500))return;
        focuseSate = STATE_ACTION_SEND;
        int id = view.getId();
//        if (i == R.id.ib_praise) {
//            if (isPanShow) {
////                emojiPopup.toggle();
//                ibPraise.setImageResource(emojiPopup.isShowing() ? R.drawable.svg_keyboard : R.drawable.svg_emoji);
//            } else {
////                ScreenUtils.showKeyBoard(etMessage);
//                isPanShow = true;
////                emojiPopup.toggle();
//                ibPraise.setImageResource(R.drawable.svg_keyboard);
//            }
//        } else if (i == R.id.et_comment) {
//            if (!isPanShow) {
//                isPanShow = true;
//            }
//        } else if (i == R.id.tv_send) {
//
//            //传递
//            String message = etMessage.getText().toString().trim();
//            sendRealTimeMessage(view.getContext(), message);
//            resetInput();
//        }
        if(id == R.id.iv_im_me){
            System.out.println("----------");
        }else if (id == R.id.tv_send) {
            String message = etMessage.getText().toString().trim();
//            sendTextMessage(message);
            sendImageMessage();
            resetInput();
        }

    }

    private void sendImageMessage() {
        File externalStorageDirectory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        String filePath = externalStorageDirectory.getPath() + "/test.png";
        File file = new File(filePath);
        model.sendOffLineFileMessage(file, Constant.FILE_MESSAGE_IMAGE);
    }

    private void resetInput() {
        etMessage.setText("");
        CommonUtils.hideSoftInput(etMessage);
    }

    private void sendTextMessage(String message) {
        model.sendOffLineTextMessage(message);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setSendState(s);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onLoadMore() {
        if (loadMore.compareAndSet(false, true)) {
            recyclerView.addOnScrollListener(listener);
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}
