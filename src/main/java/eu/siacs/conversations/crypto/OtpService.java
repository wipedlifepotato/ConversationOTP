package eu.siacs.conversations.crypto;
import java.io.*;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;


import eu.siacs.conversations.Config;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.generator.MessageGenerator;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.ui.ConversationActivity;
import eu.siacs.conversations.xmpp.chatstate.ChatState;
import eu.siacs.conversations.xmpp.jid.InvalidJidException;
import eu.siacs.conversations.xmpp.jid.Jid;
import eu.siacs.conversations.xmpp.stanzas.MessagePacket;
import eu.siacs.conversations.ui.ConversationActivity;

public class OtpService  {

    final int PICKFILE_RESULT_CODE = 1;
    protected File mKeyFile;
    protected byte[] mKey;
    protected /*BigInteger*/ int offset;
    protected String mJid;
    private XmppConnectionService mXmppConnectionService;
    public OtpService( Jid account ) throws IOException {
        this.mJid=account.getLocalpart()+"."+account.getDomainpart();
        this.setKey();
    }
    public OtpService() {
        //Intent intent =
    }
    public OtpService(String Jid) {
        //Intent intent =
        this.mJid=Jid;
        try {
            this.setKey();
        }catch(Exception e){
            Log.d("ERROR_OTP_SETKEY", e.toString());
        }
    }
    public static void copyFile(File src, File dst) throws IOException { // https://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }


    public File getmKeyFile(){
        return mKeyFile;
    }
     public void setKey(/*String KeyFilePath*/) throws IOException {

        String fileName=ConversationActivity.OtpKeyFilePaths+"/"+mJid+".txt";

        this.mKeyFile = new File(fileName );
        //check if file readable
        if (!this.mKeyFile.exists()) {
            this.mKeyFile=null;
           return;
        }

        if( !this.mKeyFile.canRead() )
            throw new IOException("Can't open file or this shit dont readable; "
                +fileName );
    }

    public int getOffset(){
        return this.offset;
    }
    public void setOffset(int offset){
        this.offset=offset;
    }
    public String doCryptDecrypt(String message,boolean isEncrypted) throws FileNotFoundException, IOException{
        //thanks to hypn for consultation
        if( this.mKeyFile == null ) return "OTP ERROR KEY FILE NOT EXISTS, but message: "+message;
        RandomAccessFile i = new RandomAccessFile(this.mKeyFile,"r");
        i.seek(offset);

        byte[] messageArray;
        if (isEncrypted) {
            messageArray = Base64.decode(message,Base64.DEFAULT);
        }else messageArray = (byte[])message.getBytes(Charset.defaultCharset() );
        byte[] cryptedMessage = new byte[messageArray.length+1];

        int x;
        for(x=0;x<messageArray.length;x++){
            byte tmp=i.readByte();
            cryptedMessage[x]= (byte)((messageArray[x])^(tmp))  ;//0x7f?
            assert (cryptedMessage[x]^(tmp) ) == messageArray[x];
        }
        i.close();
        this.offset = offset + x;
        if(isEncrypted) return new String(cryptedMessage, "UTF-8");
        return Base64.encodeToString(cryptedMessage, 0, cryptedMessage.length, Base64.DEFAULT);
    }
    public String doCryptDecrypt(String message) throws FileNotFoundException, IOException{
        return doCryptDecrypt(message, false);
    }
    public void ReSizeFile() throws IOException{
        //resize
    }
}
