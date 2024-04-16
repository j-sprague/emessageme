package edu.uncc.emessageme;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.uncc.emessageme.databinding.FragmentSelectRecipientBinding;
import edu.uncc.emessageme.models.User;


public class SelectRecipientFragment extends Fragment {

    public SelectRecipientFragment() {
        // Required empty public constructor
    }

    ArrayList<User> users = new ArrayList<>();
    ArrayAdapter<User> adapter;
    private ArrayList<String> blocked = new ArrayList<>();
    private FirebaseAuth mAuth;
    FragmentSelectRecipientBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSelectRecipientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, users);
        binding.listView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                blocked = (ArrayList<String>) task.getResult().get("blocked");

                db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            users.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                if (!user.getUid().equals(mAuth.getCurrentUser().getUid()) && !user.getBlocked().contains(mAuth.getCurrentUser().getUid()) && !blocked.contains(user.getUid())) {
                                    users.add(user);
                                }
                            }
//                    User current = new User(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName());
//                    users.remove(users.indexOf(current));
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            Log.d("demo", "onComplete: Error getting user documents: ", task.getException());
                        }
                    }
                });


            }
        });



        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User sel = users.get(position);
                mListener.onRecipientSelected(sel);
            }
        });
    }

    RecipientListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (RecipientListener) context;
    }

    public interface RecipientListener{
        void onRecipientSelected(User user);
    }
}