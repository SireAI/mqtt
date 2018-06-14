package com.sire.messagepushmodule.Controller;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sire.corelibrary.Controller.SireController;
import com.sire.corelibrary.Executors.AppExecutors;
import com.sire.corelibrary.Lifecycle.DataLife.AbsentLiveData;
import com.sire.corelibrary.Networking.dataBound.DataResource;
import com.sire.corelibrary.RecyclerView.AutoViewStateAdapter;
import com.sire.corelibrary.RecyclerView.base.ItemViewDataBindingDelegate;
import com.sire.corelibrary.RecyclerView.base.ViewHolder;
import com.sire.corelibrary.Utils.ScreenUtils;
import com.sire.corelibrary.Utils.TimeOffSetUtils;
import com.sire.corelibrary.Utils.ToastUtils;
import com.sire.corelibrary.View.EmojiView.EmojiPopup;
import com.sire.messagepushmodule.BR;
import com.sire.messagepushmodule.DB.Entry.IMMessage;
import com.sire.messagepushmodule.Pojo.MessageRequestInfor;
import com.sire.messagepushmodule.R;
import com.sire.messagepushmodule.View.BackListenEditText;
import com.sire.messagepushmodule.ViewModel.MessagePushViewModel;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static com.sire.messagepushmodule.Constant.Constant.TALK_USER_ID;
import static com.sire.messagepushmodule.Constant.Constant.TALK_USER_IMG;
import static com.sire.messagepushmodule.Constant.Constant.TALK_USER_NAME;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/02/27
 * Author:Sire
 * Description:
 * ==================================================
 */

public class IMMessageController extends SireController implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher, BackListenEditText.BackListener, OnRefreshListener {
    private static final int LEFT_MESSAGE_TYPE = 1003;
    private static final int RIGHT_MESSAGE_TYPE = 1004;
    @Inject
    MessagePushViewModel messagePushViewModel;
    @Inject
    AppExecutors appExecutors;
    private int SETP_PAGE_SIZE = 0;
    private LiveData<DataResource<List<IMMessage>>> messageData;
    private MutableLiveData<MessageRequestInfor> messageRequestInforData = new MutableLiveData<>();
    private RecyclerView recyclerView;
    private IMMessageAdapter immessageAdapter;
    private EmojiPopup emojiPopup;
    private BackListenEditText etMessage;
    private ImageButton ibPraise;
    private TextView tvSend;
    private boolean isPanShow;
    private  boolean loadMore = false;
    private  boolean firstTime = true;
    private SmartRefreshLayout swipeRefreshView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        String talkUserId = intent.getStringExtra(TALK_USER_ID);
        //注册监听
        imMessageCallback(talkUserId);
        requestMoreIMMessageData();
        messagePushViewModel.setImSate(true);
    }

    private void imMessageCallback(String talkUserId) {
        messageData = Transformations.switchMap(messageRequestInforData, messageRequestInfor -> {
            if (messageRequestInfor == null) {
                return AbsentLiveData.create();
            } else {
                return messagePushViewModel.observerIMMessageDbDataChange(talkUserId, messageRequestInfor.getTimeLine(), messageRequestInfor.getPageSize());
            }
        });
        messageData.observe(this, listDataResource -> {
            switch (listDataResource.status) {
                case LOADING:
                    break;
                case ERROR:
                    cloesRefresh();
                    break;
                case SUCCESS:
                    cloesRefresh();
                    if(needRefresh(listDataResource.data)){
                        refreshUI(listDataResource.data);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private boolean needRefresh(List<IMMessage> data) {
        boolean need = immessageAdapter == null||immessageAdapter.getDataSource() == null||immessageAdapter.getDataSource().size()==0 || immessageAdapter.getDataSource().size() < data.size() || immessageAdapter.getDataSource().get(immessageAdapter.getDataSource().size()-1).getMessageCreateTime().getTime() != data.get(0).getMessageCreateTime().getTime();
        if (!need) {
            swipeRefreshView.setEnableRefresh(false);
        }
        return need;
    }

    public void requestMoreIMMessageData() {
        MessageRequestInfor messageRequestInfor = new MessageRequestInfor();
        messageRequestInfor.setTimeLine(TimeOffSetUtils.tommorrow());
        messageRequestInfor.setPageSize(SETP_PAGE_SIZE += 10);
        messageRequestInforData.setValue(messageRequestInfor);
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.contentLay))
                .build(etMessage);
    }

    private void initView() {
        setContentView(R.layout.controller_immessage);
        Toolbar toolBar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        String talkUserName = intent.getStringExtra(TALK_USER_NAME);
        toolBar.setTitle(talkUserName);
        setActionBarEnabled(toolBar);
        //Recyclerview
        recyclerView = findViewById(R.id.rv_immessage);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Drawable drawable = getResources().getDrawable(R.drawable.shape_tranluncent);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(drawable);
        recyclerView.addItemDecoration(dividerItemDecoration);

        //pull Refresh
        swipeRefreshView = findViewById(R.id.srl);
        swipeRefreshView.setOnRefreshListener(this);
        swipeRefreshView.setDisableContentWhenLoading(true);
        swipeRefreshView.setDisableContentWhenRefresh(true);
        swipeRefreshView.setRefreshHeader(new ClassicsHeader(this));

        //comment write
        ibPraise = findViewById(R.id.ib_praise);
        ibPraise.setOnClickListener(this);
        tvSend = findViewById(R.id.tv_send);
        etMessage = findViewById(R.id.et_comment);
        etMessage.setBackListener(this);
        etMessage.setOnClickListener(this);
        etMessage.setOnFocusChangeListener(this);
        tvSend.setOnClickListener(this);

        etMessage.addTextChangedListener(this);
        setUpEmojiPopup();
    }

    private void refreshUI(List<IMMessage> imMessages) {
        Collections.reverse(imMessages);
        if (immessageAdapter == null) {
            immessageAdapter = new IMMessageAdapter(imMessages, recyclerView, messagePushViewModel);
            recyclerView.setAdapter(immessageAdapter);
        } else {
            immessageAdapter.refreshDataSource(imMessages);
        }
        if(!loadMore){
            if(firstTime){
                recyclerView.scrollToPosition(immessageAdapter.getDataSource().size() - 1);
                firstTime = false;
            }else {
                recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(immessageAdapter.getDataSource().size() - 1), 200);
            }
        }else {
            loadMore = false;
        }

    }

    /**
     * 设置发送文字按钮状态
     *
     * @param s
     */
    private void setSendState(CharSequence s) {
        tvSend.setEnabled(!checkEmpty(s));
        if (checkEmpty(s)) {
            tvSend.setEnabled(false);
            tvSend.setTextColor(getResources().getColor(R.color.white_bg_3_text
            ));
        } else {
            tvSend.setEnabled(true);
            tvSend.setTextColor(getResources().getColor(R.color.colorPrimaryDark
            ));
        }
    }

    private boolean checkEmpty(CharSequence s) {
        return s == null || TextUtils.isEmpty(s.toString().trim());
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ib_praise) {
            if (isPanShow) {
                emojiPopup.toggle();
                ibPraise.setImageResource(emojiPopup.isShowing() ? R.drawable.svg_keyboard : R.drawable.svg_emoji);
            } else {
                ScreenUtils.showKeyBoard(etMessage);
                isPanShow = true;
                emojiPopup.toggle();
                ibPraise.setImageResource(R.drawable.svg_keyboard);
            }
        } else if (i == R.id.et_comment) {
            if (!isPanShow) {
                isPanShow = true;
            }
        } else if (i == R.id.tv_send) {

            //传递
            String message = etMessage.getText().toString().trim();
            sendMessage(view.getContext(), message);
            resetInput();
        }
    }

    private void sendMessage(Context context, String message) {
        Intent intent = getIntent();
        String talkUserImg = intent.getStringExtra(TALK_USER_IMG);
        String talkUserName = intent.getStringExtra(TALK_USER_NAME);
        String talkUserID = intent.getStringExtra(TALK_USER_ID);
        messagePushViewModel.sendMessage(message, talkUserID, talkUserName, talkUserImg).observe(this, new Observer<DataResource<IMMessage>>() {
            @Override
            public void onChanged(@Nullable DataResource<IMMessage> imMessageDataResource) {
                switch (imMessageDataResource.status) {
                    case SUCCESS:
                        Timber.i("信息发送成功");
                        appExecutors.diskIO().execute(() -> messagePushViewModel.saveUserMessage(imMessageDataResource.data));
                        break;
                    case ERROR:
                        ToastUtils.showToast(context, "信息发送失败");
                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }

    private void resetInput() {
        isPanShow = false;
        etMessage.setText("");
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
        ScreenUtils.hideSoftInput(etMessage);
        ibPraise.setImageResource(R.drawable.svg_emoji);

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
    public boolean onBackSoftInputDismiss() {
        isPanShow = false;
        ibPraise.setImageResource(R.drawable.svg_emoji);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        loadMore = true;
        requestMoreIMMessageData();
    }

    private void cloesRefresh(){
        if(swipeRefreshView.isRefreshing()){
            swipeRefreshView.finishRefresh();
        }
    }

    private static class IMMessageAdapter extends AutoViewStateAdapter<IMMessage> {



        public IMMessageAdapter(List<IMMessage> dataSource, RecyclerView recyclerView, MessagePushViewModel messagePushViewModel) {
            super(dataSource, recyclerView);
            addItemViewDelegate(LEFT_MESSAGE_TYPE, new IMMessageDelegate(messagePushViewModel, LEFT_MESSAGE_TYPE));
            addItemViewDelegate(RIGHT_MESSAGE_TYPE, new IMMessageDelegate(messagePushViewModel, RIGHT_MESSAGE_TYPE));
        }

        @Override
        protected boolean areContentsTheSame(IMMessage oldItem, IMMessage newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        protected boolean areItemsTheSame(IMMessage oldItem, IMMessage newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    }

    private static class IMMessageDelegate extends ItemViewDataBindingDelegate<IMMessage> {
        private final int messageType;
        private final MessagePushViewModel messagePushViewModel;

        public IMMessageDelegate(MessagePushViewModel messagePushViewModel, int messageType) {
            this.messagePushViewModel = messagePushViewModel;
            this.messageType = messageType;
        }

        @Override
        public int getItemViewLayoutId(int viewType) {
            if (messageType == RIGHT_MESSAGE_TYPE) {
                return R.layout.view_componnent_im_right;
            } else if (messageType == LEFT_MESSAGE_TYPE) {
                return R.layout.view_componnent_im_left;
            }
            throw new RuntimeException("undefined type");
        }

        @Override
        public boolean isForViewType(IMMessage item, int position) {
            if (item.getFromAuthorId().equals(messagePushViewModel.getUserId()) && messageType == RIGHT_MESSAGE_TYPE) {
                return true;
            } else if (item.getToAuthorId().equals(messagePushViewModel.getUserId()) && messageType == LEFT_MESSAGE_TYPE) {
                return true;
            }
            return false;
        }

        @Override
        public void convert(ViewHolder holder, IMMessage item, int position) {
            super.convert(holder, item, position);
            changeIMMessageState(item);
        }

        private void changeIMMessageState(IMMessage item) {
            if(!item.isRead()){
                item.setRead(true);
                messagePushViewModel.setIMMessageRead(item);
            }
        }

        @Override
        protected int getItemBRName() {
            return BR.imMessage;
        }
    }

    @Override
    protected void onDestroy() {
        messagePushViewModel.setImSate(false);
        super.onDestroy();
    }
}
