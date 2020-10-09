package com.example.workwithstorages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.UUID;

import static android.os.storage.StorageManager.ACTION_MANAGE_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnReader, btnWriter;//for reading and writing datas from storages
    private EditText etContent, etFilename;//for content and filename
    private static final String FOLDER_NAME = "notes";//folder name
    private boolean isEncryptedSavingEnabled = false;//for saving data encryptly
    private String content, filename;//the text of etContent and etFilename
    private static final long FREE_SPACE = 1024 * 1024 * 5L;
    private SharedPreferences preferences;//for getting preference from settings
    private File extDir;//the dir for saving data in external storage
    private File intDir;//the dir for saving encrypted data in internal storage
    //fields for encrypted saving
    private MasterKey masterKey;
    private EncryptedFile encryptedFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //init widgets
        btnReader = findViewById(R.id.btnReader);
        btnWriter = findViewById(R.id.btnWriter);
        etContent = findViewById(R.id.etContent);
        etFilename = findViewById(R.id.etFilename);
        btnWriter.setOnClickListener(this);
        btnReader.setOnClickListener(this);
        //init widgets
        //init isEncryptedSavingEnabled
        if (preferences.contains("encryptedNotes")){
            isEncryptedSavingEnabled = preferences.getBoolean("encryptedNotes", false);
        }
        //init isEncryptedSavingEnabled
        //init extDir
        extDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FOLDER_NAME);
        if (!extDir.exists()){
            extDir.mkdir();
        }
        //init extDir
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
        content = etContent.getText().toString();
        filename = etFilename.getText().toString().trim();
        switch (v.getId()){
            case R.id.btnWriter:
                if (!isExternalStorageWritable()){
                    writeData();
                } else {
                    Toast.makeText(this, "The external storage is read-only.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnReader:
                etContent.setText(readData());
                break;
        }
    }

    private void writeData() {
        if (!isEncryptedSavingEnabled){
            if (!extDir.exists()){
                extDir.mkdir();
            }
            filename = filename.replace(" ", "_");
            File dir = new File(extDir, filename);
            try (FileOutputStream outputStream = new FileOutputStream(dir)){
                outputStream.write(content.getBytes());
                Toast.makeText(this, "The note saved successfully.", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "FileNotFoundException in writeData()", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "IOException in writeData()", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (hasFreeSpace()){
                try {
                    intDir = new File(getFilesDir(), FOLDER_NAME);
                    if (!intDir.exists())
                        intDir.mkdir();
                    File dir = new File(intDir, filename);
                    masterKey = new MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
                    encryptedFile = new EncryptedFile.Builder(this, dir, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build();
                    OutputStream outputStream = encryptedFile.openFileOutput();
                    outputStream.write(content.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    Toast.makeText(this, "The note saved successfully.", Toast.LENGTH_SHORT).show();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "GeneralSecurityException in writeData()", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "IOException in writeData()", Toast.LENGTH_SHORT).show();
                }
            } else {
                new Intent().setAction(ACTION_MANAGE_STORAGE);
            }

        }
    }

    private boolean isExternalStorageWritable(){
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
    }

    private String readData(){
        if (isEncryptedSavingEnabled){
            try {
                File dir = new File(intDir, filename);
                if (!dir.exists())
                    etFilename.setError("The file does not exist.");
                masterKey = new MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
                encryptedFile = new EncryptedFile.Builder(this,
                        dir,
                        masterKey,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build();
                InputStream inputStream = encryptedFile.openFileInput();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                int bytes;
                while ((bytes = inputStream.read()) != -1){
                    byteArray.write(bytes);
                }
                return byteArray.toString();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Toast.makeText(this, "GeneralSecurityException", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
            }
        } else {
            File dir = new File(extDir, filename);
            if (!dir.exists())
                etFilename.setError("This file does not exist");
            try (FileInputStream inputStream = new FileInputStream(dir);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "FileNotFoundException in readData()", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "IOException in readData()", Toast.LENGTH_SHORT).show();
            }
        }
        return "";
    }

    private boolean hasFreeSpace() {
        if (Build.VERSION.SDK_INT >= 26){
            try {
                StorageManager manager = getSystemService(StorageManager.class);
                UUID uuid = manager.getUuidForPath(getFilesDir());
                long availableStorage = manager.getAllocatableBytes(uuid);
                if (availableStorage >= FREE_SPACE){
                    manager.allocateBytes(uuid, FREE_SPACE);
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                Toast.makeText(this, "IOException in hasFreeSpace()", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return true;
    }
}