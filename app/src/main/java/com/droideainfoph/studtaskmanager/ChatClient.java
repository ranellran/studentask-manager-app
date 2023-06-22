package com.droideainfoph.studtaskmanager;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatClient {

    private static final String SERVER_URL = "http://192.168.43.79/chatServerConnector.php";

    public static void sendMessage(String senderCode, String recipientCode, String message, SendMessageCallback callback) {
        new SendMessageTask(callback).execute(senderCode, recipientCode, message);
    }

    public static void getMessages(String user1Code, String user2Code, GetMessagesCallback callback) {
        new GetMessagesTask(callback).execute(user1Code, user2Code);
    }

    interface SendMessageCallback {
        void onSendMessageResult(boolean success);
    }

    interface GetMessagesCallback {
        void onGetMessagesResult(String senders, String messages);
    }

    private static class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        private final SendMessageCallback callback;

        public SendMessageTask(SendMessageCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String senderCode = params[0];
            String recipientCode = params[1];
            String message = params[2];

            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                String postData = "action=send&senderCode=" + senderCode + "&recipientCode=" + recipientCode + "&message=" + message;
                conn.setDoOutput(true);
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postData.getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            callback.onSendMessageResult(success);
        }
    }

    private static class GetMessagesTask extends AsyncTask<String, Void, String[]> {
        private final GetMessagesCallback callback;

        public GetMessagesTask(GetMessagesCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String user1Code = params[0];
            String user2Code = params[1];

            try {
                URL url = new URL(SERVER_URL + "?action=get_messages&user1Code=" + user1Code + "&user2Code=" + user2Code);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();

                    String response = stringBuilder.toString();
                    String[] parts = response.split(",");
                    String senders = "";
                    String messages = "";
                    for (int i = 0, j = 0; i < parts.length; i += 2, j++) {
                        senders += parts[i];
                        messages += parts[i + 1];
                    }
                    return new String[]{senders, messages};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                callback.onGetMessagesResult(result[0], result[1]);
            } else {
                callback.onGetMessagesResult("", "");
            }
        }
    }
}
