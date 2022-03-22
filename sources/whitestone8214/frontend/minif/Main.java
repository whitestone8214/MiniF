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
		}
		
		public Profile copy() {
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
		}
	}
	
	
	// Non-visible things
	public Activity _activity;
	public String[] _permissions;
	public ArrayList<Profile> _profiles;
	public int _from;
	/*public String _nameCodeForSelectedApp;
	public String _versionCodeForSelectedApp;
	public String _siteForSelectedApp;*/
	
	// Widgets
	public LinearLayout _overall;
	public LinearLayout _overall1;
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
		
		// List of profiles for each app
		_profiles = new ArrayList<Profile>();
		
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
				String _file = _activity.getExternalFilesDir(null).getPath() + "/index-v1.jar";
				ZipEntry _file1 = new ZipFile(_file).getEntry("index-v1.json");
				JsonReader _reader = new JsonReader(new InputStreamReader(new ZipFile(_file).getInputStream(_file1), "UTF-8"));
				
				//_roster.removeAllViews();
				int _indent = 0;
				String _key = null;
				int _inList = -1;
				int _oneApp = -1;
				int _localized = -1;
				int _english = -1;
				String _name = null;
				/*String _author = null;
				String _contact = null;
				String _summary = null;
				String _version = null;
				String _license = null;
				int _items = 0;*/
				Profile _profile = new Profile();
				while (_file != null) {
					String _type = _reader.peek().name();
					String _text = "";
					
					if (_type.compareTo("BEGIN_OBJECT") == 0) {
						//Log.e("MiniF", "OBJECT_N_MEMBERS " + new Integer(_reader.peek().values().length).toString() + " " + _text);
						
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
							
							/*TextView _item = new TextView(_activity);
							//_item.setText("Name: " + _name + "\nAuthor: " + _author + "\nContact: " + _contact + "\nSummary: " + _summary + "\nVersion: " + _version + "\nSource code: " + _source + "\nLicense: " + _license);
							_item.setText("Name: " + _name + "\nSummary: " + _summary + "\nVersion: " + _version + "\nSource code: " + _siteForSelectedApp + "\nLicense: " + _license);
							_roster.addView(_item);
							
							final String _site1 = new String(_siteForSelectedApp);
							final String _id1 = new String(_nameCodeForSelectedApp);
							final String _version1 = new String(_versionCodeForSelectedApp);
							
							TextView _site = new TextView(_activity);
							_site.setText("Official source code");
							_site.setTextSize(_site.getTextSize() * 1.0625f);
							_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(_site1));
								_activity.startActivity(_order);
							}});
							_roster.addView(_site);
							
							TextView _apk = new TextView(_activity);
							_apk.setText("Download latest APK");
							_apk.setTextSize(_apk.getTextSize() * 1.0625f);
							_apk.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse("https://f-droid.org/repo/" + _id1 + "_" + _version1 + ".apk"));
								_activity.startActivity(_order);
							}});
							_roster.addView(_apk);
							
							TextView _tar = new TextView(_activity);
							_tar.setText("Download latest source tarball");
							_tar.setTextSize(_tar.getTextSize() * 1.0625f);
							_tar.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse("https://f-droid.org/repo/" + _id1 + "_" + _version1 + "_src.tar.gz"));
								_activity.startActivity(_order);
							}});
							_roster.addView(_tar);
							
							TextView _item1 = new TextView(_activity);
							_item1.setText("===== ===== ===== ===== =====");
							_roster.addView(_item1);*/
							
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
							_roster.scrollTo(0, 0);
							
							_here.setText(new Integer(_from).toString() + " ~ " + new Integer(_from + 99).toString() + " / " + new Integer(_profiles.size()).toString());
						}
						else if (_localized == _indent) _localized = -1;
						else if (_english == _indent) _english = -1;
						
						_reader.endObject();
						_key = null;
					}
					else if (_type.compareTo("BEGIN_ARRAY") == 0) {
						//Log.e("MiniF", "ARRAY_N_MEMBERS " + new Integer(_reader.peek().values().length).toString() + " " + _text);
						
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
								if (_key.compareTo("name") == 0) _profile.name = new String(_value);
								else if (_key.compareTo("summary") == 0) _profile.summary = _value;
							}
						}
						else if (_oneApp >= 0) {
							if (_key != null) {
								/*if (_key.compareTo("authorName") == 0) _author = _value;
								if (_key.compareTo("authorEmail") == 0) _contact = _value;
								else if (_key.compareTo("packageName") == 0) _nameCodeForSelectedApp = _value;
								else if (_key.compareTo("suggestedVersionCode") == 0) _versionCodeForSelectedApp = _value;
								else if (_key.compareTo("suggestedVersionName") == 0) _version = _value;
								else if (_key.compareTo("sourceCode") == 0) _siteForSelectedApp = _value;
								else if (_key.compareTo("license") == 0) _license = _value;*/
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
				}
				_reader.close();
				
				Toast.makeText(_activity, "Total " + new Integer(_profiles.size()).toString() + " app(s)", 1).show();
				_roster.removeAllViews();
				for (Profile _profile1 : _profiles) {
					TextView _item = new TextView(_activity);
					_item.setText("Name: " + _profile1.name + "\nAuthor: " + _profile1.author + "\nContact: " + _profile1.contact + "\nSummary: " + _profile1.summary + "\nVersion: " + _profile1.version + "\nSource code: " + _profile1.source + "\nLicense: " + _profile1.license);
					//_item.setText("Name: " + _name + "\nSummary: " + _summary + "\nVersion: " + _version + "\nSource code: " + _siteForSelectedApp + "\nLicense: " + _license);
					_roster.addView(_item);
					
					/*final String _site1 = new String(_siteForSelectedApp);
					final String _id1 = new String(_nameCodeForSelectedApp);
					final String _version1 = new String(_versionCodeForSelectedApp);*/
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
				_roster.scrollTo(0, 0);
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
		
		Button _previous = new Button(_activity);
		_previous.setText("Previous 100");
		_previous.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			_from -= 100;
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
			
			_here.setText(new Integer(_from).toString() + " ~ " + new Integer(_from + 99).toString() + " / " + new Integer(_profiles.size()).toString());
		}});
		_overall2.addView(_previous);
		
		Button _next = new Button(_activity);
		_next.setText("Next 100");
		_next.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
			if (_from + 100 >= _profiles.size()) return;
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
			
			_here.setText(new Integer(_from).toString() + " ~ " + new Integer(_from + 99).toString() + " / " + new Integer(_profiles.size()).toString());
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
}
