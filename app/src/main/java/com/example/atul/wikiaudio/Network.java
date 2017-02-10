package com.example.atul.wikiaudio;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Network {
	private static Map<String, String> cookies = new HashMap<>(12);

	public static String uploadFile(String title, InputStream in_stream, String wpEditToken){
		try {

			StringBuilder buffer = new StringBuilder(300000);
			buffer.append("&filename=");
			buffer.append(URLEncoder.encode(title, "UTF-8"));
			buffer.append("&file=");
			byte[] b = new byte[in_stream.available()];
			in_stream.read(b);
			buffer.append(new String(b));
			buffer.append("&token=");
			buffer.append(URLEncoder.encode(wpEditToken, "UTF-8"));

			String url = "https://en.wikipedia.org/w/api.php?action=upload&format=json";
			String text = buffer.toString();
			Log.d("request", text);
			URLConnection connection = makeConnection(url);
			setCookies(connection);
			connection.addRequestProperty("Content-Type", "multipart/form-data");
			connection.addRequestProperty("Content-Disposition", "attachment; filename=record.WAV");
			connection.setDoOutput(true);
			connection.connect();

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.write(text);

			StringBuilder temp = new StringBuilder(100000);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(),
					"UTF-8"));
			grabCookies(connection);
			String line;
            while ((line = in.readLine()) != null) {
                temp.append(line);
                temp.append("\n");
            }
			return temp.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}

	public static String getEditToken() {
		try {
			String responseBody = fetch(
					"https://en.wikipedia.org/w/api.php?action=query&format=json&meta=tokens");

            String editToken;
            JSONObject reader;
			JSONObject queryJSONObject;
			JSONObject tokensJSONObject;

			try {
				reader = new JSONObject(responseBody);
				queryJSONObject = reader.getJSONObject("query");
				tokensJSONObject = queryJSONObject.getJSONObject("tokens");
                editToken = tokensJSONObject.getString("csrftoken");
				Log.d("edit_token", editToken);
				return (editToken);

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
		return "WRONG";
	}

	protected static URLConnection makeConnection(String url) throws IOException {
		return new URL(url).openConnection();
	}

	protected static void setCookies(URLConnection u)
	{
		StringBuilder cookie = new StringBuilder(100);
		for (Map.Entry<String, String> entry : cookies.entrySet())
		{
			cookie.append(entry.getKey());
			cookie.append("=");
			cookie.append(entry.getValue());
			cookie.append("; ");
		}
		u.setRequestProperty("Cookie", cookie.toString());
	}

	private static void grabCookies(URLConnection u)
	{
		String headerName;
		for (int i = 1; (headerName = u.getHeaderFieldKey(i)) != null; i++)
			if (headerName.equals("Set-Cookie"))
			{
				String cookie = u.getHeaderField(i);
				cookie = cookie.substring(0, cookie.indexOf(';'));
				String name = cookie.substring(0, cookie.indexOf('='));
				String value = cookie.substring(cookie.indexOf('=') + 1, cookie.length());
				// these cookies were pruned, but are still sent for some reason?
				// TODO: when these cookies are no longer sent, remove this test
				if (!value.equals("deleted"))
					cookies.put(name, value);
			}
	}

	protected static String post(String url, String text) throws IOException
	{
		URLConnection connection = makeConnection(url);
		setCookies(connection);
		connection.setRequestProperty( "Content-Type", "multipart/form-data" );
		connection.setDoOutput(true);
		connection.connect();
		try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"))
		{
			out.write(text);
		}
		StringBuilder temp = new StringBuilder(100000);
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(connection.getInputStream(),
						"UTF-8")))
		{
			grabCookies(connection);
			String line;
			while ((line = in.readLine()) != null)
			{
				temp.append(line);
				temp.append("\n");
			}
		}
		return temp.toString();
	}

	protected static String fetch(String url) throws IOException {
		// connect
		URLConnection connection = makeConnection(url);
		setCookies(connection);
		connection.connect();
		grabCookies(connection);

		// check lag

		String temp;
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(connection.getInputStream(),
						"UTF-8"))) {
			String line;
			StringBuilder text = new StringBuilder(100000);
			while ((line = in.readLine()) != null) {
				text.append(line);
				text.append("\n");
			}
			temp = text.toString();
		}
		return temp;
	}
}