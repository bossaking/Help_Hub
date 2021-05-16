package com.example.help_hub.AlertDialogues;

public class Dialog {

    public interface onDialogDismissedListener {
        void onDismissed();
    }

    public onDialogDismissedListener onDialogDismissedListener;

    public void setOnDialogDismissedListener(onDialogDismissedListener dismissedListener) {
        onDialogDismissedListener = dismissedListener;
    }

    public void dismissDialog() {
        if (onDialogDismissedListener != null) onDialogDismissedListener.onDismissed();
    }
}
