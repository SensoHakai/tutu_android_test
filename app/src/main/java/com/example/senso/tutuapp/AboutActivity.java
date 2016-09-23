package com.example.senso.tutuapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Senso on 23.09.2016.
 */

public class AboutActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        Intent newActivity1 = new Intent();
        setResult(RESULT_OK,newActivity1);
    }}
