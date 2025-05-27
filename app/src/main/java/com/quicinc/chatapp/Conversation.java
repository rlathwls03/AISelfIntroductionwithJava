// ---------------------------------------------------------------------
// Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// ---------------------------------------------------------------------
package com.quicinc.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Conversation extends AppCompatActivity {

    ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>(1000);

    private static final String cWelcomeMessage = "Hi! How can I help you?";
    public static final String cConversationActivityKeyHtpConfig = "htp_config_path";
    public static final String cConversationActivityKeyModelName = "model_dir_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat);
        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        Message_RecyclerViewAdapter adapter = new Message_RecyclerViewAdapter(this, messages);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton sendUserMsgButton = (ImageButton) findViewById(R.id.send_button);
        TextView userMsg = (TextView) findViewById(R.id.user_input);

        try {
            // Make QNN libraries discoverable
            String nativeLibPath = getApplicationContext().getApplicationInfo().nativeLibraryDir;
            Os.setenv("ADSP_LIBRARY_PATH", nativeLibPath, true);
            Os.setenv("LD_LIBRARY_PATH", nativeLibPath, true);

            // Get information from MainActivity regarding
            //  - Model to run
            //  - HTP config to use
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                Log.e("ChatApp", "Error getting additional info from bundle.");
                Toast.makeText(this, "Unexpected error observed. Exiting app.", Toast.LENGTH_LONG).show();
                finish();
            }

            String htpExtensionsDir = bundle.getString(cConversationActivityKeyHtpConfig);
            String modelName = bundle.getString(cConversationActivityKeyModelName);
            String externalCacheDir = this.getExternalCacheDir().getAbsolutePath().toString();
            String modelDir = Paths.get(externalCacheDir, "models", modelName).toString();

            // Load Model
            GenieWrapper.Instance = new GenieWrapper(modelDir, htpExtensionsDir);
            Log.i("ChatApp", modelName + " Loaded.");

            messages.add(new ChatMessage(cWelcomeMessage, MessageSender.BOT));

            // ✅ 모델 로드 완료 후 HomeActivity로 이동
            Intent intent = new Intent(Conversation.this, HomeActivity.class);
            intent.putExtra(cConversationActivityKeyHtpConfig, htpExtensionsDir);
            intent.putExtra(cConversationActivityKeyModelName, modelName);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("ChatApp", "Error during conversation with Chatbot: " + e.toString());
            Toast.makeText(this, "Unexpected error observed. Exiting app.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
