package com.warmtel.expandtab;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class ExpandPopTabView extends LinearLayout implements OnDismissListener {
    private ArrayList<RelativeLayout> mViewLists = new ArrayList<RelativeLayout>();
    private ToggleButton mSelectedToggleBtn;
    private PopupWindow mPopupWindow;
    private Context mContext;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mSelectPosition;
    private int mTabPostion = -1; //记录TAB页号
    private int mToggleBtnBackground;
    private int mToggleBtnBackgroundColor;
    private int mToggleTextColor;
    private int mPopViewBackgroundColor;
    private float mToggleTextSize;

    public ExpandPopTabView(Context context) {
        super(context);
        init(context,null);
    }

    public ExpandPopTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.ExpandPopTabView);
            mToggleBtnBackground = a.getResourceId(R.styleable.ExpandPopTabView_tab_toggle_btn_bg, -1);
            mToggleBtnBackgroundColor = a.getColor(R.styleable.ExpandPopTabView_tab_toggle_btn_color, -1);
            mToggleTextColor = a.getColor(R.styleable.ExpandPopTabView_tab_toggle_btn_font_color,-1);
            mPopViewBackgroundColor = a.getColor(R.styleable.ExpandPopTabView_tab_pop_bg_color,-1);
            mToggleTextSize = a.getDimension(R.styleable.ExpandPopTabView_tab_toggle_btn_font_size, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(a != null) {
                a.recycle();
            }
        }
        mContext = context;
        /*mDisplayWidth = ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth();
        mDisplayHeight = ((Activity) mContext).getWindowManager().getDefaultDisplay().getHeight();*/
        WindowManager windowManager = ((Activity) mContext).getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;
        setOrientation(LinearLayout.HORIZONTAL);
    }
    public void addItemToExpandTab(String tabTitle, ViewGroup tabItemView) {
        ToggleButton tButton = (ToggleButton) LayoutInflater.from(mContext).inflate(R.layout.expand_tab_toggle_button, this, false);
        if(mToggleBtnBackground != -1){
            tButton.setBackgroundResource(mToggleBtnBackground);
        }
        if(mToggleBtnBackgroundColor != -1){
            tButton.setBackgroundColor(mToggleBtnBackgroundColor);
        }
        if(mToggleTextColor != -1){
            tButton.setTextColor(mToggleTextColor);
        }
        if(mToggleTextSize != -1){
            tButton.setTextSize(mToggleTextSize);
        }

        tButton.setText(tabTitle);
        tButton.setTag(++mTabPostion);
        tButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleButton tButton = (ToggleButton) view;
                if (mSelectedToggleBtn != null && mSelectedToggleBtn != tButton) {
                    mSelectedToggleBtn.setChecked(false);
                }
                mSelectedToggleBtn = tButton;
                mSelectPosition = (Integer) mSelectedToggleBtn.getTag();
                expandPopView();
            }
        });
        addView(tButton);

        RelativeLayout popContainerView = new RelativeLayout(mContext);

        if(mPopViewBackgroundColor != -1){
            popContainerView.setBackgroundColor(mPopViewBackgroundColor);
        }else{
            popContainerView.setBackgroundColor(Color.parseColor("#b0000000"));
        }
        //(int) (mDisplayHeight * 0.7) 可以设置高度为该值（屏幕的0.7倍）
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        popContainerView.addView(tabItemView, rl);
        popContainerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onExpandPopView();
            }
        });

        mViewLists.add(popContainerView);
    }

    public void setToggleButtonText(String tabTitle){
        ToggleButton toggleButton = (ToggleButton) getChildAt(mSelectPosition);
        toggleButton.setText(tabTitle);
    }

    private void expandPopView() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(mViewLists.get(mSelectPosition), mDisplayWidth, mDisplayHeight);
            mPopupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setOnDismissListener(this);
        }

        if (mSelectedToggleBtn.isChecked()) {
            if (!mPopupWindow.isShowing()) {
                showPopView();
                //Toast.makeText(mContext, "打开", Toast.LENGTH_SHORT).show();
            } else {
                mPopupWindow.setOnDismissListener(this);
                mPopupWindow.dismiss();
            }
        } else {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }
    }

    /**
     * 如果菜单成展开状态，则让菜单收回去
     * 注：Activty 销毁时 如果对话框没有关闭刚关闭
     */
    public boolean onExpandPopView() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            if (mSelectedToggleBtn != null) {
                mSelectedToggleBtn.setChecked(false);
                //Toast.makeText(mContext, "expand", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return false;
        }
    }

    public void showPopView(){
        if (mPopupWindow.getContentView() != mViewLists.get(mSelectPosition)) {
            mPopupWindow.setContentView(mViewLists.get(mSelectPosition));
        }
        //mPopupWindow.showAsDropDown(this, 0, 0);
        //解决7.0的popwindow永远在上方问题
        if(Build.VERSION.SDK_INT<24){
            mPopupWindow.showAsDropDown(this, 0, 0);
        }else {
            int[] location = new int[2];
            this.getLocationOnScreen(location);
            mPopupWindow.showAtLocation(this, Gravity.NO_GRAVITY,0,location[1] + this.getHeight());
        }
    }

    @Override
    public void onDismiss() {
        /*showPopView();
        mPopupWindow.setOnDismissListener(null);*/
        mSelectedToggleBtn.setChecked(false);
    }
}
