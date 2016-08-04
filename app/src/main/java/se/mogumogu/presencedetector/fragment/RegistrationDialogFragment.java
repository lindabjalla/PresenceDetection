package se.mogumogu.presencedetector.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import se.mogumogu.presencedetector.R;

public final class RegistrationDialogFragment extends DialogFragment {

    private RegistrationDialogListener listener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            listener = (RegistrationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RegistrationDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final View view = View.inflate(getContext(), R.layout.dialog_fragment_registration, null);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.dialog_fragment_registration_title);

        dialogBuilder.setView(view)
                .setPositiveButton(R.string.activate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int id) {

                        listener.onDialogPositiveClick(RegistrationDialogFragment.this, view);
                    }
                })

                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int id) {

                        listener.onDialogNegativeClick(RegistrationDialogFragment.this);
                    }
                });

        return dialogBuilder.create();
    }

    public interface RegistrationDialogListener {

        void onDialogPositiveClick(final DialogFragment dialog, final View view);

        void onDialogNegativeClick(final DialogFragment dialog);
    }
}
