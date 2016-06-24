package se.mogumogu.presencedetection.dialogfragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import se.mogumogu.presencedetection.R;

public class RegistrationDialogFragment extends DialogFragment {

    private RegistrationDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            listener = (RegistrationDialogListener) context;

        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement RegistrationDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        final View view = View.inflate(getContext(), R.layout.dialog_fragment_registration, null);

        dialogBuilder.setView(view)
                .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {

                        listener.onDialogPositiveClick(RegistrationDialogFragment.this, view);
                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {

                        listener.onDialogNegativeClick(RegistrationDialogFragment.this);
                    }
                });

        return dialogBuilder.create();
    }

    public interface RegistrationDialogListener {

        void onDialogPositiveClick(DialogFragment dialog, View view);

        void onDialogNegativeClick(DialogFragment dialog);
    }
}
