package edu.uncc.emessageme;

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import edu.uncc.emessageme.databinding.FragmentResponseBinding;
import edu.uncc.emessageme.models.Message;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResponseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResponseFragment extends Fragment {

    private static final String ARG_PARAM_MSG = "ARG_PARAM_MSG";

    private FirebaseAuth mAuth;

    private Message mMessage;

    public ResponseFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ResponseFragment newInstance(Message msg) {
        ResponseFragment fragment = new ResponseFragment();
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

    FragmentResponseBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentResponseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mAuth = FirebaseAuth.getInstance();

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancel();
            }
        });

        binding.textViewFrom.setText("From: " + mMessage.getReceiverName());
        binding.textViewTo.setText("To: " + mMessage.getSenderName());
        binding.textViewSubject.setText("RE: " + mMessage.getSubject());

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.editTextTextMultiLine.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please type a body", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference senderRef = db.collection("users")
                            .document(mAuth.getCurrentUser().getUid())
                            .collection("messages")
                            .document();
                    DocumentReference recipientRef = db.collection("users")
                            .document(mMessage.getSenderId())
                            .collection("messages")
                            .document(senderRef.getId());
                    HashMap<String, Object> data = new HashMap<>();

                    data.put("senderId", mAuth.getCurrentUser().getUid());
                    data.put("senderName",mAuth.getCurrentUser().getDisplayName());
                    data.put("receiverId",mMessage.getSenderId());
                    data.put("receiverName",mMessage.getSenderName());
                    data.put("subject","RE: " + mMessage.getSubject());
                    data.put("text",binding.editTextTextMultiLine.getText().toString());
                    data.put("replyTo",mMessage.getText());
                    data.put("messageId",senderRef.getId());
                    data.put("sentAt", FieldValue.serverTimestamp());
                    data.put("opened",false);

                    senderRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                recipientRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mListener.backtoInbox();
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });

    }


    ResponseListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (ResponseListener) context;
    }

    public interface ResponseListener {
        void cancel();
        void backtoInbox();
    }
}