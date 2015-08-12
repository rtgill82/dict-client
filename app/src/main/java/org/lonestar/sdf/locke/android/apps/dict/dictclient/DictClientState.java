package org.lonestar.sdf.locke.android.apps.dict.dictclient;


public class DictClientState {
	static private DictClientState _instance = null;
	
	public DictionarySpinnerAdapter dictAdapter;
	public String viewText;
	
	protected DictClientState()
	{
		dictAdapter = null;
		viewText = null;
	}
	
	static public DictClientState getInstance()
	{
		if (_instance == null) {
			_instance = new DictClientState();
		}
		
		return _instance;
	}
}
