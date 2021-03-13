package com.holzhausen.mediastore.callbacks;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.holzhausen.mediastore.databases.IDBHelper;
import com.holzhausen.mediastore.model.MultimediaItem;

public class DeleteItemSnackBarCallback extends Snackbar.Callback {

    private final IDBHelper<MultimediaItem> helper;

    private final MultimediaItem itemToBeDeleted;

    public DeleteItemSnackBarCallback(IDBHelper<MultimediaItem> helper,
                                      MultimediaItem itemToBeDeleted) {
        this.helper = helper;
        this.itemToBeDeleted = itemToBeDeleted;
    }

    @Override
    public void onDismissed(Snackbar transientBottomBar, int event) {
        super.onDismissed(transientBottomBar, event);

        if(event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION){
            helper.removeItem(itemToBeDeleted);
        }

    }
}
