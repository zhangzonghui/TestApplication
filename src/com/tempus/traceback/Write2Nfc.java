package com.tempus.traceback;

import java.io.IOException;

import com.tempus.traceback.ActionbarView.OnActionBtnClickListener;
import com.tempus.utils.ToastUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Write2Nfc extends Activity implements OnActionBtnClickListener {
	private EditText editText;
	private TextView noteText;
	private TextView wButton;
	private IntentFilter[] mWriteTagFilters;
	private NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	String[][] mTechLists;
	private Boolean ifWrite;
    private ActionbarView writeActionbar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_ifo);
		initActionBarView();
		init();
		displayControl(false);
		System.out.println("0....");
	}
	
	/**@author==ZZH
	 * ����ActionBar
	 */
	public void initActionBarView(){
		writeActionbar = (ActionbarView) findViewById(R.id.write_actionbar);
	    writeActionbar.setLeftbunttonImage(R.drawable.title_left_button_bg);
	    writeActionbar.setTitle(R.string.writenfc_title);
	    writeActionbar.setTitleColor(getResources().getColor(R.color.white));
	    writeActionbar.setOnActionBtnClickListener(this);
	}

	private void init() {
		// TODO Auto-generated method stub
		ifWrite = false;
		editText = (EditText) findViewById(R.id.editText);
		wButton = (TextView) findViewById(R.id.writeBtn);
		noteText=(TextView)findViewById(R.id.noteText);
		wButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ifWrite = true;
				displayControl(true);
			}
		});
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndef.addCategory("*/*");
		mWriteTagFilters = new IntentFilter[] { ndef };
		mTechLists = new String[][] { new String[] { NfcA.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcV.class.getName() } };
	}
	public void displayControl(Boolean ifWriting){
		if(ifWriting){			
			noteText.setVisibility(View.VISIBLE);
			editText.setVisibility(View.INVISIBLE);
			wButton.setVisibility(View.INVISIBLE);
			return;
		}
		noteText.setVisibility(View.INVISIBLE);
		editText.setVisibility(View.VISIBLE);
		wButton.setVisibility(View.VISIBLE);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		nfcAdapter.enableForegroundDispatch(this, pendingIntent,
				mWriteTagFilters, mTechLists);
		System.out.println("1....");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		String text = editText.getText().toString();
		if (text == null) {
			ToastUtils.toastMessage(ThisApp.getInstance()
					.getResources().getString(R.string.no_message));

			return;
		}
		if (ifWrite == true) {
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())||
					NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
				Tag tag =intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				Ndef ndef = Ndef.get(tag);
				try {
					//数据的写入过程一定要有连接操作
					ndef.connect();
					//构建数据包，也就是要写入标签的数据
					NdefRecord ndefRecord = new NdefRecord(
							NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
							new byte[] {}, text.getBytes());
					NdefRecord[] records = { ndefRecord };
					NdefMessage ndefMessage = new NdefMessage(records);
					ndef.writeNdefMessage(ndefMessage);
					ToastUtils.toastMessage(ThisApp.getInstance()
							.getResources().getString(R.string.write_message_sucess));
					displayControl(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (FormatException e) {
					
				}
			}
		}
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
