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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.uncc.emessageme.databinding.FragmentAddressBookBinding;
import edu.uncc.emessageme.databinding.MessageRowItemBinding;
import edu.uncc.emessageme.databinding.UserRowItemBinding;
import edu.uncc.emessageme.models.Message;
import edu.uncc.emessageme.models.User;


public class AddressBookFragment extends Fragment {

    public AddressBookFragment() {
        // Required empty public constructor
    }

    FragmentAddressBookBinding binding;
    private FirebaseAuth mAuth;
    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<String> blocked = new ArrayList<>();
    UserAdapter adapter;

    ListenerRegistration listenerRegistration;

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
        binding = FragmentAddressBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        adapter = new UserAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);


        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancel();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    mUsers.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        if (!user.getUid().equals(mAuth.getCurrentUser().getUid())) {
                            mUsers.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        listenerRegistration = db.collection("users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    error.printStackTrace();
                    return;
                }
                blocked = (ArrayList<String>) value.get("blocked");
                adapter.notifyDataSetChanged();
//                for (User user : mUsers) {
//                    if (blocked.contains(user.getUid())) {
//                        user.setBlocked(true);
//                    } else {
//                        user.setBlocked(false);
//                    }
//                }
            }
        });


    }

    public interface AddressBookListener {
        void cancel();
    }

    AddressBookListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (AddressBookListener) context;
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            UserRowItemBinding itemBinding = UserRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new UserViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = mUsers.get(position);
            holder.setupUI(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            UserRowItemBinding itemBinding;
            User mUser;
            public UserViewHolder(UserRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }


            private void setupUI(User user){
                this.mUser = user;
                itemBinding.textViewName.setText(mUser.getName());
//                if (user.isBlocked()) {
//                    itemBinding.button.setText("Unblock");
//                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                if (blocked.contains(user.getUid())) {
                    itemBinding.button.setText("Unblock");
                    itemBinding.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            db.collection("users").document(mAuth.getCurrentUser().getUid()).update("blocked", FieldValue.arrayRemove(mUser.getUid()));
                        }
                    });
                } else {
                    itemBinding.button.setText("Block");
                    itemBinding.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            db.collection("users").document(mAuth.getCurrentUser().getUid()).update("blocked", FieldValue.arrayUnion(mUser.getUid()));
                        }
                    });
                }
            }
        }
    }
}