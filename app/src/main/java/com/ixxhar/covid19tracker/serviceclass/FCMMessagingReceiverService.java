package com.ixxhar.covid19tracker.serviceclass;

import android.app.Notification;
import android.app.NotificationManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ixxhar.covid19tracker.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static com.ixxhar.covid19tracker.App.CHANNEL_ID;

public class FCMMessagingReceiverService extends FirebaseMessagingService {
    private static final String TAG = "FCMMessagingReceiverSer";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            //String imgURI = remoteMessage.getNotification().getImageUrl().toString();

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused)//assign received image app logo
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(101, notification);
        }

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().toString());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG, "onDeletedMessages: ");
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: ");
        //upload this token to the app server
    }
}
