package trust.web3view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import trust.Call;
import trust.SignMessageRequest;
import trust.SignPersonalMessageRequest;
import trust.SignTransactionRequest;
import trust.SignTypedMessageRequest;
import trust.Trust;
import trust.core.entity.Address;
import trust.core.entity.Message;
import trust.core.entity.Transaction;
import trust.core.entity.TypedData;
import trust.web3.OnSignMessageListener;
import trust.web3.OnSignPersonalMessageListener;
import trust.web3.OnSignTransactionListener;
import trust.web3.OnSignTypedMessageListener;
import trust.web3.Web3View;

public class MainActivity extends AppCompatActivity implements
        OnSignTransactionListener, OnSignPersonalMessageListener, OnSignTypedMessageListener, OnSignMessageListener {

    private TextView url;
    private Web3View web3;
    private Call<SignMessageRequest> callSignMessage;
    private Call<SignPersonalMessageRequest> callSignPersonalMessage;
    private Call<SignTypedMessageRequest> callSignTypedMessage;
    private Call<SignTransactionRequest> callSignTransaction;
    private static final String TAG = "Web3View";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = findViewById(R.id.url);
        web3 = findViewById(R.id.web3view);
        findViewById(R.id.go).setOnClickListener(v -> {
            web3.loadUrl(url.getText().toString());
            web3.requestFocus();
        });

        setupWeb3();
    }

    private void setupWeb3() {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        web3.setChainId(1);
        web3.setRpcUrl("https://mainnet.infura.io/v3/95fa3a86534344ee9d1bf00e2b0d6d06");
        web3.setWalletAddress(new Address("0xF18F1926648b2bABE9c6e3981D696d3613b53821"));

        web3.setOnSignMessageListener(message ->
                callSignMessage = Trust.signMessage().message(message).call(this));
        web3.setOnSignPersonalMessageListener(message ->
                callSignPersonalMessage = Trust.signPersonalMessage().message(message).call(this));
        web3.setOnSignTransactionListener(transaction ->
                callSignTransaction = Trust.signTransaction().transaction(transaction).call(this));
        web3.setOnSignTypedMessageListener(message ->
                callSignTypedMessage = Trust.signTypedMessage().message(message).call(this));
    }

    @Override
    public void onSignMessage(Message<String> message) {
        Toast.makeText(this, message.value, Toast.LENGTH_LONG).show();
        web3.onSignCancel(message);
    }

    @Override
    public void onSignPersonalMessage(Message<String> message) {
        Toast.makeText(this, message.value, Toast.LENGTH_LONG).show();
        web3.onSignCancel(message);
    }

    @Override
    public void onSignTypedMessage(Message<TypedData[]> message) {
        Toast.makeText(this, new Gson().toJson(message), Toast.LENGTH_LONG).show();
        web3.onSignCancel(message);
    }

    @Override
    public void onSignTransaction(Transaction transaction) {
        String str = new StringBuilder()
                .append(transaction.recipient == null ? "" : transaction.recipient.toString()).append(" : ")
                .append(transaction.contract == null ? "" : transaction.contract.toString()).append(" : ")
                .append(transaction.value.toString()).append(" : ")
                .append(transaction.gasPrice.toString()).append(" : ")
                .append(transaction.gasLimit).append(" : ")
                .append(transaction.nonce).append(" : ")
                .append(transaction.payload).append(" : ")
                .toString();
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        web3.onSignCancel(transaction);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        Log.e(TAG, data.getDataString());
        Log.e(TAG, "requestCode: " + requestCode);
        Log.e(TAG, "resultCode: " + resultCode);
        Log.e(TAG, "resultCode: " + Activity.RESULT_OK);

        String resultMessage;
        if(resultCode == Activity.RESULT_OK){
            resultMessage= "message signed ok :" + data.getDataString();
        } else  {
            resultMessage= "message cancelled";
        }

       new AlertDialog.Builder(this).setTitle("Sign result")
               .setMessage(resultMessage)
               .setPositiveButton("ok", null)
               .show();

        if (callSignTransaction != null) {
            callSignTransaction.onActivityResult(requestCode, resultCode, data, response -> {
                Transaction transaction = response.request.body();
                if (response.isSuccess()) {
                    web3.onSignTransactionSuccessful(transaction, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3.onSignCancel(transaction);
                    } else {
                        web3.onSignError(transaction, "Some error");
                    }
                }
            });
        }

        if (callSignMessage != null) {
            callSignMessage.onActivityResult(requestCode, resultCode, data, response -> {
                Message message = response.request.body();
                if (response.isSuccess()) {
                    web3.onSignMessageSuccessful(message, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3.onSignCancel(message);
                    } else {
                        web3.onSignError(message, "Some error");
                    }
                }
            });
        }

        if (callSignPersonalMessage != null) {
            callSignPersonalMessage.onActivityResult(requestCode, resultCode, data, response -> {
                Message message = response.request.body();
                if (response.isSuccess()) {
                    web3.onSignMessageSuccessful(message, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3.onSignCancel(message);
                    } else {
                        web3.onSignError(message, "Some error");
                    }
                }
            });
        }

        if (callSignTypedMessage != null) {
            callSignTypedMessage.onActivityResult(requestCode, resultCode, data, response -> {
                Message message = response.request.body();
                if (response.isSuccess()) {
                    web3.onSignMessageSuccessful(message, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        web3.onSignCancel(message);
                    } else {
                        web3.onSignError(message, "Some error");
                    }
                }
            });
        }
    }
}
