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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Main extends Activity {
	class Profile {
		public int index; public Profile index(int value) {index = value; return this;}
		public String comment; public Profile comment(String value) {comment = value; return this;}
		public String name; public Profile name(String value) {name = value; return this;}
		public String id; public Profile id(String value) {id = value; return this;}
		public String author; public Profile author(String value) {author = value; return this;}
		public String contact; public Profile contact(String value) {contact = value; return this;}
		public String summary; public Profile summary(String value) {summary = value; return this;}
		public String version; public Profile version(String value) {version = value; return this;}
		public String model; public Profile model(String value) {model = value; return this;}
		public String source; public Profile source(String value) {source = value; return this;}
		public String license; public Profile license(String value) {license = value; return this;}
		
		public Profile() {
			/*index = 0;
			comment = null;
			name = null;
			id = null;
			author = null;
			contact = null;
			summary = null;
			version = null;
			model = null;
			source = null;
			license = null;*/
			
			clean();
		}
		
		/*public Profile copy() {
			return new Profile().
				index(index).
				comment((comment != null) ? new String(comment) : null).
				name((name != null) ? new String(name) : null).
				id((id != null) ? new String(id) : null).
				author((author != null) ? new String(author) : null).
				contact((contact != null) ? new String(contact) : null).
				summary((summary != null) ? new String(summary) : null).
				version((version != null) ? new String(version) : null).
				model((model != null) ? new String(model) : null).
				source((source != null) ? new String(source) : null).
				license((license != null) ? new String(license) : null);
		}*/
		
		public Profile clean() {
			index = 0;
			comment = null;
			name = null;
			id = null;
			author = null;
			contact = null;
			summary = null;
			version = null;
			model = null;
			source = null;
			license = null;
			
			return this;
		}
	}
	
	
	// Non-visible things
	public Activity _activity;
	public String[] _permissions;
	public String _home;
	public int _nthPageNow;
	//public ArrayList<Profile> _profiles;
	public int _from;
	
	// Widgets
	public LinearLayout _overall;
	public LinearLayout _overall1;
	public Button _previous, _next;
	public TextView _here;
	public LinearLayout _roster;
	
	
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
		
		// Home directory
		_home = _activity.getExternalFilesDir(null).getPath();
		
		// Which page we are on?
		_nthPageNow = -1;
		
		// List of profiles for each app
		//_profiles = new ArrayList<Profile>();
		
		// 몇 번째 앱부터 보여줄까요?
		_from = 0;
		
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
				/*String _file = _activity.getExternalFilesDir(null).getPath() + "/index-v1.jar";
				ZipEntry _file1 = new ZipFile(_file).getEntry("index-v1.json");
				JsonReader _reader = new JsonReader(new InputStreamReader(new ZipFile(_file).getInputStream(_file1), "UTF-8"));*/
				
				// ~/summary, ~/catalogue
				if (deleteRecursively(_home + "/summary") != true) throw new Exception("Failed to initialize catalogue: #0");
				if (deleteRecursively(_home + "/catalogue") != true) throw new Exception("Failed to initialize catalogue: #1");
				if (new File(_home + "/summary").createNewFile() != true) throw new Exception("Failed to initialize catalogue: #2");
				if (new File(_home + "/catalogue").mkdir() != true) throw new Exception("Failed to initialize catalogue: #3");
				
				// index-v1.json
				ZipFile _jar = new ZipFile(_home + "/index-v1.jar");
				Log.e("MiniF", "CRAWL_INDEX_SIZE " + new Long(_jar.getEntry("index-v1.json").getSize()));
				JsonReader _json = new JsonReader(new InputStreamReader(_jar.getInputStream(_jar.getEntry("index-v1.json")), "UTF-8"));
				
				// Crawl the JSON and build the catalogue
				String _type = null;
				String _key = null;
				int _state = 0;
				int _indent = 0;
				int _indentForApps = -99;
				int _indentForLocalized = -99;
				boolean _english = false;
				Profile _profile = new Profile();
				int _nApps = 0;
				int _nthPage = 0;
				while (_json != null) {
					_type = _json.peek().name();
					//Log.e("MiniF", "CRAWL MET " + _type);
					if (_type.compareTo("END_DOCUMENT") == 0) {
						//Log.e("MiniF", "CRAWL " + _type);
						if (_nApps >= 1) break;
					}
					else if (_type.compareTo("NAME") == 0) {
						_key = _json.nextName();
					}
					else if (_type.compareTo("STRING") == 0) {
						String _value = _json.nextString();
						String _key0 = (_key != null) ? _key : "NOKEY";
						//Log.e("MiniF", "CRAWL " + _indent + " " + ((_key != null) ? (_key + " = ") : "unnamed ") + _value);
						//Log.e("MiniF", "A " + _indent + " " + _indentForApps + " " + _key0 + " " + _value);
						
						if (_indent == _indentForApps + 1) {
						//if ((_state == 3) && (_key != null)) {
							if (_key0.compareTo("authorName") == 0) _profile.author = new String(_value);
							else if (_key0.compareTo("authorEmail") == 0) _profile.contact = new String(_value);
							else if (_key0.compareTo("packageName") == 0) _profile.id = new String(_value);
							else if (_key0.compareTo("suggestedVersionName") == 0) _profile.version = new String(_value);
							else if (_key0.compareTo("suggestedVersionCode") == 0) _profile.model = new String(_value);
							else if (_key0.compareTo("sourceCode") == 0) _profile.source = new String(_value);
							else if (_key0.compareTo("license") == 0) _profile.license = new String(_value);
							//Log.e("MiniF", "CRAWL #" + _nApps + " " + ((_key != null) ? (_key + " = ") : "unnamed ") + _value);
						}
						//else if ((_state == 5) && (_key != null)) {
						else if ((_indent == _indentForApps + 3) && (_english == true)) {
							if (_key0.compareTo("name") == 0) _profile.name = new String(_value);
							else if (_key0.compareTo("summary") == 0) _profile.summary = new String(_value).replace('\n', ' ');
							//Log.e("MiniF", "CRAWL #" + _nApps + " " + ((_key != null) ? (_key + " = ") : "unnamed ") + _value);
						}
						
						_key = null;
					}
					else if (_type.compareTo("NUMBER") == 0) {
						double _value = _json.nextDouble();
						//Log.e("MiniF", "CRAWL " + ((_key != null) ? (_key + " = ") : "unnamed ") + new Double(_value).toString());
						
						_key = null;
					}
					else if (_type.compareTo("BEGIN_ARRAY") == 0) {
						//Log.e("MiniF", "CRAWL " + ((_key != null) ? (_key + " = [") : "unnamed ["));
						_json.beginArray();
						_indent++;
						String _key0 = (_key != null) ? _key : "NOKEY";
						
						if ((_key0.compareTo("apps") == 0) && (_indentForApps < 0)) _indentForApps = _indent;
						
						_key = null;
					}
					else if (_type.compareTo("BEGIN_OBJECT") == 0) {
						//Log.e("MiniF", "CRAWL " + ((_key != null) ? (_key + " = {") : "unnamed {"));
						_json.beginObject();
						_indent++;
						String _key0 = (_key != null) ? _key : "NOKEY";
						
						//if ((_state == 1) && (_indent == _indentForApps + 1)) {
						/*if (_indent == _indentForApps + 2) {
							Log.e("MiniF", "CRAWL ENTER_APP");
							_state = 2;
						}*/
						//else if ((_state == 2) && (_key != null) && (_key.compareTo("localized") == 0)) {
						if ((_indent == _indentForApps + 2) && (_key0.compareTo("localized") == 0)) {
							_indentForLocalized = _indent;
						}
						else if ((_indent == _indentForLocalized + 1) && (_key0.compareTo("en-US") == 0)) {
							_english = true;
						}
						
						_key = null;
					}
					else if (_type.compareTo("END_ARRAY") == 0) {
						//Log.e("MiniF", "CRAWL ]");
						_json.endArray();
						_indent--;
						
						//if ((_state == 1) && (_indent == _indentForApps - 1)) {
						/*if (_state == 1) {
							_state = 0;
						}*/
						if (_indent == _indentForApps - 1) {
							_indentForApps = -99;
							break;
						}
					}
					else if (_type.compareTo("END_OBJECT") == 0) {
						//Log.e("MiniF", "CRAWL }");
						_json.endObject();
						_indent--;
						
						//if ((_state == 2) && (_indent == _indentForApps)) {
						if ((_indent == _indentForLocalized) && (_english == true)) {
							_english = false;
						}
						else if (_indent == _indentForLocalized - 1) {
							_indentForLocalized = -99;
						}
						//else if (_state == 2) {
						else if (_indent == _indentForApps) {
							Log.e("MiniF", "CRAWL LEAVE_APP");
							//_state = 1;
							
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.name != null) ? _profile.name : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.summary != null) ? _profile.summary : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.author != null) ? _profile.author : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.contact != null) ? _profile.contact : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.source != null) ? _profile.source : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.license != null) ? _profile.license : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.id != null) ? _profile.id : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.version != null) ? _profile.version : "?") + "\n");
							writeOnFile(_home + "/catalogue/" + new Integer(_nthPage).toString(), ((_profile.model != null) ? _profile.model : "?") + "\n");
							
							Log.e("MiniF", "APP #" + _nApps + " " + _profile.name + " DONE");
							_nApps++;
							if (_nApps % 100 == 0) _nthPage++;
							_profile.clean();
						}
						/*else if (_state == 3) {
							_state = 2;
						}
						else if (_state == 4) {
							_state = 3;
						}*/
					}
					else {
						//Log.e("MiniF", "CRAWL " + _type);
						_json.skipValue();
					}
				}
				Log.e("MiniF", "CRAWL_END " + _nApps + " " + _indent + " " + _type);
				writeOnFile(_home + "/summary", "0\n");
				writeOnFile(_home + "/summary", new Integer(_nApps).toString() + "\n");
				
				// Close the JSON
				_json.close();
				
				// Click "Next" = Load next page = Load first page
				_next.performClick();
				
				//_roster.removeAllViews();
				/*int _indent = 0;
				String _key = null;
				int _inList = -1;
				int _oneApp = -1;
				int _localized = -1;
				int _english = -1;
				String _name = null;
				Profile _profile = new Profile();
				while (_file != null) {
					//String _type = _reader.peek().name();
					String _type = _json.peek().name();
					String _text = "";
					
					if (_type.compareTo("BEGIN_OBJECT") == 0) {
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
						_indent++;
						
						_json.beginObject();
					}
					else if (_type.compareTo("END_OBJECT") == 0) {
						_indent--;
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + "}";
						
						if (_oneApp == _indent) {
							_oneApp = -1;
							
							_profiles.add(_profile.copy());
							_profile = new Profile();
							
							_roster.removeAllViews();
							for (int _n = _from; _n < _from + 100; _n++) {
								if (_n >= _profiles.size()) break;
								final Profile _app = _profiles.get(_n);
								
								if (_n > _from) {
									TextView _item1 = new TextView(_activity);
									_item1.setText("===== ===== ===== ===== =====");
									_roster.addView(_item1);
								}
								
								TextView _item = new TextView(_activity);
								_item.setText("Name: " + _app.name + "\nAuthor: " + _app.author + "\nContact: " + _app.contact + "\nSummary: " + _app.summary + "\nVersion: " + _app.version + "\nSource code: " + _app.source + "\nLicense: " + _app.license);
								_roster.addView(_item);
								
								TextView _site = new TextView(_activity);
								_site.setText("Official source code");
								_site.setTextSize(_site.getTextSize() * 1.0625f);
								_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
									Intent _order = new Intent(Intent.ACTION_VIEW);
									_order.setData(Uri.parse(_app.source));
									_activity.startActivity(_order);
								}});
								_roster.addView(_site);
								
								TextView _apk = new TextView(_activity);
								_apk.setText("Download latest APK");
								_apk.setTextSize(_apk.getTextSize() * 1.0625f);
								_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
									Intent _order = new Intent(Intent.ACTION_VIEW);
									_order.setData(Uri.parse("https://f-droid.org/repo/" + _app.id + "_" + _app.model + ".apk"));
									_activity.startActivity(_order);
								}});
								_roster.addView(_apk);
								
								TextView _tar = new TextView(_activity);
								_tar.setText("Download latest source tarball");
								_tar.setTextSize(_tar.getTextSize() * 1.0625f);
								_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
									Intent _order = new Intent(Intent.ACTION_VIEW);
									_order.setData(Uri.parse("https://f-droid.org/repo/" + _app.id + "_" + _app.model + "_src.tar.gz"));
									_activity.startActivity(_order);
								}});
								_roster.addView(_tar);
							}
							_roster.scrollTo(0, 0);
							
							_here.setText(new Integer(_from).toString() + " ~ " + new Integer(_from + 99).toString() + " / " + new Integer(_profiles.size()).toString());
						}
						else if (_localized == _indent) _localized = -1;
						else if (_english == _indent) _english = -1;
						
						_json.endObject();
						_key = null;
					}
					else if (_type.compareTo("BEGIN_ARRAY") == 0) {
						//Log.e("MiniF", "ARRAY_N_MEMBERS " + new Integer(_json.peek().values().length).toString() + " " + _text);
						
						if (_key.compareTo("apps") == 0) {
							if (_inList == -1) _inList = _indent;
						}
						
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = [";
						_indent++;
						
						_json.beginArray();
					}
					else if (_type.compareTo("END_ARRAY") == 0) {
						_indent--;
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + "]";
						
						if (_inList == _indent) _inList = -1;
						
						_json.endArray();
						_key = null;
					}
					else if (_type.compareTo("NAME") == 0) {
						_key = _json.nextName();
					}
					else if (_type.compareTo("BOOLEAN") == 0) {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = " + ((_json.nextBoolean() == true) ? "true" : "false");
						
						_key = null;
					}
					else if (_type.compareTo("NUMBER") == 0) {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = " + new Double(_json.nextDouble()).toString();
						
						_key = null;
					}
					else if (_type.compareTo("STRING") == 0) {
						String _value = _json.nextString();
						if (_english >= 0) {
							if (_key != null) {
								if (_key.compareTo("name") == 0) _profile.name = new String(_value);
								else if (_key.compareTo("summary") == 0) _profile.summary = _value;
							}
						}
						else if (_oneApp >= 0) {
							if (_key != null) {
								if (_key.compareTo("authorName") == 0) _profile.author = new String(_value);
								else if (_key.compareTo("authorEmail") == 0) _profile.contact = new String(_value);
								else if (_key.compareTo("packageName") == 0) _profile.id = new String(_value);
								else if (_key.compareTo("suggestedVersionCode") == 0) _profile.model = new String(_value);
								else if (_key.compareTo("suggestedVersionName") == 0) _profile.version = new String(_value);
								else if (_key.compareTo("sourceCode") == 0) {
									Log.e("MiniF", "SAUCE! " + _value);
									_profile.source = new String(_value);
								}
								else if (_key.compareTo("license") == 0) _profile.license = new String(_value);
							}
						}
						
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _key + " = " + _value;
						
						_key = null;
					}
					else if (_type.compareTo("END_DOCUMENT") == 0) {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _type;
						
						break;
					}
					else {
						for (int _n = 0; _n < _indent; _n++) _text = "\t" + _text;
						_text = _text + _type;
						
						_json.skipValue();
					}
				}*/
				//_json.close();
				
				/*Toast.makeText(_activity, "Total " + new Integer(_profiles.size()).toString() + " app(s)", 1).show();
				_roster.removeAllViews();
				for (Profile _profile1 : _profiles) {
					TextView _item = new TextView(_activity);
					_item.setText("Name: " + _profile1.name + "\nAuthor: " + _profile1.author + "\nContact: " + _profile1.contact + "\nSummary: " + _profile1.summary + "\nVersion: " + _profile1.version + "\nSource code: " + _profile1.source + "\nLicense: " + _profile1.license);
					_roster.addView(_item);
					
					final Profile _profile11 = _profile1;
					
					TextView _site = new TextView(_activity);
					_site.setText("Official source code");
					_site.setTextSize(_site.getTextSize() * 1.0625f);
					_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
						Intent _order = new Intent(Intent.ACTION_VIEW);
						_order.setData(Uri.parse(_profile11.source));
						_activity.startActivity(_order);
					}});
					_roster.addView(_site);
					
					TextView _apk = new TextView(_activity);
					_apk.setText("Download latest APK");
					_apk.setTextSize(_apk.getTextSize() * 1.0625f);
					_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
						Intent _order = new Intent(Intent.ACTION_VIEW);
						_order.setData(Uri.parse("https://f-droid.org/repo/" + _profile11.id + "_" + _profile11.model + ".apk"));
						_activity.startActivity(_order);
					}});
					_roster.addView(_apk);
					
					TextView _tar = new TextView(_activity);
					_tar.setText("Download latest source tarball");
					_tar.setTextSize(_tar.getTextSize() * 1.0625f);
					_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
						Intent _order = new Intent(Intent.ACTION_VIEW);
						_order.setData(Uri.parse("https://f-droid.org/repo/" + _profile11.id + "_" + _profile11.model + "_src.tar.gz"));
						_activity.startActivity(_order);
					}});
					_roster.addView(_tar);
					
					TextView _item1 = new TextView(_activity);
					_item1.setText("===== ===== ===== ===== =====");
					_roster.addView(_item1);
				}
				_roster.scrollTo(0, 0);*/
			}
			catch(Exception x) {x.printStackTrace();}
		}});
		_overall1.addView(_load);
		
		Button _about = new Button(_activity);
		_about.setText("About");
		_about.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			_roster.removeAllViews();
			
			TextView _about1 = new TextView(_activity);
			_about1.setText("Name: Mini F\nAuthor: Minho Jo (whitestone8214@gmail.com)\nVersion: 0.0\nLicense: GNU GPL v3 or later");
			_roster.addView(_about1);
			
			TextView _site = new TextView(_activity);
			_site.setText("Official source code");
			_site.setTextSize(_site.getTextSize() * 1.0625f);
			_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
				Intent _order = new Intent(Intent.ACTION_VIEW);
				_order.setData(Uri.parse("https://gitlab.com/whitestone8214/MiniF"));
				_activity.startActivity(_order);
			}});
			_roster.addView(_site);
			
			_roster.scrollTo(0, 0);
		}});
		_overall1.addView(_about);
		
		LinearLayout _overall2 = new LinearLayout(_activity);
		_overall1.addView(_overall2);
		
		_previous = new Button(_activity);
		_previous.setText("Previous 100");
		_previous.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			// Keep _nthPageNow from being less than 0
			if (_nthPageNow <= 0) _nthPageNow = 0;
			else _nthPageNow--;
			
			FileInputStream _stream = null;
			int _byte = -1;
			try {
				// Total number of apps
				_stream = new FileInputStream(_home + "/summary");
				_byte = _stream.read();
				String _nApps0 = "";
				boolean _stateLine2 = false;
				while (_byte != -1) {
					if (_byte == '\n') _stateLine2 = (_stateLine2 == true) ? false : true;
					else if (_stateLine2 == true) {
						byte[] _byte0 = new byte[1];
						_byte0[0] = (byte)_byte;
						_nApps0 += new String(_byte0);
					}
					_byte = _stream.read();
				}
				_stream.close();
				
				// Clean the box
				_roster.removeAllViews();
				
				// Add apps to the box
				_stream = new FileInputStream(_home + "/catalogue/" + new Integer(_nthPageNow).toString());
				_byte = _stream.read();
				String[] _slots = new String[9];
				int _slot = 0;
				String _item = "";
				boolean _first = true;
				while (_byte != -1) {
					if (_byte == '\n') {
						_slots[_slot] = new String(_item);
						_slot++;
						_item = "";
					}
					else {
						byte[] _byte0 = new byte[1];
						_byte0[0] = (byte)_byte;
						_item += new String(_byte0);
					}
					
					if (_slot >= 9) {
						final String[] _slots0 = _slots;
						
						if (_first == true) _first = false;
						else {
							TextView _0 = new TextView(_activity);
							_0.setText("===== ===== ===== ===== =====");
							_roster.addView(_0);
						}
						
						TextView _1 = new TextView(_activity);
						_1.setText("Name: " + new String(_slots0[0]) + "\nAuthor: " + new String(_slots0[2]) + "\nContact: " + new String(_slots0[3]) + "\nSummary: " + new String(_slots0[1]) + "\nVersion: " + new String(_slots0[7]) + "\nSource code: " + new String(_slots0[4]) + "\nLicense: " + new String(_slots0[5]));
						_roster.addView(_1);
						
						TextView _site = new TextView(_activity);
						_site.setText("Official source code");
						_site.setTextSize(_site.getTextSize() * 1.0625f);
						_site.setTag((Object)new String(_slots0[4]));
						_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
							//Toast.makeText(_activity, (String)_this.getTag(), 1).show();
							Intent _order = new Intent(Intent.ACTION_VIEW);
							_order.setData(Uri.parse((String)_this.getTag()));
							_activity.startActivity(_order);
						}});
						_roster.addView(_site);
						
						TextView _apk = new TextView(_activity);
						_apk.setText("Download latest APK");
						_apk.setTextSize(_apk.getTextSize() * 1.0625f);
						_apk.setTag((Object)("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + ".apk"));
						_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
							//Toast.makeText(_activity, (String)_this.getTag(), 1).show();
							Intent _order = new Intent(Intent.ACTION_VIEW);
							_order.setData(Uri.parse((String)_this.getTag()));
							_activity.startActivity(_order);
						}});
						_roster.addView(_apk);
						
						TextView _tar = new TextView(_activity);
						_tar.setText("Download latest source tarball");
						_tar.setTextSize(_tar.getTextSize() * 1.0625f);
						_tar.setTag((Object)("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + "_src.tar.gz"));
						_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
							//Toast.makeText(_activity, (String)_this.getTag(), 1).show();
							Intent _order = new Intent(Intent.ACTION_VIEW);
							_order.setData(Uri.parse((String)_this.getTag()));
							_activity.startActivity(_order);
						}});
						_roster.addView(_tar);
					}
					
					_byte = _stream.read();
				}
				_stream.close();
				_roster.scrollTo(0, 0);
				
				_here.setText(new Integer(_nthPageNow * 100).toString() + " ~ " + new Integer((_nthPageNow * 100) + 99).toString() + " / " + _nApps0);
			}
			catch(Exception x) {x.printStackTrace();}
			
			/*_from -= 100;
			if (_from < 0) _from = 0;
			
			_roster.removeAllViews();
			_roster.scrollTo(0, 0);
			for (int _n = _from; _n < _from + 100; _n++) {
				if (_n >= _profiles.size()) break;
				final Profile _app = _profiles.get(_n);
				
				if (_n > _from) {
					TextView _item1 = new TextView(_activity);
					_item1.setText("===== ===== ===== ===== =====");
					_roster.addView(_item1);
				}
				
				TextView _item = new TextView(_activity);
				_item.setText("Name: " + _app.name + "\nAuthor: " + _app.author + "\nContact: " + _app.contact + "\nSummary: " + _app.summary + "\nVersion: " + _app.version + "\nSource code: " + _app.source + "\nLicense: " + _app.license);
				_roster.addView(_item);
				
				TextView _site = new TextView(_activity);
				_site.setText("Official source code");
				_site.setTextSize(_site.getTextSize() * 1.0625f);
				_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
					Intent _order = new Intent(Intent.ACTION_VIEW);
					_order.setData(Uri.parse(_app.source));
					_activity.startActivity(_order);
				}});
				_roster.addView(_site);
				
				TextView _apk = new TextView(_activity);
				_apk.setText("Download latest APK");
				_apk.setTextSize(_apk.getTextSize() * 1.0625f);
				_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
					Intent _order = new Intent(Intent.ACTION_VIEW);
					_order.setData(Uri.parse("https://f-droid.org/repo/" + _app.id + "_" + _app.model + ".apk"));
					_activity.startActivity(_order);
				}});
				_roster.addView(_apk);
				
				TextView _tar = new TextView(_activity);
				_tar.setText("Download latest source tarball");
				_tar.setTextSize(_tar.getTextSize() * 1.0625f);
				_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
					Intent _order = new Intent(Intent.ACTION_VIEW);
					_order.setData(Uri.parse("https://f-droid.org/repo/" + _app.id + "_" + _app.model + "_src.tar.gz"));
					_activity.startActivity(_order);
				}});
				_roster.addView(_tar);
			}
			
			_here.setText(new Integer(_from).toString() + " ~ " + new Integer(_from + 99).toString() + " / " + new Integer(_profiles.size()).toString());*/
		}});
		_overall2.addView(_previous);
		
		_next = new Button(_activity);
		_next.setText("Next 100");
		_next.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			if (new File(_home + "/catalogue/" + new Integer(_nthPageNow + 1).toString()).exists() != true) return;
			_nthPageNow++;
			
			FileInputStream _stream = null;
			int _byte = -1;
			try {
				// Total number of apps
				_stream = new FileInputStream(_home + "/summary");
				_byte = _stream.read();
				String _nApps0 = "";
				boolean _stateLine2 = false;
				while (_byte != -1) {
					if (_byte == '\n') _stateLine2 = (_stateLine2 == true) ? false : true;
					else if (_stateLine2 == true) {
						byte[] _byte0 = new byte[1];
						_byte0[0] = (byte)_byte;
						_nApps0 += new String(_byte0);
					}
					_byte = _stream.read();
				}
				_stream.close();
				
				// Clean the box
				_roster.removeAllViews();
				
				// Add apps to the box
				_stream = new FileInputStream(_home + "/catalogue/" + new Integer(_nthPageNow).toString());
				_byte = _stream.read();
				String[] _slots = new String[9];
				int _slot = 0;
				String _item = "";
				boolean _first = true;
				while (_byte != -1) {
					if (_byte == '\n') {
						_slots[_slot] = new String(_item);
						_slot++;
						_item = "";
					}
					else {
						byte[] _byte0 = new byte[1];
						_byte0[0] = (byte)_byte;
						_item += new String(_byte0);
					}
					
					if (_slot >= 9) {
						final String[] _slots0 = _slots;
						_slot = 0;
						
						if (_first == true) _first = false;
						else {
							TextView _0 = new TextView(_activity);
							_0.setText("===== ===== ===== ===== =====");
							_roster.addView(_0);
						}
						
						TextView _1 = new TextView(_activity);
						_1.setText("Name: " + new String(_slots0[0]) + "\nAuthor: " + new String(_slots0[2]) + "\nContact: " + new String(_slots0[3]) + "\nSummary: " + new String(_slots0[1]) + "\nVersion: " + new String(_slots0[7]) + "\nSource code: " + new String(_slots0[4]) + "\nLicense: " + new String(_slots0[5]));
						_roster.addView(_1);
						
						TextView _site = new TextView(_activity);
						_site.setText("Official source code");
						_site.setTextSize(_site.getTextSize() * 1.0625f);
						_site.setTag((Object)new String(_slots0[4]));
						_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
							//Toast.makeText(_activity, (String)_this.getTag(), 1).show();
							Intent _order = new Intent(Intent.ACTION_VIEW);
							_order.setData(Uri.parse((String)_this.getTag()));
							_activity.startActivity(_order);
						}});
						_roster.addView(_site);
						
						TextView _apk = new TextView(_activity);
						_apk.setText("Download latest APK");
						_apk.setTextSize(_apk.getTextSize() * 1.0625f);
						_apk.setTag((Object)("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + ".apk"));
						_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
							//Toast.makeText(_activity, (String)_this.getTag(), 1).show();
							Intent _order = new Intent(Intent.ACTION_VIEW);
							_order.setData(Uri.parse((String)_this.getTag()));
							_activity.startActivity(_order);
						}});
						_roster.addView(_apk);
						
						TextView _tar = new TextView(_activity);
						_tar.setText("Download latest source tarball");
						_tar.setTextSize(_tar.getTextSize() * 1.0625f);
						_tar.setTag((Object)("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + "_src.tar.gz"));
						_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
							//Toast.makeText(_activity, (String)_this.getTag(), 1).show();
							Intent _order = new Intent(Intent.ACTION_VIEW);
							_order.setData(Uri.parse((String)_this.getTag()));
							_activity.startActivity(_order);
						}});
						_roster.addView(_tar);
					}
					
					_byte = _stream.read();
				}
				_stream.close();
				_roster.scrollTo(0, 0);
				
				_here.setText(new Integer(_nthPageNow * 100).toString() + " ~ " + new Integer((_nthPageNow * 100) + 99).toString() + " / " + _nApps0);
			}
			catch(Exception x) {x.printStackTrace();}
			
			/*if (_from + 100 >= _profiles.size()) return;
			_from += 100;
			
			_roster.removeAllViews();
			_roster.scrollTo(0, 0);
			for (int _n = _from; _n < _from + 100; _n++) {
				if (_n >= _profiles.size()) break;
				final Profile _app = _profiles.get(_n);
				
				if (_n > _from) {
					TextView _item1 = new TextView(_activity);
					_item1.setText("===== ===== ===== ===== =====");
					_roster.addView(_item1);
				}
				
				TextView _item = new TextView(_activity);
				_item.setText("Name: " + _app.name + "\nAuthor: " + _app.author + "\nContact: " + _app.contact + "\nSummary: " + _app.summary + "\nVersion: " + _app.version + "\nSource code: " + _app.source + "\nLicense: " + _app.license);
				//_item.setText("Name: " + _name + "\nSummary: " + _summary + "\nVersion: " + _version + "\nSource code: " + _siteForSelectedApp + "\nLicense: " + _license);
				_roster.addView(_item);
				
				TextView _site = new TextView(_activity);
				_site.setText("Official source code");
				_site.setTextSize(_site.getTextSize() * 1.0625f);
				_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
					Intent _order = new Intent(Intent.ACTION_VIEW);
					_order.setData(Uri.parse(_app.source));
					_activity.startActivity(_order);
				}});
				_roster.addView(_site);
				
				TextView _apk = new TextView(_activity);
				_apk.setText("Download latest APK");
				_apk.setTextSize(_apk.getTextSize() * 1.0625f);
				_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
					Intent _order = new Intent(Intent.ACTION_VIEW);
					_order.setData(Uri.parse("https://f-droid.org/repo/" + _app.id + "_" + _app.model + ".apk"));
					_activity.startActivity(_order);
				}});
				_roster.addView(_apk);
				
				TextView _tar = new TextView(_activity);
				_tar.setText("Download latest source tarball");
				_tar.setTextSize(_tar.getTextSize() * 1.0625f);
				_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
					Intent _order = new Intent(Intent.ACTION_VIEW);
					_order.setData(Uri.parse("https://f-droid.org/repo/" + _app.id + "_" + _app.model + "_src.tar.gz"));
					_activity.startActivity(_order);
				}});
				_roster.addView(_tar);
			}
			
			_here.setText(new Integer(_from).toString() + " ~ " + new Integer(_from + 99).toString() + " / " + new Integer(_profiles.size()).toString());*/
		}});
		_overall2.addView(_next);
		
		_here = new TextView(_activity);
		_here.setText("? of ?");
		_overall2.addView(_here);
		
		// Box for apps
		ScrollView _0 = new ScrollView(_activity);
		_overall1.addView(_0);
		_roster = new LinearLayout(_activity);
		_roster.setOrientation(LinearLayout.VERTICAL);
		_0.addView(_roster);
	}
	public void onBackPressed() {
		finish();
	}
	
	private boolean deleteRecursively(String path) {
		try {
			// File
			File _file = new File(path);
			if (_file.exists() != true) return true;
			
			// Remove children first if the file is directory
			if (_file.isDirectory() == true) {
				File[] _children = _file.listFiles();
				for (File _child : _children) {
					if (deleteRecursively(_child.getPath()) != true) return false;
				}
			}
			
			return _file.delete();
		}
		catch(Exception x) {x.printStackTrace(); return false;}
	}
	private void writeOnFile(String path, String string) {
		try {
			FileOutputStream _stream = new FileOutputStream(path, true);
			_stream.write(string.getBytes("UTF-8"));
			_stream.close();
		}
		catch(Exception x) {x.printStackTrace();}
	}
}
