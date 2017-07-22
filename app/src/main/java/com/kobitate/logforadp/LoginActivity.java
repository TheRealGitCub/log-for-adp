package com.kobitate.logforadp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kobitate.etimejava.ETime;
import com.kobitate.etimejava.ETimePasswordException;

import java.io.IOException;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

	private Button loginButton;
	private EditText textUsername;
	private EditText textPassword;

	private ETime etime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		textUsername = (EditText) findViewById(R.id.text_username);
		textPassword = (EditText) findViewById(R.id.text_password);

		loginButton = (Button) findViewById(R.id.login_submit);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String[] credentials = {textUsername.getText().toString(), textPassword.getText().toString()};

				LoginThread login = new LoginThread(LoginActivity.this);
				login.execute(credentials);
			}
		});
	}
}

class LoginThread extends AsyncTask {

	private Context context;

	public LoginThread(Context context) {
		this.context = context;
	}

	@Override
	protected Object doInBackground(Object[] credentials) {
		ETime etime;
		try {
			etime = new ETime((String) credentials[0], (String) credentials[1]);
			return etime;
		} catch (ETimePasswordException e) {
			return e;
			//e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return e;
		}
	}

	@Override
	protected void onPostExecute(Object o) {
		super.onPostExecute(o);
		if (o instanceof ETimePasswordException) {
			Toast.makeText(context, "Incorrect Username or Password", Toast.LENGTH_LONG).show();
		} else if (o instanceof  IOException) {
			Toast.makeText(context, "Error connecting to ADP", Toast.LENGTH_LONG).show();
			((IOException) o).printStackTrace();
		} else {
			HashMap <String, String> cookies = new HashMap<>(((ETime) o).getCookies());

			Intent toMain = new Intent(context.getApplicationContext(), MainActivity.class);
			toMain.putExtra("cookies", cookies);

			context.startActivity(toMain);
		}
	}
}
