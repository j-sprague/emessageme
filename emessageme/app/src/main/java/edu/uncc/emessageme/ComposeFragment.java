package edu.uncc.emessageme;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import edu.uncc.emessageme.databinding.FragmentComposeBinding;
import edu.uncc.emessageme.models.User;

public class ComposeFragment extends Fragment {

    private FirebaseAuth mAuth;

    public User selectedRecipient;

    public ComposeFragment() {
        // Required empty public constructor
    }

    FragmentComposeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentComposeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        if (selectedRecipient != null) {
            binding.textViewRecipient.setText("To: " + selectedRecipient.getName());
        }

        binding.textViewSender.setText("From: " + mAuth.getCurrentUser().getDisplayName());
        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancel();
            }
        });

        binding.selectRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoRecipient();
            }
        });

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedRecipient == null) {
                    Toast.makeText(getActivity(), "Please select a recipient", Toast.LENGTH_SHORT).show();
                } else if (binding.subject.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please type a subject", Toast.LENGTH_SHORT).show();
                } else if (binding.editTextTextMultiLine.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please type a body", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference senderRef = db.collection("users")
                            .document(mAuth.getCurrentUser().getUid())
                            .collection("messages")
                            .document();
                    DocumentReference recipientRef = db.collection("users")
                            .document(selectedRecipient.getUid())
                            .collection("messages")
                            .document(senderRef.getId());
                    HashMap<String, Object> data = new HashMap<>();

                    data.put("senderId", mAuth.getCurrentUser().getUid());
                    data.put("senderName",mAuth.getCurrentUser().getDisplayName());
                    data.put("receiverId",selectedRecipient.getUid());
                    data.put("receiverName",selectedRecipient.getName());
                    data.put("subject",binding.subject.getText().toString());
                    data.put("text",binding.editTextTextMultiLine.getText().toString());
                    data.put("replyTo","");
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
                                        mListener.cancel();
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });

    }

    ComposeListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (ComposeListener) context;
    }

    interface ComposeListener {
        void cancel();
        void gotoRecipient();
    }
}