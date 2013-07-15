package org.martus.android.orchid;

import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
import com.subgraph.orchid.xmlrpc.OrchidXmlRpcTransportFactory;

public class MainActivity extends Activity
{

	private TorClient tor;
	private static final String APP_LABEL = "orchid-android";
	private TextView screenText;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	    screenText = (TextView)findViewById(R.id.mainText);

	    createRealTorClient();
    }

	private void createRealTorClient()
	{
		tor = new TorClient();

		tor.getConfig().setDataDirectory(this.getCacheDir());

	}

	private void onInitialized()  {
		screenText.setText("Tor Initialized");
		testTor();
	}

	private void onTestCompleted(String result)
	{
		screenText.setText(result);
	}

	private void testTor()
	{
		Log.i(APP_LABEL, "about to test Tor");
		final AsyncTask<TorClient, Object, String> testTask = new TestTorTask();
		testTask.execute(tor);
	}

	public void initOrchid(View view) {
		final AsyncTask<TorClient, Object, String> initializeTask = new InitializeTask();
		Log.i(APP_LABEL, "about to start initialization");
		initializeTask.execute(tor);
    }

	private class TestTorTask extends AsyncTask<TorClient, Object, String>
	{
		protected String doInBackground(TorClient... clients)
		{
			final TorClient torClient = clients[0];

			try
			{
				XmlRpcClient client = new XmlRpcClient();
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				client.setConfig(config);

				// Note: There is also a three argument constructor available which allows passing an SSLContext instance
				//       that you have previously configured with a customized trust manager.
				//
				//    JTorXmlRpcTransportFactory(XmlRpcClient client, TorClient torClient, SSLContext sslContext)

				client.setTransportFactory(new OrchidXmlRpcTransportFactory(client,torClient));

				// Just some random xmlrpc service on the internet that accepts unauthenticated requests
				config.setServerURL(new URL("http://xmlrpc.texy.info/"));
				Log.i(APP_LABEL, "Result from texy1.version is "+ client.execute("texy1.version", new Object[0]));
				return (String)client.execute("texy1.version", new Object[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "failure";
		}

		@Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
	        onTestCompleted(result);
        }
	}

	private class InitializeTask extends AsyncTask<TorClient, Object, String> implements  TorInitializationListener
	{
        @Override
        protected String doInBackground(TorClient... clients) {

            final TorClient client = clients[0];

	        client.addInitializationListener(this);
	        tor.start();
            tor.enableSocksListener();
	        try
	        {
		        tor.waitUntilReady();
	        } catch (InterruptedException e)
	        {
		        Log.e(APP_LABEL, "error", e);
	        }
	        return "Tor Initialized";
        }

		public void initializationProgress(String message, int percent)
        {
            onProgressUpdate(percent, message);
        }

        public void initializationCompleted()
        {
            onProgressUpdate(101, "Tor initialization complete");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
	        onInitialized();
        }

		@Override
	    protected void onProgressUpdate(Object... progress) {
	        super.onProgressUpdate(progress);
			Log.i(APP_LABEL, "Tor initialization: " + progress[0] + "% - " + progress[1]);
	    }
    }
}
