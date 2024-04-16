package edu.uncc.emessageme;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

import edu.uncc.emessageme.databinding.FragmentMessageBinding;
import edu.uncc.emessageme.models.Message;


public class MessageFragment extends Fragment {

    private static final String ARG_PARAM_MSG = "ARG_PARAM_MSG";

    private FirebaseAuth mAuth;

    private Message mMessage;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(Message msg) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_MSG, msg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMessage = (Message) getArguments().getSerializable(ARG_PARAM_MSG);
        }
    }



    FragmentMessageBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancel();
            }
        });

        binding.imageTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("messages").document(mMessage.getMessageId())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                mListener.cancel();
                            }
                        });
            }
        });

        binding.imageReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoResponse(mMessage);
            }
        });

        binding.textViewFrom.setText("From: " + mMessage.getSenderName());
        binding.textViewTo.setText("To: " + mMessage.getReceiverName());
        if (!mMessage.getReplyTo().equals("")) {
            binding.buttonOriginal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(mMessage.getReplyTo())
                            .setTitle("Original Message");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } else {
            binding.buttonOriginal.setVisibility(View.INVISIBLE);
        }
        binding.textViewBody.setText(mMessage.getText());
        binding.textViewBody.setMovementMethod(new ScrollingMovementMethod());
        binding.textViewSubject.setText(mMessage.getSubject());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            String date = sdf.format(mMessage.getSentAt().toDate());
            binding.textViewDatetime.setText(date);
        } catch (Exception e) {
            Log.d("demo", "setupUI: " + e.toString());
        }
        if (mMessage.getReceiverId().equals(mAuth.getCurrentUser().getUid())) {
            binding.textViewRead.setText("");
            // IMPLEMENT READ
            if (!mMessage.isOpened()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(mMessage.getSenderId()).collection("messages")
                        .document(mMessage.getMessageId()).update("opened",true);
                db.collection("users").document(mMessage.getReceiverId()).collection("messages")
                        .document(mMessage.getMessageId()).update("opened",true);

            }
        }
        else {
            binding.imageReply.setVisibility(View.INVISIBLE);
            if (mMessage.isOpened()) {
                binding.textViewRead.setText(R.string.read);
            } else {
                binding.textViewRead.setText(R.string.unread);
            }
        }
    }


    MessageListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MessageListener) context;
    }

    public interface MessageListener {
        void cancel();
        void gotoResponse(Message msg);
    }
}