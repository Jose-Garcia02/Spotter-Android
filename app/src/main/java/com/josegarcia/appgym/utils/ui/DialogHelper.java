package com.josegarcia.appgym.utils.ui;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.josegarcia.appgym.R;

public class DialogHelper {

    public interface DialogCallback {
        void onPositive();
    }

    public static void showConfirmationDialog(Context context, String title, String message, String positiveText, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_generic_confirmation, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        Button btnPositive = view.findViewById(R.id.btnDialogPositive);
        Button btnNegative = view.findViewById(R.id.btnDialogNegative);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(positiveText);

        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onPositive();
        });

        btnNegative.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
