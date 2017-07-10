package videopager.wilsonmanzano.globaluz.videopagerpc.shared.object;

import android.app.Activity;

import java.net.InetAddress;
import java.net.UnknownHostException;

import videopager.wilsonmanzano.globaluz.videopagerpc.Activity.OrderActivity;
import videopager.wilsonmanzano.globaluz.videopagerpc.Background.ChatConnection;
import videopager.wilsonmanzano.globaluz.videopagerpc.R;

public class OrderObject {

    private  int mPort = 0;
    private  InetAddress mInetAddress = null;
    private Activity mActivity;

    //In case that will display in a view de message

    private int order;
    private int pager;
    private ChatConnection mChatConnection;
    private boolean notify;
    private boolean finishAction = true;

    // A void constructor
    public OrderObject() {

    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    //Fuction that send the message
    public void sentMessage(String message) {

        if (!message.isEmpty()) {
            mChatConnection.sendMessage(message);
        }
    }
    public void SocketClose() {
        mChatConnection.tearDown();
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public int getPager() {
        return pager;
    }

    public void setPager(int pager) {
        this.pager = pager;
    }

    public void createNewConnection(){

        //Create a new chatConnction

        mChatConnection = new ChatConnection(((OrderActivity) mActivity).mUpdateHandler,mPort, mActivity);
        mChatConnection.connectToServer(mInetAddress, mPort);

    }

    //Constructor that create the connection inmmediately

    public OrderObject(int order, int pager, Activity activity) throws UnknownHostException {
        this.order = order;
        this.pager = pager;
        this.mActivity = activity;
        notify = false;
        //Get the port and the InetAddress
        mPort = Integer.parseInt(activity.getApplicationContext().getString(R.string.PORT)) + pager;
        mInetAddress = InetAddress.getByName(activity.getApplicationContext().getString(R.string.IP) + pager);
        //Create new socket connection and tries to connect with the pager
        mChatConnection = new ChatConnection(((OrderActivity)activity).mUpdateHandler,mPort,activity);
        mChatConnection.connectToServer(mInetAddress, mPort);
    }


    public boolean isFinishAction() {
        return finishAction;
    }

    public void setFinishAction(boolean finishAction) {
        this.finishAction = finishAction;
    }
}
