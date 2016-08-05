package se.mogumogu.presencedetector.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

public final class BasicDialogFragment extends DialogFragment {

    private static final String DIALOG_LAYOUT_ID = "se.mogumogu.presencedetector.DIALOG_LAYOUT_ID";
    private static final String DIALOG_TITLE_ID = "se.mogumogu.presencedetector.DIALOG_TITLE_ID";
    private static final String POSITIVE_BUTTON_TEXT_ID = "se.mogumogu.presencedetector.POSITIVE_BUTTON_TEXT_ID";

    private BasicDialogListener listener;
    private int dialogLayoutId;
    private int dialogTitleId;
    private int positiveButtonTextId;

    public static DialogFragment newInstance(final int dialogLayoutId, final int dialogTitleId, final int positiveButtonTextId) {

        final DialogFragment dialogFragment = new BasicDialogFragment();

        final Bundle arguments = new Bundle();
        arguments.putInt(DIALOG_LAYOUT_ID, dialogLayoutId);
        arguments.putInt(DIALOG_TITLE_ID, dialogTitleId);
        arguments.putInt(POSITIVE_BUTTON_TEXT_ID, positiveButtonTextId);
        dialogFragment.setArguments(arguments);

        return dialogFragment;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            listener = (BasicDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BasicDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dialogLayoutId = getArguments().getInt(DIALOG_LAYOUT_ID);
        dialogTitleId = getArguments().getInt(DIALOG_TITLE_ID);
        positiveButtonTextId = getArguments().getInt(POSITIVE_BUTTON_TEXT_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final View view = View.inflate(getContext(), dialogLayoutId, null);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(dialogTitleId);

        dialogBuilder.setView(view)
                .setPositiveButton(positiveButtonTextId, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int id) {

                        listener.onDialogPositiveClick(BasicDialogFragment.this, view);
                    }
                })

                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int id) {

                        listener.onDialogNegativeClick(BasicDialogFragment.this);
                    }
                });

        return dialogBuilder.create();
    }

    public interface BasicDialogListener {

        void onDialogPositiveClick(final DialogFragment dialog, final View view);

        void onDialogNegativeClick(final DialogFragment dialog);
    }
}
