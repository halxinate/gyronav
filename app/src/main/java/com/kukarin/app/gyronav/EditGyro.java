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
        char[] spc = {' '};
        if(selStart==this.length()){ //at the end - insert space first
            this.setText(spc, length(),1);
        }
        this.setSelection(selStart, selStart + 1);
    }

    public void replaceAt(int i, char c) {
        String oriContent = getText().toString();
        int index = getSelectionStart() >= 0 ? getSelectionStart() : 0;
        StringBuilder sBuilder = new StringBuilder(oriContent);
        sBuilder.replace(index, index+1, ""+c);
        setText(sBuilder.toString());
        setSelection(index);
    }
}
