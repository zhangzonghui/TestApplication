package com.tempus.traceback;

import java.io.UnsupportedEncodingException;

import com.tempus.traceback.ActionbarView.OnActionBtnClickListener;
import com.tempus.utils.ToastUtils;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ReadActivity extends Activity implements OnActionBtnClickListener {
	private TextView ifo_NFC;
	private NfcAdapter nfcAdapter;
	private String readResult = "";
	private PendingIntent pendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private boolean isFirst = true;
	private TextView toWBtn;
	private IntentFilter ndef;
	private ActionbarView readActionbarView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		initActionBarView();
		//该方法完成接收到Intent时的初始化工作
		init();	
	}
	
	
	/**@author==ZZH
	 * ����ActionBar
	 */
	public void initActionBarView(){
		readActionbarView = (ActionbarView) findViewById(R.id.read_actionbar);
	    readActionbarView.setLeftbunttonImage(R.drawable.title_left_button_bg);
	    readActionbarView.setTitle(R.string.nfc_title);
	    readActionbarView.setTitleColor(getResources().getColor(R.color.white));
	    readActionbarView.setOnActionBtnClickListener(this);
	}

	
	/**
	 * 检测工作,判断设备的NFC支持情况
	 * @return
	 */
	private Boolean ifNFCUse() {
		// TODO Auto-generated method stub
		if (nfcAdapter == null) {
			ToastUtils.toastMessage(ThisApp.getInstance().getResources()
					.getString(R.string.readActivity_nonfc));
			finish();
			return false;
		}
		if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
			ToastUtils.toastMessage(ThisApp.getInstance().getResources()
					.getString(R.string.readctivity_opennfc));
			finish();
			return false;
		}
		return true;
	}

	/**
	 * 初始化过程
	 */
	private void init() {
		// TODO Auto-generated method stub
		toWBtn=(TextView)findViewById(R.id.new_nfc);
		toWBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(ReadActivity.this,Write2Nfc.class);
				startActivity(intent);
			}
		});
		ifo_NFC = (TextView) findViewById(R.id.ifo_NFC);
		//NFC适配器，所有的关于NFC的操作从该适配器进行
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(!ifNFCUse()){
			return;
		}
		//将被调用的Intent，用于重复被Intent触发后将要执行的跳转
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		//设定要过滤的标签动作，这里只接收ACTION_NDEF_DISCOVERED类型
		ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndef.addCategory("*/*");
		mFilters = new IntentFilter[] { ndef };// 过滤器
		mTechLists = new String[][] { new String[] { NfcA.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcV.class.getName() } };// 允许扫描的标签类型
		
		if (isFirst) {
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent()
					.getAction())) {
				System.out.println(getIntent().getAction());
				if (readFromTag(getIntent())) {
					ifo_NFC.setText(readResult);
					System.out.println("1.5...");
				} else {
					ifo_NFC.setText("标签数据为空");
				}
			}
			isFirst = false;
		}
		System.out.println("onCreate...");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);
		System.out.println("onPause...");
	}

	/* 
	 * 重写onResume回调函数的意义在于处理多次读取NFC标签时的情况
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.==zzh
		// 必须配置否则第二次不能检测到Intent  
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
				mTechLists);
	}

	/*
	 * 
	 *  (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		//System.out.println(intent.getAction());
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())||
				NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			System.out.println("onNewIntent2...");
			if (readFromTag(intent)) {
				ifo_NFC.setText(readResult);
				System.out.println("onNewIntent3...");
			} else {
				ifo_NFC.setText(ThisApp.getInstance().getResources()
						.getString(R.string.error_nfcmessage));
			}
		}

	}

	/**
	 * 读取NFC标签数据的操作
	 * @param intent
	 * @return
	 */
	private boolean readFromTag(Intent intent) {
		Parcelable[] rawArray = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawArray != null) {
			NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
			NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
			try {
				if (mNdefRecord != null) {
					readResult = new String(mNdefRecord.getPayload(), "UTF-8");
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return false;
		}
		return false;
	}


	@Override
	public void onLeftBtnClick(View view) {
		// TODO Auto-generated method stub
		finish();
	}


	@Override
	public void onRightBtnClick(View view) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onRightSecondBtnClick(View view) {
		// TODO Auto-generated method stub
		
	}

	

}
