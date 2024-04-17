package edu.uncc.emessageme;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.uncc.emessageme.databinding.FragmentMessagesBinding;
import edu.uncc.emessageme.databinding.FragmentSearchBinding;
import edu.uncc.emessageme.databinding.MessageRowItemBinding;
import edu.uncc.emessageme.models.Message;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    FragmentSearchBinding binding;

    private FirebaseAuth mAuth;

    ArrayList<Message> mMessages = new ArrayList<>();
    ListenerRegistration listenerRegistration;
    MessagesAdapter adapter;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        adapter = new MessagesAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        search("", true, true);


        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancel();
            }
        });

        binding.buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.receivedBox.isChecked() && !binding.sentBox.isChecked()) {
                    Toast.makeText(getActivity(), "Please select received and/or messages", Toast.LENGTH_SHORT).show();
                } else {
                    search(binding.editTextText.getText().toString(),
                            binding.sentBox.isChecked(),
                            binding.receivedBox.isChecked());
                }
            }
        });
    }

    private void search(String term, boolean sent, boolean received) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (term.isEmpty()) {
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .collection("messages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                mMessages.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Message msg = document.toObject(Message.class);
                                    if (msg.getReceiverId().equals(mAuth.getUid()) && received) {
                                        mMessages.add(msg);
                                    }
                                    else if (msg.getSenderId().equals(mAuth.getUid()) && sent) {
                                        mMessages.add(msg);
                                    }
                                }
                                Collections.sort(mMessages, new Comparator<Message>() {
                                    @Override
                                    public int compare(Message o1, Message o2) {
                                        if (o1.getSentAt() == null || o2.getSentAt() == null)
                                            return 0;
                                        return o2.getSentAt().compareTo(o1.getSentAt());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        } else {
//            Toast.makeText(getActivity(), term, Toast.LENGTH_SHORT).show();
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .collection("messages")
                    .whereGreaterThanOrEqualTo("subject", term)
                    .whereLessThanOrEqualTo("subject", term + "\uF7FF")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                mMessages.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Message msg = document.toObject(Message.class);
                                    if (msg.getReceiverId().equals(mAuth.getUid()) && received) {
                                        mMessages.add(msg);
                                    }
                                    else if (msg.getSenderId().equals(mAuth.getUid()) && sent) {
                                        mMessages.add(msg);
                                    }
                                }
                                Collections.sort(mMessages, new Comparator<Message>() {
                                    @Override
                                    public int compare(Message o1, Message o2) {
                                        if (o1.getSentAt() == null || o2.getSentAt() == null)
                                            return 0;
                                        return o2.getSentAt().compareTo(o1.getSentAt());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }

    }

    interface SearchListener {
        void cancel();
        void gotoMessage(Message msg);
    }

    SearchListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (SearchListener) context;
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
        @NonNull
        @Override
        public MessagesAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MessageRowItemBinding itemBinding = MessageRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new MessagesAdapter.MessageViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MessagesAdapter.MessageViewHolder holder, int position) {
            Message msg = mMessages.get(position);
            holder.setupUI(msg);
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            MessageRowItemBinding itemBinding;
            Message mMessage;
            public MessageViewHolder(MessageRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                this.itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.gotoMessage(mMessage);
                    }
                });
            }


            private void setupUI(Message msg){
                this.mMessage = msg;
                itemBinding.textViewName.setText(msg.getSubject());
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String date = sdf.format(mMessage.getSentAt().toDate());
                    itemBinding.textViewDate.setText(date);
                } catch (Exception e) {
                    Log.d("demo", "setupUI: " + e.toString());
                }

                if (msg.getReceiverId().equals(mAuth.getCurrentUser().getUid())) {
                    itemBinding.textViewUserName.setText(msg.getSenderName());
                    if (msg.isOpened()) {
                        itemBinding.imageView4.setImageResource(R.drawable.read);
                    } else {
                        itemBinding.imageView4.setImageResource(R.drawable.unread);
                    }
                } else {
                    itemBinding.textViewUserName.setText(msg.getReceiverName());
                    itemBinding.imageView4.setImageResource(R.drawable.sent);
                }

            }
        }
    }

}