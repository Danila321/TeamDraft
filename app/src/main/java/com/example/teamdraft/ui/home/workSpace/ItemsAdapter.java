package com.example.teamdraft.ui.home.workSpace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.teamdraft.R;
import com.example.teamdraft.ui.home.workSpace.dialogs.card.AddCardDialog;
import com.example.teamdraft.ui.home.workSpace.dialogs.item.DeleteItemDialog;
import com.example.teamdraft.ui.home.workSpace.dialogs.item.EditItemDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemsAdapter extends ArrayAdapter<Item> {
    private final Context context;
    private final String boardId;
    FragmentManager fragmentManager;
    boolean isAdmin;

    public ItemsAdapter(@NonNull Context context, ArrayList<Item> items, String boardId, FragmentManager fragmentManager, boolean isAdmin) {
        super(context, R.layout.workspace_custom_item, items);
        this.context = context;
        this.boardId = boardId;
        this.fragmentManager = fragmentManager;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.workspace_custom_item, null);
        }

        Item item = getItem(position);

        TextView itemNameView = convertView.findViewById(R.id.itemName);
        itemNameView.setText(item.getName());

        //Настраиваем ListView карточек
        ArrayList<Card> cards = new ArrayList<>();
        ListView listView = convertView.findViewById(R.id.cardsListView);
        CardsAdapter adapter = new CardsAdapter(context, cards, fragmentManager, boardId, item.getId());
        listView.setAdapter(adapter);
        updateCards(item.getId(), adapter, listView, cards);

        Button addCard = convertView.findViewById(R.id.buttonAddCard);
        ImageButton edit = convertView.findViewById(R.id.imageButtonEditCard);
        ImageButton delete = convertView.findViewById(R.id.imageButtonDeleteCard);

        if (isAdmin){
            //Кнопка добавления карточки
            addCard.setOnClickListener(view -> {
                AddCardDialog dialog = AddCardDialog.newInstance(boardId, item.getId());
                dialog.show(fragmentManager, "addCard");
            });

            //Кнопка изменения названия

            edit.setOnClickListener(v -> {
                EditItemDialog dialog = EditItemDialog.newInstance(boardId, item.getId(), item.getName());
                dialog.show(fragmentManager, "editItemName");
            });

            //Кнопка удаления
            delete.setOnClickListener(v -> {
                DeleteItemDialog dialog = DeleteItemDialog.newInstance(boardId, item.getId());
                dialog.show(fragmentManager, "deleteItem");
            });
        } else {
            addCard.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }


        return convertView;
    }

    private void updateCards(String itemId, CardsAdapter adapter, ListView list, ArrayList<Card> cards) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("boards").child(boardId).child("items").child(itemId).child("cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Добавляем названия карточек в список
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    Card card = cardSnapshot.getValue(Card.class);
                    cards.add(card);
                }

                adapter.notifyDataSetChanged();

                int height = adapter.getCount() * 230;
                ViewGroup.LayoutParams params = list.getLayoutParams();
                params.height = height;
                list.setLayoutParams(params);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}