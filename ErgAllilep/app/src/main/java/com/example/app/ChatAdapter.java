package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    private List<ChatMessage> messages;
    private LayoutInflater inflater;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = messages.get(position);
        ViewHolder holder;

        if (message.isUser()) {
            convertView = inflater.inflate(R.layout.item_user_message, parent, false);
            holder = new ViewHolder();
            holder.messageText = convertView.findViewById(R.id.textMessage);
            convertView.setTag(holder);
        } else {
            convertView = inflater.inflate(R.layout.item_bot_message, parent, false);
            holder = new ViewHolder();
            holder.messageText = convertView.findViewById(R.id.textMessage);
            holder.avatarImage = convertView.findViewById(R.id.avatarImage);
            holder.xIcon = convertView.findViewById(R.id.x_icon);

            // ✅ Αυτό κρατάει την υποστήριξη link για email/τηλέφωνο
            holder.messageText.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            holder.messageText.setMovementMethod(LinkMovementMethod.getInstance());

            convertView.setTag(holder);
        }

        // ✅ Χειρισμός clickable διεύθυνσης
        if (!message.isUser() && message.getMessage().contains("Βουλής 13")) {
            CharSequence clickable = makeAddressClickable(context, message.getMessage());
            holder.messageText.setText(clickable);
            holder.messageText.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.messageText.setText(message.getMessage());
        }

        // ✅ Χειρισμός εμφάνισης X icon ανάλογα με flag
        if (!message.isUser() && holder.xIcon != null) {
            if (message.shouldShowCancelIcon()) {
                holder.xIcon.setVisibility(View.VISIBLE);
                holder.xIcon.setOnClickListener(v -> {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).requestCancelFromAdapter();
                    }
                });
            } else {
                holder.xIcon.setVisibility(View.GONE);
                holder.xIcon.setOnClickListener(null); // για αποφυγή leaks/bugs
            }
        }

        return convertView;
    }

    private CharSequence makeAddressClickable(Context context, String message) {
        SpannableString spannable = new SpannableString(message);
        int start = message.indexOf("Βουλής 13");
        int end = start + "Βουλής 13, Αθήνα".length();

        if (start >= 0) {
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=Βουλής+13,+Αθήνα");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                }
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#80ddff")); // 👈 Επιλογή χρώματος
                    ds.setUnderlineText(true);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }


    static class ViewHolder {
        TextView messageText;
        ImageView avatarImage;
        ImageView xIcon;
    }

}
