package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brodev.socialapp.view.base.BaseCursorAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.chat.model.QBDialog;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.base.BaseCursorAdapter;
import com.brodev.socialapp.view.imageview.MaskedImageView;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.brodev.socialapp.utils.Consts;
import com.brodev.socialapp.utils.DateUtils;
import com.brodev.socialapp.utils.ImageUtils;
import com.brodev.socialapp.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate_core.db.tables.MessageTable;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class BaseDialogMessagesAdapter extends BaseCursorAdapter implements ReceiveFileFromBitmapTask.ReceiveFileListener, StickyListHeadersAdapter {

    private final int colorMaxValue = 255;
    private final float colorAlpha = 0.8f;
    protected ScrollMessagesListener scrollMessagesListener;
    protected ImageUtils imageUtils;
    protected QBDialog dialog;
    private Random random;
    private Map<Integer, Integer> colorsMap;

    public BaseDialogMessagesAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        random = new Random();
        colorsMap = new HashMap<Integer, Integer>();
        imageUtils = new ImageUtils((android.app.Activity) context);
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return ConstsCore.MESSAGES_TYPE_COUNT;
    }

    protected boolean isOwnMessage(int senderId) {
        return senderId == currentUser.getId();
    }

    protected void displayAttachImage(String attachUrl, final ViewHolder viewHolder) {
        ImageLoader.getInstance().displayImage(attachUrl, viewHolder.attachImageView,
                Consts.UIL_DEFAULT_DISPLAY_OPTIONS, new ImageLoadingListener(viewHolder),
                new SimpleImageLoadingProgressListener(viewHolder));
    }

    protected int getTextColor(Integer senderId) {
        if (colorsMap.get(senderId) != null) {
            return colorsMap.get(senderId);
        } else {
            int colorValue = getRandomColor();
            colorsMap.put(senderId, colorValue);
            return colorsMap.get(senderId);
        }
    }

    protected int getMessageStatusIconId(boolean isDelivered, boolean isRead) {
        int iconResourceId;
        if (isDelivered && isRead) {
            iconResourceId = R.drawable.ic_status_mes_sent_received;
        } else if (isDelivered) {
            iconResourceId = R.drawable.ic_status_mes_sent;
        } else {
            iconResourceId = 0;
        }
        return iconResourceId;
    }

    protected void setMessageStatus(View view, ViewHolder viewHolder, int resourceId,
            boolean messageDelivered, boolean messageRead) {
        viewHolder.messageDeliveryStatusImageView = (ImageView) view.findViewById(resourceId);
        viewHolder.messageDeliveryStatusImageView.setImageResource(getMessageStatusIconId(messageDelivered, messageRead));
    }

    protected void resetUI(ViewHolder viewHolder) {
        setViewVisibility(viewHolder.attachMessageRelativeLayout, View.GONE);
        setViewVisibility(viewHolder.progressRelativeLayout, View.GONE);
        setViewVisibility(viewHolder.textMessageView, View.GONE);
    }

    private int getRandomColor() {
        float[] hsv = new float[3];
        int color = Color.argb(colorMaxValue, random.nextInt(colorMaxValue), random.nextInt(colorMaxValue),
                random.nextInt(colorMaxValue));
        Color.colorToHSV(color, hsv);
        hsv[2] *= colorAlpha;
        color = Color.HSVToColor(hsv);
        return color;
    }

    private int getItemViewType(Cursor cursor) {
        int senderId = cursor.getInt(cursor.getColumnIndex(MessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            return ConstsCore.OWN_DIALOG_MESSAGE_TYPE;
        } else {
            return ConstsCore.OPPONENT_DIALOG_MESSAGE_TYPE;
        }
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
        imageUtils.showFullImage(context, absolutePath);
    }

    protected void setViewVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        Cursor cursor = null;

        if (getCursor().getCount() > ConstsCore.ZERO_INT_VALUE) {
            cursor = (Cursor) getItem(position);
        }

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = layoutInflater.inflate(R.layout.list_item_chat_sticky_header_date, parent, false);
            holder.headerTextView = (TextView) convertView.findViewById(R.id.header_date_textview);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (cursor != null) {
            long time = cursor.getLong(cursor.getColumnIndex(MessageTable.Cols.TIME));
            holder.headerTextView.setText(DateUtils.longToMessageListHeaderDate(time));
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        String timeString;

        if (getCursor().getCount() > ConstsCore.ZERO_INT_VALUE) {
            Cursor cursor = (Cursor) getItem(position);
            long time = cursor.getLong(cursor.getColumnIndex(MessageTable.Cols.TIME));
            timeString = DateUtils.longToMessageListHeaderDate(time);
        } else {
            return ConstsCore.ZERO_INT_VALUE;
        }

        if (!TextUtils.isEmpty(timeString)) {
            return timeString.subSequence(ConstsCore.ZERO_INT_VALUE, timeString.length() - 1).charAt(
                    ConstsCore.ZERO_INT_VALUE);
        } else {
            return ConstsCore.ZERO_INT_VALUE;
        }
    }

    private class HeaderViewHolder {

        TextView headerTextView;
    }

    public class ImageLoadingListener extends SimpleImageLoadingListener {

        private ViewHolder viewHolder;
        private Bitmap loadedImageBitmap;

        public ImageLoadingListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
            viewHolder.verticalProgressBar.setProgress(ConstsCore.ZERO_INT_VALUE);
            viewHolder.centeredProgressBar.setProgress(ConstsCore.ZERO_INT_VALUE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            updateUIAfterLoading();
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedBitmap) {
            initMaskedImageView(loadedBitmap);
        }

        private void initMaskedImageView(Bitmap loadedBitmap) {
            loadedImageBitmap = loadedBitmap;
            viewHolder.attachImageView.setOnClickListener(receiveImageFileOnClickListener());
            viewHolder.attachImageView.setImageBitmap(loadedImageBitmap);
            setViewVisibility(viewHolder.attachMessageRelativeLayout, View.VISIBLE);
            setViewVisibility(viewHolder.attachImageView, View.VISIBLE);
            updateUIAfterLoading();
            scrollMessagesListener.onScrollToBottom();
        }

        private void updateUIAfterLoading() {
            if (viewHolder.progressRelativeLayout != null) {
                setViewVisibility(viewHolder.progressRelativeLayout, View.GONE);
            }
        }

        private View.OnClickListener receiveImageFileOnClickListener() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(context,
                            R.anim.chat_attached_file_click));
                    new ReceiveFileFromBitmapTask(BaseDialogMessagesAdapter.this).execute(imageUtils,
                            loadedImageBitmap, false);
                }
            };
        }
    }

    public class SimpleImageLoadingProgressListener implements ImageLoadingProgressListener {

        private ViewHolder viewHolder;

        public SimpleImageLoadingProgressListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            viewHolder.verticalProgressBar.setProgress(Math.round(100.0f * current / total));
        }
    }

    public class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView nameTextView;
        public View textMessageView;
        public ImageView messageDeliveryStatusImageView;
        public RelativeLayout progressRelativeLayout;
        public RelativeLayout attachMessageRelativeLayout;
        public TextView messageTextView;
        public MaskedImageView attachImageView;
        public TextView timeTextMessageTextView;
        public TextView timeAttachMessageTextView;
        public ProgressBar verticalProgressBar;
        public ProgressBar centeredProgressBar;
        public ImageView acceptFriendImageView;
        public View dividerView;
        public ImageView rejectFriendImageView;
    }
}