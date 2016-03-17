package com.kukarin.app.gyronav;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Always select 1 char even if there are zero!
 * Created by alexk on 3/16/2016.
 */
public class EditGyro extends EditText {
    public EditGyro(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if(selStart==this.length()){ //at the end - insert space first
            setText(getText().toString() + "_"); //extend the value to the right
        }
        setSelection(selStart, selStart + 1);
    }

    public void replaceAt(int i, char c) {
        String oriContent = getText().toString();
        int index = getSelectionStart() >= 0 ? getSelectionStart() : 0;
        StringBuilder sBuilder = new StringBuilder(oriContent);
        sBuilder.replace(index, index+1, "" + c);
        setText(sBuilder.toString());
        setSelection(index);
    }

    /**
     * Prevents softkeyboard or manual cursor selection, but retains selection display!
     * @return
     */
    @Override
    public boolean onCheckIsTextEditor() {
        return false;
    }

    /**
     * Delete char, but not set sursor. XCursor must be set on outside
     * @param i
     */
    public void deleteAt(int i) {
        if(i<0||i>this.length()-1) return;
        String oriContent = getText().toString();
        StringBuilder sBuilder = new StringBuilder(oriContent);
        sBuilder.deleteCharAt(i);
        setText(sBuilder.toString());
    }
}
