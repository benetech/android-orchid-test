package org.martus.android.orchid;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;

public class MainActivity extends Activity
{

	private TorClient tor;
	private static final String APP_LABEL = "orchid-android";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	    createRealTorClient();
	    tor.start();
    }

	private void createRealTorClient()
	{
		tor = new TorClient();
		//File torDirectory = new File(this.getCacheDir().getParent(), "shared_prefs");
        //torDirectory.mkdirs();
		//tor.getConfig().setDataDirectory(this.getExternalFilesDir(null));
		tor.getConfig().setDataDirectory(this.getCacheDir());


		class TorInitializationHandler implements TorInitializationListener
		{
			public void initializationProgress(String message, int percent)
			{
				updateProgress(message, percent);
			}

			public void initializationCompleted()
			{
				updateProgressComplete();
			}

		}

		tor.addInitializationListener(new TorInitializationHandler());
	}

	private void updateProgress(String message, int percent)
	{
		Log.i(APP_LABEL, "Tor initialization: " + percent + "% - " + message);
	}

	private void updateProgressComplete()
	{
		Log.i(APP_LABEL,"Tor initialization complete");
	}
}
