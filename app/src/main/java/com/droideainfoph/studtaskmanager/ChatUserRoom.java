package com.droideainfoph.studtaskmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;
import com.vishnusivadas.advanced_httpurlconnection.FetchData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class ChatUserRoom extends AppCompatActivity {

    private RecyclerView messagePreviewRecyclerView;
    private EditText messageSendEditText;
    private ImageView sendMessageButton;
    private String senderCode;
    private String recipientCode;
    private ChatUserAdapter messageAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_room);


        messagePreviewRecyclerView = findViewById(R.id.messageRecyclerView);
        messageSendEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendButton);

        Intent intent = getIntent();
        if (intent != null) {
            senderCode = intent.getStringExtra("senderCode");
            recipientCode = intent.getStringExtra("recipientCode");
        }



        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();// Perform the back button action

            }
        });

        List<MessageFile> messages = new ArrayList<>();
        messageAdapter = new ChatUserAdapter(messages, senderCode);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagePreviewRecyclerView.setLayoutManager(layoutManager);
        messagePreviewRecyclerView.setAdapter(messageAdapter);

        sendMessageButton.setOnClickListener(v -> {
            String toSendMessage = String.valueOf(messageSendEditText.getText());

            if (!toSendMessage.isEmpty()) {
                SendMessageTask sendMessageTask = new SendMessageTask();
                sendMessageTask.execute(senderCode, recipientCode, toSendMessage);
            } else {
                Toast.makeText(ChatUserRoom.this, "ChatBox is Empty", Toast.LENGTH_SHORT).show();
            }
        });

        RetrieveMessagesTask retrieveMessagesTask = new RetrieveMessagesTask();
        retrieveMessagesTask.execute(senderCode, recipientCode);
        startPollingForUpdates();

    }

    private class SendMessageTask extends AsyncTask<String, Void, MessageFile> {

        @Override
        protected MessageFile doInBackground(String... params) {
            String senderCode = params[0];
            String recipientCode = params[1];
            String message = params[2];

            Log.i("PutData", senderCode);
            Log.i("PutData", recipientCode);
            Log.i("PutData", message);

            String[] field = {"senderCode", "recipientCode", "message"};
            String[] data = {senderCode, recipientCode, message};

            PutData putData = new PutData("http://192.168.43.79/chatServerConnectorFolder/chatServerConnector.php", "POST", field, data);
            if (putData.startPut() && putData.onComplete()) {
                String result = putData.getResult();
                return new MessageFile(senderCode, message);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MessageFile message) {
            if (message != null) {
                // Add the message to the adapter and notify the UI
                messageAdapter.addMessage(message);
                messageAdapter.notifyDataSetChanged();

                // Scroll to the last item
                messagePreviewRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            } else {
                Toast.makeText(ChatUserRoom.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }

            // Clear the message input
            messageSendEditText.setText("");
        }
    }


    private class RetrieveMessagesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String senderCode = params[0];
            String recipientCode = params[1];
            String url = "http://192.168.43.79/chatServerConnectorFolder/chatReceiverConnector.php?senderCode=" + senderCode + "&recipientCode=" + recipientCode;
            FetchData fetchData = new FetchData(url);
            fetchData.startFetch();
            fetchData.onComplete();
            return fetchData.getResult();
        }


        @Override
        protected void onPostExecute(String result) {
            if (!result.startsWith("Error:")) {
                List<MessageFile> messages = parseMessages(result);
                if (!messages.isEmpty()) {
                    messageAdapter.setMessages(messages);
                    messageAdapter.notifyDataSetChanged();
                    messagePreviewRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                } else {
                }
            } else {
                Toast.makeText(ChatUserRoom.this, result, Toast.LENGTH_SHORT).show();
            }
        }

        private List<MessageFile> parseMessages(String result) {
            List<MessageFile> messages = new ArrayList<>();
            try {
                if (result.startsWith("<br")) {
                    Log.e("ParseMessages", "Invalid response format: " + result);
                    return messages;
                }

                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String senderCode = jsonObject.getString("senderCode");
                    String messageText = jsonObject.getString("messageText");

                    if (messageText.startsWith("<<<requestaccess>>>gdwtedfiwfiowbcbhvuedtedjshydtfVTDQExrfwdgqkcopociehwubf2qojwshfbh<<<requestaccess>>>")) {

                        String requestAccessMessage = messageText.substring("<<<requestaccess>>>gdwtedfiwfiowbcbhvuedtedjshydtfVTDQExrfwdgqkcopociehwubf2qojwshfbh<<<requestaccess>>>".length());

                        // Check if the request is for grade level
                        if (requestAccessMessage.contains("<gradeLevel>")) {
                            messages.add(new MessageFile(senderCode, messageText));
                        }
                        // Check if the request is for subject
                        else if (requestAccessMessage.contains("<subjectName>") && requestAccessMessage.contains("<subjectCode>")) {

                            messages.add(new MessageFile(senderCode, messageText));

                        }
                    } else {
                        messages.add(new MessageFile(senderCode, messageText));
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return messages;
        }



    }
    private void startPollingForUpdates() {
        Handler handler = new Handler();
        int delay = 2000; // Interval for polling in milliseconds (e.g., 5 seconds)

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Retrieve the updated messages if the sender or recipient code matches
                RetrieveMessagesTask retrieveMessagesTask = new RetrieveMessagesTask();
                retrieveMessagesTask.execute(senderCode, recipientCode);

                // Poll for updates again after the delay
                handler.postDelayed(this, delay);
            }
        }, delay);
    }


}
