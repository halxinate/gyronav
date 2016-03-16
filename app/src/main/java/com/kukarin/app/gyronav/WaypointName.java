package com.kukarin.app.gyronav;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class WaypointName extends AppCompatActivity {
    EditText mEdit;
    String mText="";
    private int mX;
    private int mY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_name);
        mEdit = (EditText)findViewById(R.id.kbedit);
        mX=0;
        mY=0;
    }

    public void onKbdClick(View v) {
        int vid = v.getId();
        switch (vid) {
            case R.id.kbok1:
            case R.id.kbok2:

                Intent i = new Intent();
                i.putExtra(Set.EXID_WPNAME, mText);
                setResult(Activity.RESULT_OK, i);
                finish();

                break;
            case R.id.kbcancel1:
            case R.id.kbcancel2:
                finish();
                break;
            default: //manual input from kbd
                String c = ((Button) v).getText().toString();
                int i0 = mEdit.getSelectionStart();
                int i1 = mEdit.getSelectionEnd();
                String s = mText.substring(0,i0)+c+mText.substring(i1);
                mText = s;
                mEdit.setText(mText);
                //move marker
                mEdit.setSelection(++i0);
                break;
        }
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
                selectLetter(0,-1);
                break;
            case 2: //Rt
                selectLetter(1,0);
                break;
            case 3: //Dn (top up)
                selectLetter(0,1);
                break;
            case 4: //Lt
                selectLetter(-1,0);
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
        // TODO: 3/15/2016 shift and highlight, adding currently selected on kbd letter
    }

    //                        .....!.....!.....!.....!.....!.....!.....!
    private final String kbd="123456abcde7fghij8klmno9pqrst0uvwxyz!+-*?_";

    private void selectLetter(int dx, int dy) { //zero top left
        int xsz;
        if((mY==0 && dy==1) || (mY==7 && dy==-1)) { //jump from OK to letters
            if(mX==1) mX = 6; //jump to right side if it was on Cancel
        }
        mY = My.loopValue(mY, dy, 8);
        if(mY==0 || mY==7) {
            xsz = 2;
            if(mX>1) mX=0; //jump from letters row, reset X to OK
        }
        else {
            xsz = 6;
        }
        mX = My.loopValue(mX, dx, xsz);
        if(xsz==2) {//controls

        }
        String c = "" + kbd.charAt(mX+mY*6);
        c = c.toUpperCase();

        // TODO: 3/15/2016 Highlight the button.
        // But it will be beneficial to add them programmatically
    }
}
