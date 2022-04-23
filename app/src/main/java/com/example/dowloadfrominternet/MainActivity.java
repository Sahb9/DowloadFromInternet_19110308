package com.example.dowloadfrominternet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Get Web Page Source Code");

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.protocol_list, R.layout.spinner_item);
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("...");
        dialog.setCancelable(false);

        binding.getSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.URL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "URL Trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.show();
                new Thread(new Runnable() {
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                try {
                                    binding.source.setText(getSourceFromURL(binding.spinner.getSelectedItem().toString()
                                            + binding.URL.getText()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        dialog.dismiss();
                    }
                }).start();

                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    private String getSourceFromURL(String urlText) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            System.out.println(urlText);
            URL url = new URL(urlText);
            URLConnection con = null;
            con = url.openConnection();
            String encoding = con.getContentEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), encoding));
            StringBuilder sb = new StringBuilder();
            try {
                String s;
                while ((s = r.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                r.close();
            }
            return sb.toString();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            if (ex.getMessage().contains("Cleartext HTTP traffic") &&
                    ex.getMessage().contains("not permitted")) {
                return "Cleartext HTTP traffic not permitted.\nPlease change to HTTPS!";
            } else if (ex.getMessage().contains("Unable to resolve host")) {
                return "Địa chỉ URL ko hợp lệ .\nHãy thử cái khác!";
            }
            return "Xảy ra lỗi!";
        }
    }
}