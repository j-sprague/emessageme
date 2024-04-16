package edu.uncc.emessageme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import edu.uncc.emessageme.auth.LoginFragment;
import edu.uncc.emessageme.auth.RegisterFragment;
import edu.uncc.emessageme.models.Message;
import edu.uncc.emessageme.models.User;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, RegisterFragment.SignUpListener, MessagesFragment.MessagesListener, ComposeFragment.ComposeListener, SelectRecipientFragment.RecipientListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mAuth.getCurrentUser() == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new LoginFragment())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new MessagesFragment())
                    .commit();
        }
    }

    @Override
    public void createNewAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new RegisterFragment())
                .commit();
    }

    @Override
    public void login() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }
    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }

    @Override
    public void gotoCompose() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new ComposeFragment(), "compose-fragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoRecipient() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new SelectRecipientFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void authCompleted() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new MessagesFragment())
                .commit();
    }

    @Override
    public void cancel() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onRecipientSelected(User user) {
        ComposeFragment fragment = (ComposeFragment) getSupportFragmentManager().findFragmentByTag("compose-fragment");
        if (fragment != null) {
            fragment.selectedRecipient = user;
        }
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void gotoMessage(Message msg) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, MessageFragment.newInstance(msg))
                .addToBackStack(null)
                .commit();
    }

}