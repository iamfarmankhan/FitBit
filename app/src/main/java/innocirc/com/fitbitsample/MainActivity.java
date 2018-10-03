package innocirc.com.fitbitsample;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitbit.authentication.AuthenticationHandler;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.AuthenticationResult;
import com.fitbit.authentication.Scope;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements AuthenticationHandler {

    Button mBtnLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnLogIn = (Button)findViewById(R.id.btn_login);
        mBtnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthenticationManager.login(MainActivity.this);
            }
        });
        if (AuthenticationManager.isLoggedIn()) {
            Toast.makeText(this, "User already logged in", Toast.LENGTH_SHORT).show();
            Log.d("Farman",String.valueOf(AuthenticationManager.getCurrentAccessToken().getAccessToken()));
            Log.d("Farman",String.valueOf(AuthenticationManager.getCurrentAccessToken().getExpirationDate()));
        }
        else
        {
            mBtnLogIn.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         *  4. When the Login UI finishes, it will invoke the `onActivityResult` of this activity.
         *  We call `AuthenticationManager.onActivityResult` and set ourselves as a login listener
         *  (via AuthenticationHandler) to check to see if this result was a login result. If the
         *  result code matches login, the AuthenticationManager will process the login request,
         *  and invoke our `onAuthFinished` method.
         *
         *  If the result code was not a login result code, then `onActivityResult` will return
         *  false, and we can handle other onActivityResult result codes.
         *
         */

        if (!AuthenticationManager.onActivityResult(requestCode, resultCode, data, MainActivity.this)) {
            // Handle other activity results, if needed
        }

    }

    public void onAuthFinished(AuthenticationResult authenticationResult) {

        if (authenticationResult.isSuccessful()) {
            Log.d("Farman",String.valueOf(AuthenticationManager.getCurrentAccessToken().getScopes().toString()));
            Log.d("Farman",String.valueOf(AuthenticationManager.getCurrentAccessToken().getExpirationDate()));
            Toast.makeText(this, String.valueOf(authenticationResult.getAccessToken()), Toast.LENGTH_SHORT).show();

        } else {
            displayAuthError(authenticationResult);
        }
    }

    private void displayAuthError(AuthenticationResult authenticationResult) {
        String message = "";

        switch (authenticationResult.getStatus()) {
            case dismissed:
                message = getString(R.string.login_dismissed);
                break;
            case error:
                message = authenticationResult.getErrorMessage();
                break;
            case missing_required_scopes:
                Set<Scope> missingScopes = authenticationResult.getMissingScopes();
                String missingScopesText = TextUtils.join(", ", missingScopes);
                message = getString(R.string.missing_scopes_error) + missingScopesText;
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.login_title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create()
                .show();
    }



    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, TestJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1 * 1000); // wait at least
        builder.setOverrideDeadline(3 * 1000); // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }



}
