package com.kukarin.app.gyronav;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

// TODO: 3/16/2016  caps input, last letter not going to the right, initial letter not set, on letter index change kbd not updating
public class WaypointName extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "WaypointName";
    private final int ID0 = 9000;
    EditGyro mEdit;
    private String SPACE = "_";
    private final String[] mBnText = {"1", "2", "3", "4", "5", "6", "A", "B", "C", "D", "E", "7", "F", "G", "H", "I", "J", "8", "K", "L", "M", "N", "O", "9", "P", "Q", "R", "S", "T", "0", "U", "V", "W", "X", "Y", "Z", "_", "?", "!", "-", "\"", "OK"};
    private int OK_BUTTON = mBnText.length-1;
    //private int DEL_BUTTON = mBnText.length-2;
    private View mPushedButton = null;
    private int mRows = 7;
    private int mCols = 6;
    private LinearLayout mKBlayout;
    private int mBnIndex;
    private int mCursor;

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

        String text = getIntent().getStringExtra(Set.EXID_WPNAME).trim().toUpperCase();
        text = text.replaceAll(" ", SPACE);
        mEdit = (EditGyro) findViewById(R.id.kbedit);
        mEdit.setText(text);
        mCursor = 0; //selection start
        mEdit.setSelection(mCursor);
        mBnIndex = pushPutton(text.charAt(mCursor)); //highlight first letter
        if(mBnIndex<0) { //not found select OK
            mBnIndex = OK_BUTTON;
            pushButton(mBnIndex);
        }
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
        View b;
        for(String c : mBnText){
            if(c.charAt(0)==s) { //TAG found among kb buttons
                b = mKBlayout.findViewById(index + ID0);
                if(b!=null) mPushedButton = b; //in case not found the old button will be re-pushed
                if(mPushedButton!=null) //the old one could be null
                    mPushedButton.setBackgroundResource(R.drawable.bnbackon);
                mBnIndex = index;
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
        mBnIndex = index;
        return mPushedButton.getTag().toString().charAt(0);
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
                onSelectButton(0, -1);
                break;
            case 2: //Rt
                onSelectButton(-1, 0);
                break;
            case 3: //Dn (top up)
                onSelectButton(0, 1);
                break;
            case 4: //Lt
                onSelectButton(1, 0);
                break;
            case 5: //rotate CW
                onMoveCursor(1);
                break;
            case 6: //rotate CCW
                onMoveCursor(-1);
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

    /**
     * Move text cursor in the edit field (rotate Z gesture response)
     * @param dx
     */
    private void onMoveCursor(int dx) {
        My.vib(1);
        if(mPushedButton.getTag()==mBnText[OK_BUTTON])
            returnResult();
        else {
            String text = mEdit.getText().toString();
            mCursor += dx;
            if(mCursor<0) { //leftmost hit - extend it left
                mCursor = 0;
                text = SPACE+text;
            }
            else if(mCursor==text.length())
                text += SPACE; //extend it right

            //truncate hanging underscores on the right if moving cursor left
            int sz = text.length();
            if(mCursor==sz-2 && text.charAt(sz-1)==SPACE.charAt(0)){
                text = text.substring(0,sz-1);
            }
            else if(mCursor==1 && text.charAt(0)==SPACE.charAt(0)){
                text = text.substring(1);
                mCursor = 0;
            }
            mEdit.setText(text);
            mEdit.setSelection(mCursor);
            pushPutton(text.charAt(mCursor));
        }
    }

    /**
     * Gesture for changing the letter
     * @param dx
     * @param dy
     */
    private void onSelectButton(int dx, int dy) { //zero top left
        My.vib(1);
        int mX = mBnIndex % mCols;
        int mY = mBnIndex / mCols;

        mY = My.loopValue(mY, dy, mRows);
        mX = My.loopValue(mX, dx, mCols);
        int index = mX+mY*mCols;
        char c = pushButton(index);
        if(index != OK_BUTTON)
            mEdit.replaceAt(mCursor, c);
    }

    /**
     * Manully pushed button
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getTag() == mBnText[mBnText.length - 1]) { //OK
            returnResult();
        } else {
            mBnIndex = v.getId()-ID0;
            onSelectButton(0,0);
            onMoveCursor(1); //auto advance cursor on manual input
        }
    }

    private void returnResult() {
        Intent i = new Intent();
        String name = mEdit.getText().toString().replaceAll("_", " ");
        i.putExtra(Set.EXID_WPNAME, name.trim());
        setResult(Activity.RESULT_OK, i);
        finish();
    }
}
