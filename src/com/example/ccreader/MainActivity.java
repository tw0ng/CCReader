package com.example.ccreader;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.NfcEvent;
import android.nfc.tech.IsoDep;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.FlagToString;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int MY_SCAN_REQUEST_CODE = 1;
	private Button scanbutton;
	private TextView description;
	private NfcAdapter nfcAdapter;
	private String creditcardinfo;
	private TextView isNFC_On;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		scanbutton = (Button) findViewById(R.id.scanbutton);
		description = (TextView) findViewById(R.id.description);
		isNFC_On = (TextView) findViewById(R.id.nfcbool);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		scanbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onScanPress();
			}
		});

	}

	public void onScanPress() {
		Intent scanIntent = new Intent(this, CardIOActivity.class);

		// required for authentication with card.io
		scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, "2d71c48b29534e25b43bbd4010ca3a62");

		// customize these values to suit your needs.
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: true
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

		// MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
		startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(creditcardinfo != null) {
			nfcAdapter.disableReaderMode(this);
			isNFC_On.setText("NFC is On, tap to send Credit Card info");
		   nfcAdapter.setNdefPushMessage(getNoteAsNdef(creditcardinfo), this);
		}
		else {
			isNFC_On.setText("NFC is off, please scan Credit Card");
			nfcAdapter.enableReaderMode(this, new ReaderCallback() {
				
				@Override
				public void onTagDiscovered(Tag tag) {
					// TODO Auto-generated method stub
					Log.d("CC READER", "No credit card info");
				}
			}, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == MY_SCAN_REQUEST_CODE) {
			String resultDisplayStr;
			if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
				CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

				// Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
				resultDisplayStr = "Card Number: " + scanResult.getFormattedCardNumber() + "\n";


				if (scanResult.isExpiryValid()) {
					resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
				}
				
				if (scanResult.cvv != null) {
					// Never log or display a CVV
					resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
				}

				if (scanResult.postalCode != null) {
					resultDisplayStr += "Postal Code: " + scanResult.postalCode + "\n";
				}
				creditcardinfo = resultDisplayStr;
				nfcAdapter.disableReaderMode(this);
				isNFC_On.setText("NFC is On, tap to send Credit Card info");
				nfcAdapter.setNdefPushMessage(getNoteAsNdef(creditcardinfo), this);
			}
			else {
				resultDisplayStr = "Scan was canceled.";
				creditcardinfo = null;
				isNFC_On.setText("NFC is off, please scan Credit Card");
				nfcAdapter.enableReaderMode(this, new ReaderCallback() {
					
					@Override
					public void onTagDiscovered(Tag tag) {
						// TODO Auto-generated method stub
						Log.d("CC READER", "No credit card info");
					}
				}, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
			}
			
			// do something with resultDisplayStr, maybe display it in a textView
			description.setText(resultDisplayStr);
		}
		// else handle other activity results
	}

	private NdefMessage getNoteAsNdef(String UserId) {
        byte[] textBytes = UserId.getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }
}
