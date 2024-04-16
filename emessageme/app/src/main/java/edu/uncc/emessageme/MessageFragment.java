package edu.uncc.emessageme;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

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

        binding.textViewFrom.setText("From: " + mMessage.getReceiverName());
        binding.textViewTo.setText("To: " + mMessage.getSenderName());
        binding.textViewBody.setText(mMessage.getText());
        binding.textViewSubject.setText(mMessage.getSubject());
        if (mMessage.getReceiverId().equals(mAuth.getCurrentUser().getUid())) {
            binding.textViewRead.setText("");
            // IMPLEMENT READ
        }
        else {
            binding.imageReply.setVisibility(View.INVISIBLE);
        }
    }
}