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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
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
import edu.uncc.emessageme.databinding.MessageRowItemBinding;
import edu.uncc.emessageme.models.Message;


public class MessagesFragment extends Fragment {


    private FirebaseAuth mAuth;

    public MessagesFragment() {
        // Required empty public constructor
    }


    ArrayList<Message> mMessages = new ArrayList<>();
    ListenerRegistration listenerRegistration;
    FragmentMessagesBinding binding;
    MessagesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding.userWelcome.setText("Welcome " + mAuth.getCurrentUser().getDisplayName());

        adapter = new MessagesAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        listenerRegistration = db.collection("users")
                        .document(mAuth.getCurrentUser().getUid())
                                .collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            error.printStackTrace();
                            return;
                        }
                        mMessages.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Message msg = document.toObject(Message.class);
                            mMessages.add(msg);
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
                });


        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logout();
            }
        });
        binding.composeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoCompose();
            }
        });
        binding.addressImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoAddress();
            }
        });
    }

    MessagesListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MessagesListener) context;
    }

    interface MessagesListener {
        void logout();
        void gotoCompose();
        void gotoMessage(Message msg);
        void gotoAddress();
    }


    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MessageRowItemBinding itemBinding = MessageRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new MessageViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
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