package com.example.teamdraft.ui.homeui.workSpace.cardActivity.checkList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamdraft.R;
import com.example.teamdraft.ui.homeui.workSpace.cardActivity.OnChange;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeleteChecklistItemDialog extends DialogFragment {
    private String boardId, itemId, cardId, taskId;
    OnChange onChange;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onChange = (OnChange) context;
    }

    public static DeleteChecklistItemDialog newInstance(String boardId, String itemId, String cardId, String taskId) {
        DeleteChecklistItemDialog dialog = new DeleteChecklistItemDialog();
        Bundle args = new Bundle();
        args.putString("boardId", boardId);
        args.putString("itemId", itemId);
        args.putString("cardId", cardId);
        args.putString("taskId", taskId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardId = getArguments().getString("boardId");
            itemId = getArguments().getString("itemId");
            cardId = getArguments().getString("cardId");
            taskId = getArguments().getString("taskId");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_text, null);
        builder.setView(dialogView);

        TextView title = dialogView.findViewById(R.id.TextDialogTitle);
        TextView description = dialogView.findViewById(R.id.TextDialogDescription);

        title.setText("Удаление задачи");
        description.setText("Вы уверены что хотите удалить\nзадачу? Это действие является\nбезвозвратным!");

        Button buttonCancel = dialogView.findViewById(R.id.TextDialogButtonCancel);
        Button buttonDelete = dialogView.findViewById(R.id.TextDialogButtonDelete);

        buttonCancel.setOnClickListener(view -> dismiss());
        buttonDelete.setOnClickListener(view -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("boards").child(boardId).child("items").child(itemId).child("cards").child(cardId).child("checklist").child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().removeValue();
                    onChange.onChangeCheckList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            dismiss();
        });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL);
        }
        return null;
    }
}