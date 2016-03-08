package com.scnu.swimmingtrainsystem.view;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.scnu.swimmingtrainsystem.R;


/**
 * @author lixinkun
 * @date 2016/1/5
 */
public class ClearEdittext extends EditText implements View.OnFocusChangeListener,TextWatcher {

    private Drawable mClearDrawable;
    private boolean hasFocus = false;
    private int oldGravity ;

    public ClearEdittext(Context context) {
        this(context,null);
        // TODO Auto-generated constructor stub
    }


    public ClearEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        init();
    }

    public ClearEdittext(Context context, AttributeSet attrs) {
        this(context, attrs,android.R.attr.editTextStyle);
        // TODO Auto-generated constructor stub
    }

    private void init(){
        oldGravity = getGravity();
        mClearDrawable = getCompoundDrawables()[2];
        if(mClearDrawable == null){
            mClearDrawable = getResources().getDrawable(R.drawable.deletable);
        }

        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        setClearIconVisible( false);
        setOnFocusChangeListener( this);
        addTextChangedListener( this);
        setSelection(getText().length());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if(event.getAction() == MotionEvent.ACTION_DOWN ){
            this.hasFocus = true;
            if(getCompoundDrawables()[2] == null){
                setClearIconVisible(getText().length() > 0);
                super.onTouchEvent(event);
            }
            //在ontouch 里面判断点击事件
            if(getCompoundDrawables()[2] != null){
                int x = (int ) event.getX();
                int y = (int ) event.getY();
                Rect rect = getCompoundDrawables()[2].getBounds();
                int height = rect.height();
                int destance = (getHeight() - height) / 2;
                boolean isInnerWidth = x > (getWidth() - getTotalPaddingRight()) && x < (getWidth() - getPaddingRight());
                boolean isInnerHeight = y >(destance) && y < (destance + height);
                if(isInnerWidth && isInnerHeight){
                    this.setText("" );
                }
            }
        }
        return super .onTouchEvent(event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        // TODO Auto-generated method stub
        if(hasFocus ){
            setClearIconVisible(getText().length() > 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub
        if(hasFocus == false){
            this.hasFocus = hasFocus;
        }
        if(this .hasFocus ){
            setClearIconVisible(getText().length() > 0);
        } else{
            setClearIconVisible( false);
        }
    }

    @Override
    public void setGravity(int gravity) {
        // TODO Auto-generated method stub
        oldGravity = gravity;
        super.setGravity(gravity);
    }

    private void setClearIconVisible(boolean visible){
        Drawable right = visible ? mClearDrawable:null ;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
        super.setGravity(visible ? Gravity.LEFT | Gravity.CENTER_VERTICAL : oldGravity);
    }


}