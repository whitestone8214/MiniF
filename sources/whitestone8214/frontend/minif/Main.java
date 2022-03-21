package whitestone8214.frontend.minif;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main extends Activity {
	// Non-visible things
	public Activity _activity;
	public String[] _permissions;
	public String _nameCodeForSelectedApp;
	public String _versionCodeForSelectedApp;
	public String _siteForSelectedApp;
	
	// Widgets
	public LinearLayout _overall;
	public LinearLayout _overall1;
	public LinearLayout _apps;
	
	
	// Necessary callback(s)
	public void onCreate(Bundle data) {
		// Prepare
		super.onCreate(data);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		_activity = this;
		
		// Request necessary permission(s)
		_permissions = new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE};
		requestPermissions(_permissions, 1);
	}
	public void onRequestPermissionsResult(int id, String[] permissions, int[] answers) {
		// Quit the app if permission is not granted
		if (answers[0] != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, "App can't do anything without necessary permission(s)", 1).show();
			finish();
		}
		
		// Overall
		_overall = new LinearLayout(_activity);
		_overall.setOrientation(LinearLayout.VERTICAL);
		setContentView(_overall);
		
		_overall1 = new LinearLayout(_activity);
		_overall1.setOrientation(LinearLayout.VERTICAL);
		_overall.addView(_overall1);
		
		// "Load" button
		Button _load = new Button(_activity);
		_load.setText("Load");
		_load.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			try {
				// Extract index-v1.json
				String _file = _activity.getExternalFilesDir(null).getPath() + "/index-v1.jar";
				ZipEntry _file1 = new ZipFile(_file).getEntry("index-v1.json");
				JsonReader _reader = new JsonReader(new InputStreamReader(new ZipFile(_file).getInputStream(_file1), "UTF-8"));
				
				_apps.removeAllViews();
				int _indent = 0;
				int _nth = 0;
				String _key = null;
				int _inList = -1;
				int _oneApp = -1;
				int _localized = -1;
				int _english = -1;
				String _name = null;
				String _author = null;
				String _contact = null;
				String _summary = null;
				String _version = null;
				String _license = null;
				int _items = 0;
				while (_file != null) {
					String _type = _reader.peek().name();
					String _text = "";
					
					if (_type.compareTo("BEGIN_OBJECT") == 0) {
						Log.e("MiniF", "OBJECT_N_MEMBERS " + new Integer(_reader.peek().values().length).toString() + " " + _text);
						
						if ((_inList >= 0) && (_indent == _inList + 1)) {
							_oneApp = _indent;
						}
						else if (_key != null) {
							if (_key.compareTo("localized") == 0) {
								if (_oneApp >= 0) _localized = _indent;
							}
							else if (_key.compareTo("en-US") == 0) {
								if (_localized >= 0) _english = _indent;
							}
						}
						
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = {";
						//Log.e("MiniF", "SCAN " + new Integer(_nth).toString() + " " + _text);
						_indent++;
						
						_reader.beginObject();
					}
					else if (_type.compareTo("END_OBJECT") == 0) {
						_indent--;
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + "}";
						//Log.e("MiniF", "SCAN " + new Integer(_nth).toString() + " " + _text);
						
						if (_oneApp == _indent) {
							_oneApp = -1;
							
							TextView _item = new TextView(_activity);
							//_item.setText("Name: " + _name + "\nAuthor: " + _author + "\nContact: " + _contact + "\nSummary: " + _summary + "\nVersion: " + _version + "\nSource code: " + _source + "\nLicense: " + _license);
							_item.setText("Name: " + _name + "\nSummary: " + _summary + "\nVersion: " + _version + "\nSource code: " + _siteForSelectedApp + "\nLicense: " + _license);
							_apps.addView(_item);
							
							final String _site1 = new String(_siteForSelectedApp);
							final String _id1 = new String(_nameCodeForSelectedApp);
							final String _version1 = new String(_versionCodeForSelectedApp);
							
							TextView _site = new TextView(_activity);
							_site.setText("Official source code");
							_site.setTextSize(_site.getTextSize() * 1.125f);
							_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(_site1));
								_activity.startActivity(_order);
							}});
							_apps.addView(_site);
							
							TextView _apk = new TextView(_activity);
							_apk.setText("Download latest APK");
							_apk.setTextSize(_apk.getTextSize() * 1.125f);
							_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse("https://f-droid.org/repo/" + _id1 + "_" + _version1 + ".apk"));
								_activity.startActivity(_order);
							}});
							_apps.addView(_apk);
							
							TextView _tar = new TextView(_activity);
							_tar.setText("Download latest source tarball");
							_tar.setTextSize(_tar.getTextSize() * 1.125f);
							_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse("https://f-droid.org/repo/" + _id1 + "_" + _version1 + "_src.tar.gz"));
								_activity.startActivity(_order);
							}});
							_apps.addView(_tar);
							
							TextView _item1 = new TextView(_activity);
							_item1.setText("===== ===== ===== ===== =====");
							_apps.addView(_item1);
						}
						else if (_localized == _indent) _localized = -1;
						else if (_english == _indent) _english = -1;
						
						_reader.endObject();
						_key = null;
					}
					else if (_type.compareTo("BEGIN_ARRAY") == 0) {
						Log.e("MiniF", "ARRAY_N_MEMBERS " + new Integer(_reader.peek().values().length).toString() + " " + _text);
						
						if (_key.compareTo("apps") == 0) {
							if (_inList == -1) _inList = _indent;
						}
						
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = [";
						//Log.e("MiniF", "SCAN " + new Integer(_nth).toString() + " " + _text);
						_indent++;
						
						_reader.beginArray();
					}
					else if (_type.compareTo("END_ARRAY") == 0) {
						_indent--;
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + "]";
						//Log.e("MiniF", "SCAN " + new Integer(_nth).toString() + " " + _text);
						
						if (_inList == _indent) _inList = -1;
						
						_reader.endArray();
						_key = null;
					}
					else if (_type.compareTo("NAME") == 0) {
						_key = _reader.nextName();
					}
					else if (_type.compareTo("BOOLEAN") == 0) {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = " + ((_reader.nextBoolean() == true) ? "true" : "false");
						//Log.e("MiniF", "SCAN_BOOLEAN " + new Integer(_nth).toString() + " " + _text);
						
						_key = null;
					}
					else if (_type.compareTo("NUMBER") == 0) {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = " + new Double(_reader.nextDouble()).toString();
						//Log.e("MiniF", "SCAN_NUMBER " + new Integer(_nth).toString() + " " + _text);
						
						_key = null;
					}
					else if (_type.compareTo("STRING") == 0) {
						String _value = _reader.nextString();
						if (_english >= 0) {
							if (_key != null) {
								if (_key.compareTo("name") == 0) {
									//Log.e("MiniF", "NAME! " + _value);
									_name = _value;
								}
								else if (_key.compareTo("summary") == 0) _summary = _value;
							}
						}
						else if (_oneApp >= 0) {
							if (_key != null) {
								if (_key.compareTo("authorName") == 0) _author = _value;
								if (_key.compareTo("authorEmail") == 0) _contact = _value;
								else if (_key.compareTo("packageName") == 0) _nameCodeForSelectedApp = _value;
								else if (_key.compareTo("suggestedVersionCode") == 0) _versionCodeForSelectedApp = _value;
								else if (_key.compareTo("suggestedVersionName") == 0) _version = _value;
								else if (_key.compareTo("sourceCode") == 0) _siteForSelectedApp = _value;
								else if (_key.compareTo("license") == 0) _license = _value;
							}
						}
						
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = " + _value;
						//if (_key != null) {
						//	if (_key.compareTo("description") != 0) Log.e("MiniF", "SCAN_STRING " + new Integer(_nth).toString() + " " + _text);
						//}
						
						_key = null;
					}
					else if (_type.compareTo("END_DOCUMENT") == 0) {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _type;
						//Log.e("MiniF", "SCAN " + new Integer(_nth).toString() + " " + _text);
						
						break;
					}
					else {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _type;
						//Log.e("MiniF", "SCAN " + new Integer(_nth).toString() + " " + _text);
						
						_reader.skipValue();
					}
					
					_nth++;
				}
				_reader.close();
			}
			catch(Exception x) {x.printStackTrace();}
		}});
		_overall1.addView(_load);
		
		Button _about = new Button(_activity);
		_about.setText("About");
		_about.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			_apps.removeAllViews();
			
			TextView _about1 = new TextView(_activity);
			_about1.setText("Name: Mini F\nAuthor: Minho Jo (whitestone8214@gmail.com)\nVersion: 0.0\nLicense: GNU GPL v3 or later");
			_apps.addView(_about1);
			
			TextView _site = new TextView(_activity);
			_site.setText("Official source code");
			_site.setTextSize(_site.getTextSize() * 1.125f);
			_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
				Intent _order = new Intent(Intent.ACTION_VIEW);
				_order.setData(Uri.parse("https://gitlab.com/whitestone8214/MiniF"));
				_activity.startActivity(_order);
			}});
			_apps.addView(_site);
		}});
		_overall1.addView(_about);
		
		// Box for apps
		ScrollView _0 = new ScrollView(_activity);
		_overall1.addView(_0);
		_apps = new LinearLayout(_activity);
		_apps.setOrientation(LinearLayout.VERTICAL);
		_0.addView(_apps);
		
		/*Button _x = new Button(_activity);
		_x.setText("XXX");
		_apps.addView(_x);*/
	}
	public void onBackPressed() {
		finish();
	}
}
