package com.kukarin.app.gyronav;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

// TODO: 3/16/2016  caps input, last letter not going to the right, initial letter not set, on letter index change kbd not updating
public class WaypointName extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "WaypointName";
    private final int ID0 = 9000;
    EditGyro mEdit;
    String mText = "";
    private int mX;
    private int mY;
    private int mI;
    private final String[] mBnText = {"1", "2", "3", "4", "5", "6", "a", "b", "c", "d", "e", "7", "f", "g", "h", "i", "j", "8", "k", "l", "m", "n", "o", "9", "p", "q", "r", "s", "t", "0", "u", "v", "w", "x", "y", "z", "_", "?", "!", "-", "\"", "OK"};
    private View mPushedButton = null;
    private int mRows = 7;
    private int mCols = 6;
    private LinearLayout mKBlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.activity_waypoint_name, null);
        setContentView(vg);

        //Keyboar layout building
        //Button
        LinearLayout.LayoutParams parbn = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        parbn.weight = 1;
        //Line
        LinearLayout.LayoutParams parln = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parln.weight = 1;

        mKBlayout = (LinearLayout) vg.findViewById(R.id.kbrootlayout);
        for (int j = 0; j < mRows; j++) { //rows
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setLayoutParams(parln);
            for (int i = 0; i < mCols; i++) { //columns
                Button b = new Button(this);
                int index = i + j * mCols;
                b.setText(mBnText[index]);
                b.setId(index + ID0);
                b.setTag(mBnText[index]);
                b.setLayoutParams(parbn);
                if (j == 0 || (i == mCols - 1 && j <= mRows - 2)) //highlight numbers
                    b.setTextColor(getResources().getColor(R.color.kb_bn_text2));
                else if (i == mCols - 1 && j == mRows - 1) //OK button
                    b.setTextColor(getResources().getColor(R.color.kb_bn_text3));
                b.setBackgroundResource(R.drawable.bnbackoff);
                b.setOnClickListener(this);
                line.addView(b);
            }
            mKBlayout.addView(line);
        }

        mText = getIntent().getStringExtra(Set.EXID_WPNAME);
        mEdit = (EditGyro) findViewById(R.id.kbedit);
        mEdit.setText(mText);
        mI = 0; //selection start
        mEdit.setSelection(mI);
        int index = pushPutton(mText.charAt(mI));
        mX = index % mRows;
        mY = index / mRows;

    }

    /**
     * Selects new button by its tag text, deselects prev button
     * @param s
     * @return
     */
    private int pushPutton(char s) {
        if(mPushedButton!=null)
            mPushedButton.setBackgroundResource(R.drawable.bnbackoff);
        int index = 0;
        for(String c : mBnText){
            if(c.charAt(0)==s) {
                mPushedButton = mKBlayout.findViewById(index + ID0);
                mPushedButton.setBackgroundResource(R.drawable.bnbackon);
                return index;
            }
            ++index;
        }
        return -1; //not found
    }

    /**
     * Selects button by its index
     * @param index
     */
    private char pushButton(int index) {
        if(mPushedButton!=null)
                mPushedButton.setBackgroundResource(R.drawable.bnbackoff);
        mPushedButton = mKBlayout.findViewById(index + ID0);
        mPushedButton.setBackgroundResource(R.drawable.bnbackon);
        return mPushedButton.getTag().toString().charAt(0);
    }

    private void returnResult() {
        Intent i = new Intent();
        i.putExtra(Set.EXID_WPNAME, mText);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver(true);
    }

    @Override
    protected void onStop() {
        registerBroadcastReceiver(false);
        super.onStop();
    }

    private void registerBroadcastReceiver(boolean register) {
        if (register) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(Set.BROADCASTFILTER));
        } else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

    private void handleMessage(Intent msg) {
        Bundle data = msg.getExtras();
        switch (data.getInt(Set.EXID_GESTURE, 0)) {
            //ROTATE ----------------------------------------
            case 1: //Up (top down)
                selectLetter(0, -1);
                break;
            case 2: //Rt
                selectLetter(-1, 0);
                break;
            case 3: //Dn (top up)
                selectLetter(0, 1);
                break;
            case 4: //Lt
                selectLetter(1, 0);
                break;
            case 5: //rotate CW
                moveCursor(1);
                break;
            case 6: //rotate CCW
                moveCursor(-1);
                break;
            case 10: // proximity ----------------------------
                break;
            // ACCELL ----------------------------------------
            case 11: //Y Top side up
                break;
            case 12: //X Rt side up
                break;
            case 13: //Y bottom side up
                break;
            case 14: //X Left Side up
                break;
            case 15: //Z Face down
                break;
            case 16: //Z Face up
                break;
            case 100: //Wake UP!
                break;
            default:
                return; //if 0 or other don't process
        }
    }

    private void moveCursor(int dx) {
        if(mPushedButton.getTag()==mBnText[mBnText.length-1])
            returnResult();
        else {
            mI += dx;
            mEdit.setSelection(mI);
        }
    }

    /**
     * On move gestures
     * @param dx
     * @param dy
     */
    private void selectLetter(int dx, int dy) { //zero top left
        mY = My.loopValue(mY, dy, mRows);
        mX = My.loopValue(mX, dx, mCols);
        int index = mX+mY*mCols;
        char c = pushButton(index);
        mEdit.replaceAt(mI, c);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() == mBnText[mBnText.length - 1]) { //OK
            returnResult();
        } else {
            mI = mEdit.getSelectionStart(); //in case it was manually changed
            mEdit.replaceAt(mI++, v.getTag().toString().charAt(0));
            mPushedButton = null;
        }
    }
}
