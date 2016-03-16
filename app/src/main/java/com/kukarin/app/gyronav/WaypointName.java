package com.kukarin.app.gyronav;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class WaypointName extends AppCompatActivity {
    EditText mEdit;
    String mText="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint_name);
        mEdit = (EditText)findViewById(R.id.kbedit);
    }

    public void onKbdClick(View v) {
        int vid = v.getId();
        switch (vid) {
            case R.id.kbok1:
            case R.id.kbok2:

                finishActivity(Set.RESRET_WPNAME);
                break;
            case R.id.kbcancel1:
            case R.id.kbcancel2:
                finishActivity(Set.RESRET_CANCEL);
                break;
            default:
                String c = (String) ((Button) v).getText();
                int i0 = mEdit.getSelectionStart();
                int i1 = mEdit.getSelectionEnd();
                String s = mText.substring(0,i0)+c+mText.substring(i1);
                mText = s;
                mEdit.setText(mText);
                mEdit.setSelection(i0+1, i0+2);
                break;
        }
    }
}
