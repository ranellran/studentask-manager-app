package com.droideainfoph.studtaskmanager;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private List<MessageFile> messages;
    private String senderCode;

    private String requestAccessInfo;


    public ChatUserAdapter(List<MessageFile> messages, String senderCode) {
        this.messages = messages;
        this.senderCode = senderCode;

    }



    public void addMessage(MessageFile message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            // Inflate the layout for sender messages
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_container, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == 1) {
            // Inflate the layout for receiver messages
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_message_container, parent, false);
            return new ReceiverViewHolder(view);
        } else {
            // Inflate the layout for request access messages
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_access_grade_level_container, parent, false);
            return new RequestAccessViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageFile message = messages.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageFile message = messages.get(position);
        if (message.getSenderCode().equals(senderCode)) {
            return 0; // Sender view type
        } else if (message.getMessageText().startsWith("<<<requestaccess>>>gdwtedfiwfiowbcbhvuedtedjshydtfVTDQExrfwdgqkcopociehwubf2qojwshfbh<<<requestaccess>>>")) {
            return 2; // Request access view type
        } else {
            return 1; // Receiver view type
        }
    }

    public void setMessages(List<MessageFile> messages) {
        this.messages = messages;
    }






    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(MessageFile message);
    }









    private class SenderViewHolder extends ViewHolder {
        TextView textMessageTextView;
        TextView senderCodeTextView;

        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessageTextView = itemView.findViewById(R.id.textMessage);
            senderCodeTextView = itemView.findViewById(R.id.senderCodeTextView);
        }

        @Override
        void bindMessage(MessageFile message) {
            String messageToProcess = message.getMessageText();


            if (messageToProcess.startsWith("<<<requestaccess>>>gdwtedfiwfiowbcbhvuedtedjshydtfVTDQExrfwdgqkcopociehwubf2qojwshfbh<<<requestaccess>>>")) {

                //don't show any views
            } else {
                textMessageTextView.setText(message.getMessageText());
                senderCodeTextView.setText(message.getSenderCode());

            }

        }
    }






    private class ReceiverViewHolder extends ViewHolder {
        TextView textMessageTextView;
        TextView receiverCodeTextView;

        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessageTextView = itemView.findViewById(R.id.textMessage1);
            receiverCodeTextView = itemView.findViewById(R.id.receiverCodeTextView);

            textMessageTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String messageText = textMessageTextView.getText().toString();
                    copyTextToClipboard(itemView.getContext(), messageText);
                    return true;
                }
            });
        }

        @Override
        void bindMessage(MessageFile message) {
            textMessageTextView.setText(message.getMessageText());
            receiverCodeTextView.setText(message.getSenderCode());
        }

        private void copyTextToClipboard(Context context, String text) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("text", text);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        }
    }














    private class RequestAccessViewHolder extends ViewHolder {
        TextView requestAccessTextView;
        Button grantButton;
        Button deniedButton;
        TextView teacherCodeRequesting;

        RequestAccessViewHolder(@NonNull View itemView) {
            super(itemView);
            requestAccessTextView = itemView.findViewById(R.id.requestAccessInfo);
            grantButton = itemView.findViewById(R.id.permissionGrant);
            deniedButton = itemView.findViewById(R.id.permissionDenied);
            teacherCodeRequesting = itemView.findViewById(R.id.senderCode);
        }

        @Override
        void bindMessage(MessageFile message) {
            String messageToProcess = message.getMessageText();
            String requestAccessMessage = messageToProcess.substring("<<<requestaccess>>>gdwtedfiwfiowbcbhvuedtedjshydtfVTDQExrfwdgqkcopociehwubf2qojwshfbh<<<requestaccess>>>".length());

            teacherCodeRequesting.setText(message.getSenderCode());
            String requestingTeacher = teacherCodeRequesting.getText().toString();


            int requestAccessID = Integer.parseInt(extractValueFromTag(requestAccessMessage, "<requestingToHaveAccessID>"));
            String gradeLevelColumnID = "grade_level_" + requestAccessID;

            // Check if the request is for grade level
            if (requestAccessMessage.contains("<gradeLevel>")) {
                String teacherRequesting = extractValueFromTag(requestAccessMessage, "<teacherRequesting>");
                String gradeLevel = extractValueFromTag(requestAccessMessage, "<gradeLevelName>");

                String section = extractValueFromTag(requestAccessMessage, "<section>");

                String messageTextAccess =
                        "Teacher Requesting: " + teacherRequesting + "\n" +"\n"+
                        "Grade Level: " + gradeLevel + "\n" +"\n"+
                        "Section: " + section;

                requestAccessTextView.setText(messageTextAccess);
            }
            // Check if the request is for subject
            else if (requestAccessMessage.contains("<subjectName>") && requestAccessMessage.contains("<subjectCode>")) {

                // Extract the information from the request access message
                String teacherRequesting = extractValueFromTag(requestAccessMessage, "<teacherRequesting>");
                String subjectName = extractValueFromTag(requestAccessMessage, "<subjectName>");
                String subjectCode = extractValueFromTag(requestAccessMessage, "<subjectCode>");

                // Display the information in the appropriate container or perform other actions
                // Replace the following code with your implementation
                String messageTextAccess =
                        "Teacher Requesting: " + teacherRequesting + "\n"+"\n" +
                        "Subject Name: " + subjectName + "\n" +"\n"+
                        "Subject Code: " + subjectCode;
                requestAccessTextView.setText(messageTextAccess);
            }



            grantButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String permission = "granted";
                    DatabaseInsertTask task = new DatabaseInsertTask(itemView.getContext(), permission);
                    task.execute(gradeLevelColumnID, requestingTeacher, permission);
                }
            });

            deniedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String permission = "denied";
                    DatabaseInsertTask task = new DatabaseInsertTask(itemView.getContext(), permission);
                    task.execute(gradeLevelColumnID, requestingTeacher, permission);
                }
            });


        }

        private String extractValueFromTag(String message, String tag) {
            int startIndex = message.indexOf(tag);
            if (startIndex != -1) {
                startIndex += tag.length();
                int endIndex = message.indexOf("<", startIndex);
                if (endIndex != -1) {
                    return message.substring(startIndex, endIndex);
                }
            }
            return "";
        }
    }






    private class DatabaseInsertTask extends AsyncTask<String, Void, Void> {
        private String dbUrl = "jdbc:mysql://192.168.43.79:3300/studtask_db";
        private String dbUsername = "studtask_app";
        private String dbPassword = "studtask123";


        private String permissions;
        private Context context;

        public DatabaseInsertTask(Context context, String permissions) {
            this.context = context;
            this.permissions = permissions;
        }


        @Override
        protected Void doInBackground(String... params) {
            String gradeLevelColumn = params[0];
            String requestingTeacher = params[1];
            String permissions = params[2];

            Connection connection = null;
            PreparedStatement statement = null;

            try {
                // Establish a connection to the MySQL database
                connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

                // Prepare the SQL statement with placeholders for parameters
                String sql = "UPDATE studtask_grade_level_user_permissions SET " + gradeLevelColumn + " = ? WHERE teacher_code = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, permissions);
                statement.setString(2, requestingTeacher);

                // Execute the query
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Close the resources
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Update UI based on the completion status
            if (permissions.equals("granted")) {
                // Permission granted
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show();
            } else if (permissions.equals("denied")) {
                // Permission denied
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
            } else {
                // Error occurred during the database connection or update
                Toast.makeText(context, "Error Database Connection", Toast.LENGTH_SHORT).show();
            }
        }
    }




}

