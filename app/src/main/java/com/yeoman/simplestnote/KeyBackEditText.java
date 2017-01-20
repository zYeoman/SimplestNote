package com.yeoman.simplestnote;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.optimus.edittextfield.EditTextField;

public class KeyBackEditText extends EditTextField {
    public KeyBackEditText(Context context) {
        super(context);
    }
    public KeyBackEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public KeyBackEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            ((Activity)this.getContext()).onBackPressed();
            //依据api等级2.0之后的都可以用第一种
            //或者用这一种             ((Activity)this.getContext()).onKeyDown(KeyEvent.KEYCODE_BACK, event);
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }


}
