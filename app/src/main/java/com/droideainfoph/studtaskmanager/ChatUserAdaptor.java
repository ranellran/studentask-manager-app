package com.droideainfoph.studtaskmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatUserAdaptor extends RecyclerView.Adapter<ChatUserAdaptor.UserViewHolder> {
    private List<String> userList;
    private List<String> uniqueCodes;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(String uniqueCode);
    }

    public ChatUserAdaptor(List<String> userList, List<String> uniqueCodes, OnItemClickListener clickListener) {
        this.userList = userList;
        this.uniqueCodes = uniqueCodes;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_adaptor_item_view, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (userList != null && uniqueCodes != null && position < userList.size() && position < uniqueCodes.size()) {
            String userName = userList.get(position);
            String uniqueCode = uniqueCodes.get(position);
            holder.bind(userName, uniqueCode);
        }
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setUniqueCodes(List<String> uniqueCodes) {
        this.uniqueCodes = uniqueCodes;
        notifyDataSetChanged();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
        }

        public void bind(final String userName, final String uniqueCode) {
            // Set the username
            userNameTextView.setText(userName);

            // Set the onClickListener for the username
            userNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Pass the selected user's unique code to the click listener
                    clickListener.onItemClick(uniqueCode);
                }
            });
        }
    }
}
