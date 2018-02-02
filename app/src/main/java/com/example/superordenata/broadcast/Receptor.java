package com.example.superordenata.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Receptor extends BroadcastReceiver {


    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String number;  //because the passed incoming is only valid in ringing

    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            number = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);
        }
    }
    //LLAMADA ENTRANTE----------------------------------
    protected void onIncomingCallStarted(Context ctx, String number, Date start){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd k:mm");
        String fecha = dateFormat.format(start);

        String estado = "entrante";
        Toast.makeText(ctx, estado+" Numero: "+number+" "+fecha, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ctx, ServicioCliente.class);
        intent.putExtra("state", estado);
        intent.putExtra("phoneNumber", number);
        intent.putExtra("date", fecha);
        ctx.startService(intent);

    }
    //LLAMADA SALIENTE---------------------------------------
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd k:mm");
        String fecha = dateFormat.format(start);

        String estado = "entrante";
        Toast.makeText(ctx, estado+" Numero: "+number+" "+fecha, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ctx, ServicioCliente.class);
        intent.putExtra("state", estado);
        intent.putExtra("phoneNumber", number);
        intent.putExtra("date", fecha);
        ctx.startService(intent);


    }
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){}
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){}

    //LLAMADAS PERDIDAS--------------------
    protected void onMissedCall(Context ctx, String number, Date start){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd k:mm");
        String fecha = dateFormat.format(start);

        String estado = "perdida";
        Toast.makeText(ctx, estado+" Numero: "+number+" "+fecha, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ctx, ServicioCliente.class);
        intent.putExtra("state", estado);
        intent.putExtra("phoneNumber", number);
        intent.putExtra("date", fecha);
        ctx.startService(intent);

    }

    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                Receptor.number = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, Receptor.number, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, Receptor.number, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, Receptor.number, callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, Receptor.number, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }
}
