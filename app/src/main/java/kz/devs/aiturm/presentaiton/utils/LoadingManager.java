package kz.devs.aiturm.presentaiton.utils;

import android.app.Dialog;
import android.content.Context;

import kz.devs.aiturm.R;

public class LoadingManager {
    public static Dialog getLoadingDialog(Context context, boolean isCancelable) {
        var dialog = new Dialog(context);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCancelable(isCancelable);
        dialog.create();
        return dialog;
    }
}
