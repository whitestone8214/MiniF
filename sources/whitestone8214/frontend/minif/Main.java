package whitestone8214.frontend.minif;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
	class Button2 extends Button {
		public int index; public Button2 index(int value) {index = value; return this;}
		public String comment; public Button2 comment(String value) {comment = value; return this;}
		
		public Button2(Context context) {
			super(context);
			index = 0;
			comment = null;
		}
		public Button2(Context context, String label, View.OnClickListener triggerTap, View.OnLongClickListener triggerHold) {
			this(context);
			if (label != null) setText(label);
			if (triggerTap != null) setOnClickListener(triggerTap);
			if (triggerHold != null) setOnLongClickListener(triggerHold);
		}
	}
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
		
		public Profile() {clean();}
		
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
	public int _from;
	public int _widthApp;
	public int _heightApp;
	public int _spanApp;
	public int _stateSizeGathered;
	
	// Widgets
	public LinearLayout _boxOverall;
	public Button2 _previous, _next;
	public TextView _here;
	public LinearLayout _roster;
	public Dialog _dialog;
	public LinearLayout _boxDialog;
	
	
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
		
		// 몇 번째 앱부터 보여줄까요?
		_from = 0;
		
		// Size of app window
		_widthApp = 0;
		_heightApp = 0;
		_spanApp = 0; // 가로와 세로 중 더 짧은 쪽의 16분의 1
		_stateSizeGathered = 0;
		
		// Overall
		_boxOverall = new LinearLayout(_activity) {
			@Override public void onSizeChanged(int widthAfter, int heightAfter, int widthBefore, int heightBefore) {
				if (_stateSizeGathered == 0) {
					_widthApp = widthAfter;
					_heightApp = heightAfter;
					_spanApp = (_widthApp < _heightApp) ? (_widthApp / 16) : (_heightApp / 16);
					_stateSizeGathered = 1;
					Toast.makeText(_activity, "WOW", 1).show();
					
					app_in();
				}
			}
		};
		_boxOverall.setOrientation(LinearLayout.VERTICAL);
		setContentView(_boxOverall);
	}
	public void onBackPressed() {
		finish();
	}
	public Dialog onCreateDialog(int id) {return _dialog;}
	
	private void app_in() {
		LinearLayout _boxOverall1 = new LinearLayout(_activity);
		_boxOverall1.setOrientation(LinearLayout.VERTICAL);
		setContentView(_boxOverall1);
		
		// Header bar = "Load" + "Previous page" + Page counter + "Next page" + "About"
		LinearLayout _boxHead = new LinearLayout(_activity);
		_boxOverall1.addView(_boxHead);
		_boxHead.addView(new Button2(_activity, "Load", new View.OnClickListener() {public void onClick(View _this) {
			try {
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
					if (_type.compareTo("END_DOCUMENT") == 0) {
						if (_nApps >= 1) break;
					}
					else if (_type.compareTo("NAME") == 0) {
						_key = _json.nextName();
					}
					else if (_type.compareTo("STRING") == 0) {
						String _value = _json.nextString();
						String _key0 = (_key != null) ? _key : "NOKEY";
						
						if (_indent == _indentForApps + 1) {
							if (_key0.compareTo("authorName") == 0) _profile.author = new String(_value);
							else if (_key0.compareTo("authorEmail") == 0) _profile.contact = new String(_value);
							else if (_key0.compareTo("packageName") == 0) _profile.id = new String(_value);
							else if (_key0.compareTo("suggestedVersionName") == 0) _profile.version = new String(_value);
							else if (_key0.compareTo("suggestedVersionCode") == 0) _profile.model = new String(_value);
							else if (_key0.compareTo("sourceCode") == 0) _profile.source = new String(_value);
							else if (_key0.compareTo("license") == 0) _profile.license = new String(_value);
						}
						else if ((_indent == _indentForApps + 3) && (_english == true)) {
							if (_key0.compareTo("name") == 0) _profile.name = new String(_value);
							else if (_key0.compareTo("summary") == 0) _profile.summary = new String(_value).replace('\n', ' ');
						}
						
						_key = null;
					}
					else if (_type.compareTo("NUMBER") == 0) {
						double _value = _json.nextDouble();
						
						_key = null;
					}
					else if (_type.compareTo("BEGIN_ARRAY") == 0) {
						_json.beginArray();
						_indent++;
						String _key0 = (_key != null) ? _key : "NOKEY";
						
						if ((_key0.compareTo("apps") == 0) && (_indentForApps < 0)) _indentForApps = _indent;
						
						_key = null;
					}
					else if (_type.compareTo("BEGIN_OBJECT") == 0) {
						_json.beginObject();
						_indent++;
						String _key0 = (_key != null) ? _key : "NOKEY";
						
						if ((_indent == _indentForApps + 2) && (_key0.compareTo("localized") == 0)) {
							_indentForLocalized = _indent;
						}
						else if ((_indent == _indentForLocalized + 1) && (_key0.compareTo("en-US") == 0)) {
							_english = true;
						}
						
						_key = null;
					}
					else if (_type.compareTo("END_ARRAY") == 0) {
						_json.endArray();
						_indent--;
						
						if (_indent == _indentForApps - 1) {
							_indentForApps = -99;
							break;
						}
					}
					else if (_type.compareTo("END_OBJECT") == 0) {
						_json.endObject();
						_indent--;
						
						if ((_indent == _indentForLocalized) && (_english == true)) {
							_english = false;
						}
						else if (_indent == _indentForLocalized - 1) {
							_indentForLocalized = -99;
						}
						else if (_indent == _indentForApps) {
							Log.e("MiniF", "CRAWL LEAVE_APP");
							
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
					}
					else {
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
			}
			catch(Exception x) {x.printStackTrace();}
		}}, null));
		_boxHead.addView(new Button2(_activity, "-100", new View.OnClickListener() {public void onClick(View _this) {
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
						
						LinearLayout _boxRoutes = new LinearLayout(_activity);
						_roster.addView(_boxRoutes);
						_boxRoutes.addView(new Button2(_activity, "Site",
							new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(((Button2)_this).comment));
								_activity.startActivity(_order);
							}},
							new View.OnLongClickListener() {public boolean onLongClick(View _this) {
								Toast.makeText(_activity, ((Button2)_this).comment, 1).show();
								return true;
							}}
						).comment(_slots0[4]));
						_boxRoutes.addView(new Button2(_activity, "APK",
							new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(((Button2)_this).comment));
								_activity.startActivity(_order);
							}},
							new View.OnLongClickListener() {public boolean onLongClick(View _this) {
								Toast.makeText(_activity, ((Button2)_this).comment, 1).show();
								return true;
							}}
						).comment("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + ".apk"));
						_boxRoutes.addView(new Button2(_activity, "Source code",
							new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(((Button2)_this).comment));
								_activity.startActivity(_order);
							}},
							new View.OnLongClickListener() {public boolean onLongClick(View _this) {
								Toast.makeText(_activity, ((Button2)_this).comment, 1).show();
								return true;
							}}
						).comment("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + "_src.tar.gz"));
					}
					
					_byte = _stream.read();
				}
				_stream.close();
				_roster.scrollTo(0, 0);
				
				_here.setText(new Integer(_nthPageNow * 100).toString() + " ~ " + new Integer((_nthPageNow * 100) + 99).toString() + " / " + _nApps0);
			}
			catch(Exception x) {x.printStackTrace();}
		}}, null));
		_boxHead.addView(new TextView(_activity));
		_boxHead.addView(new Button2(_activity, "+100", new View.OnClickListener() {public void onClick(View _this) {
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
						
						LinearLayout _boxRoutes = new LinearLayout(_activity);
						_roster.addView(_boxRoutes);
						_boxRoutes.addView(new Button2(_activity, "Site",
							new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(((Button2)_this).comment));
								_activity.startActivity(_order);
							}},
							new View.OnLongClickListener() {public boolean onLongClick(View _this) {
								Toast.makeText(_activity, ((Button2)_this).comment, 1).show();
								return true;
							}}
						).comment(_slots0[4]));
						_boxRoutes.addView(new Button2(_activity, "APK",
							new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(((Button2)_this).comment));
								_activity.startActivity(_order);
							}},
							new View.OnLongClickListener() {public boolean onLongClick(View _this) {
								Toast.makeText(_activity, ((Button2)_this).comment, 1).show();
								return true;
							}}
						).comment("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + ".apk"));
						_boxRoutes.addView(new Button2(_activity, "Source code",
							new View.OnClickListener() {public void onClick(View _this) {
								Intent _order = new Intent(Intent.ACTION_VIEW);
								_order.setData(Uri.parse(((Button2)_this).comment));
								_activity.startActivity(_order);
							}},
							new View.OnLongClickListener() {public boolean onLongClick(View _this) {
								Toast.makeText(_activity, ((Button2)_this).comment, 1).show();
								return true;
							}}
						).comment("https://f-droid.org/repo/" + new String(_slots0[6]) + "_" + new String(_slots0[8]) + "_src.tar.gz"));
					}
					
					_byte = _stream.read();
				}
				_stream.close();
				_roster.scrollTo(0, 0);
				
				_here.setText(new Integer(_nthPageNow * 100).toString() + " ~ " + new Integer((_nthPageNow * 100) + 99).toString() + " / " + _nApps0);
			}
			catch(Exception x) {x.printStackTrace();}
		}}, null));
		_boxHead.addView(new Button2(_activity, "About", new View.OnClickListener() {public void onClick(View _this) {
			removeDialog(0);
			_boxDialog.removeAllViews();
			
			TextView _about1 = new TextView(_activity);
			_about1.setText("Name: Mini F\nAuthor: Minho Jo (whitestone8214@gmail.com)\nVersion: 0.0\nLicense: GNU GPL v3 or later");
			_boxDialog.addView(_about1);
			
			TextView _site = new TextView(_activity);
			_site.setText("Official source code");
			_site.setTextSize(_site.getTextSize() * 1.0625f);
			_site.setOnClickListener(new View.OnClickListener() {public void onClick(View _this) {
				Intent _order = new Intent(Intent.ACTION_VIEW);
				_order.setData(Uri.parse("https://gitlab.com/whitestone8214/MiniF"));
				_activity.startActivity(_order);
			}});
			_boxDialog.addView(_site);
			
			_boxDialog.addView(new Button2(_activity, "Understood", new View.OnClickListener() {public void onClick(View _this) {
				removeDialog(0);
			}}, null));
			
			showDialog(0);
		}}, null));
		
		_here = (TextView)_boxHead.getChildAt(2);
		_here.setText("? of ?");
		
		// Box for apps
		ScrollView _0 = new ScrollView(_activity);
		_boxOverall1.addView(_0);
		_roster = new LinearLayout(_activity);
		_roster.setOrientation(LinearLayout.VERTICAL);
		_0.addView(_roster);
		
		// Dialog
		_dialog = new Dialog(_activity);
		_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		_boxDialog = new LinearLayout(_activity);
		_boxDialog.setOrientation(LinearLayout.VERTICAL);
		_dialog.setContentView(_boxDialog);
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
