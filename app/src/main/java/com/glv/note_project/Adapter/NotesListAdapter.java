package com.glv.note_project.Adapter;



import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.glv.note_project.Model.Notes;
import com.glv.note_project.NotesClickListener;
import com.glv.note_project.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesListAdapter extends RecyclerView.Adapter <NotesViewHolder>{

    Context context;
    List<Notes> list;
    NotesClickListener listner;

    public NotesListAdapter(Context context, List<Notes> list, NotesClickListener listner) {
        this.context = context;
        this.list = list;
        this.listner = listner;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.textView_title.setText(list.get(position).getTitle());
        holder.textView_title.setSelected(true);

        holder.textView_notes.setText(list.get(position).getNotes());

        holder.textView_date.setText(list.get(position).getData());
        holder.textView_date.setSelected(true);


        if (list.get(position).isPinned()) {
            holder.imageView_pin.setImageResource(R.drawable.baseline_push_pin_24);
        }
        else {
            holder.imageView_pin.setImageResource(0);
        }


        if (list.get(position).isChek()) {
            holder.check_box.setVisibility(View.VISIBLE);
            holder.check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        list.get(position).setCheck_state(true);
                        Log.d("MainActivity", "ChecckBox was not clicked!" + (list.get(position).isCheck_state()));
                    } else {
                        list.get(position).setCheck_state(false);
                        Log.d("MainActivity", "ChecckBox was not clicked!" + (list.get(position).isCheck_state()));
                    }
                }
            });
        }
        else {
            holder.check_box.setVisibility(View.GONE);
        }






        holder.notes_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.onClick(list.get(holder.getAdapterPosition()));
            }
        });

        holder.notes_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listner.onLongClick(list.get(holder.getAdapterPosition()), holder.notes_container);
                return true;
            }
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterlist(List<Notes> filterlist){
        list = filterlist;
        notifyDataSetChanged();
    }
}


class NotesViewHolder extends RecyclerView.ViewHolder {


    CardView notes_container;
    TextView textView_title, textView_notes, textView_date;
    ImageView imageView_pin;
    CheckBox check_box;

    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);

        notes_container = itemView.findViewById(R.id.notes_container);
        textView_title = itemView.findViewById(R.id.textView_title);
        textView_notes = itemView.findViewById(R.id.textView_notes);
        textView_date = itemView.findViewById(R.id.textView_date);
        imageView_pin = itemView.findViewById(R.id.imageView_pin);
        check_box = itemView.findViewById(R.id.check_Box);



    }
}