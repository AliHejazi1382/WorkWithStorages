package com.example.workwithstorages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnReader, btnWriter;//for reading and writing datas from storages
    private EditText etContent, etFilename;//for content and filename
    private static final String FOLDER_NAME = "notes";//folder name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnReader = findViewById(R.id.btnReader);
        btnWriter = findViewById(R.id.btnWriter);
        etContent = findViewById(R.id.etContent);
        etFilename = findViewById(R.id.etFilename);
        btnWriter.setOnClickListener(this);
        btnReader.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnWriter:

                break;
            case R.id.btnReader:

                break;
        }
    }
}