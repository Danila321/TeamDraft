package com.example.teamdraft.ui.home.workSpace.cardActivity.attachments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.example.teamdraft.LoadingDialog;
import com.example.teamdraft.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class AddAttachmentDialog extends DialogFragment {
    private String boardId, itemId, cardId;
    Context context;
    Uri fileUri = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static AddAttachmentDialog newInstance(String boardId, String itemId, String cardId) {
        AddAttachmentDialog dialog = new AddAttachmentDialog();
        Bundle args = new Bundle();
        args.putString("boardId", boardId);
        args.putString("itemId", itemId);
        args.putString("cardId", cardId);
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
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.workspace_card_attachments_dialog, null);
        builder.setView(dialogView);

        EditText editText = dialogView.findViewById(R.id.AttachmentDialogEditText);
        ConstraintLayout addFile = dialogView.findViewById(R.id.AttachmentDialogAdd);
        Button button = dialogView.findViewById(R.id.AttachmentDialogButton);

        TextView fileName = dialogView.findViewById(R.id.AttachmentDialogFileName);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            fileUri = data.getData();
                            fileName.setText(getFileName(getActivity().getContentResolver(), fileUri));
                        }
                    }
                });
        addFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            activityResultLauncher.launch(intent);
        });

        button.setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()) {
                editText.setError("Введите название");
            } else if (fileUri == null) {
                editText.setError("Выберите файл");
            } else {
                uploadFile(fileUri, editText.getText().toString());
                dismiss();
            }
        });

        return builder.create();
    }

    void uploadFile(Uri fileUri, String fileName) {
        //Показываем загрузочный диалог
        LoadingDialog loadingDialog = new LoadingDialog(getActivity(), "Добавляем вложение...");
        loadingDialog.startDialog();

        //Загружаем файл в БД
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("boards").child(boardId).child("attachments")
                .child(System.currentTimeMillis() + "_attachment");
        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    ContentResolver contentResolver = context.getContentResolver();
                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                    String fileType = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(fileUri));
                    if (fileType == null) {
                        fileType = "";
                    }

                    String attachmentID = UUID.randomUUID().toString();
                    @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    ItemAttachment attachment = new ItemAttachment(attachmentID, uri.toString(), fileType, fileName, date);

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("boards").child(boardId).child("items").child(itemId).child("cards").child(cardId).child("attachments").child(attachmentID).setValue(attachment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            loadingDialog.dismissDialog();
                        }
                    });
                })).addOnFailureListener(e -> {
                });
    }

    public static String getFileName(ContentResolver contentResolver, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int pixelsWidth = getResources().getDimensionPixelSize(R.dimen.dialog_add_attachment_width);
            getDialog().getWindow().setLayout(pixelsWidth, WindowManager.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}