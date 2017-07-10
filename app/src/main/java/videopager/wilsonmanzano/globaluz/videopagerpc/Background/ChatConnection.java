package videopager.wilsonmanzano.globaluz.videopagerpc.Background;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import videopager.wilsonmanzano.globaluz.videopagerpc.Activity.OrderActivity;
import videopager.wilsonmanzano.globaluz.videopagerpc.shared.object.OrderObject;

public class ChatConnection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;
    private Activity mActivity;

    private static final String TAG = "ChatConnection";

    private Socket mSocket;
    private int mPort;

    //Constructor when you will start a new connection listener
    public ChatConnection(Handler mUpdateHandler, int port, Activity activity) {
        //In the constructor the serversocket is created and wait a connection
        this.mUpdateHandler = mUpdateHandler;
        mChatServer = new ChatServer(mUpdateHandler);
        this.mPort = port;
        this.mActivity = activity;

    }

    //Close the connection (It should be closed when connection is stabilized)
    public void tearDown() {
        mChatServer.tearDown();
        mChatClient.tearDown();
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }

    //This class send the message to the Handler that is in the Main Activity
    public synchronized void updateMessages(String msg) {

        Log.e(TAG, "Updating message: " + msg);

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }



    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;
        //Create the server thread (Listener of the socket)
        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }
        //Close the server socket that was created
        public void tearDown() {
            mThread.interrupt();
            try {
                if (mServerSocket!=null) {
                    mServerSocket.close();
                }
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        //The thread that create the server socket listener
        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket();
                    mServerSocket.setReuseAddress(true);
                    mServerSocket.bind(new InetSocketAddress(mPort));
                    //Wait in a loop (listener) a connection by a client to initialize the socket
                    while (!Thread.currentThread().isInterrupted()) {

                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Connected.");
                        //When a client request connection, the is accepted
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                            Log.d(String.valueOf(address), String.valueOf(port));
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();

                }
            }
        }
    }

    //When a connection by a client is request, the chat connection is open

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            //Create the sent thread and stabilize connection
            Log.d(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        // Function that sent de message and can be access by any calls

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {

                        setSocket(new Socket(mAddress, PORT));
                        Log.d(String.valueOf(mAddress), String.valueOf(PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");



                    } else {

                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }
                    //Connection succesfully and create the receiving thread
                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                    updateMessages("No se pudo establecer conexion con el pager " + mPort%2000 );


                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                    updateMessages("No se pudo establecer conexion con el pager " + mPort%2000 );


                }

                while (true) {
                    try {
                        //ALways keep the buffer clean, flush the messages
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        //class that always read the buffer like listener

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();

                        if (messageStr != null) {
                            //If a message was received from the pager that we sent the alarm
                            //Notify that the alarm was actived succesfully
                            if (Objects.equals(messageStr.substring(0,4), "Hola")){


                                int count = 0;
                                while (count < ((OrderActivity) mActivity).mArrayList.size()) {
                                    OrderObject object = ((OrderActivity) mActivity).mArrayList.get(count);
                                    if (object.getPager() == getSocket().getPort()%2000) {

                                        updateMessages("Alarma del pager " + getSocket().getPort()%2000 + " activada");
                                        ((OrderActivity)mActivity).mArrayList.get(count).setNotify(true);

                                    }
                                    count++;
                                }

                            }
                            else if (Objects.equals(messageStr.substring(0,4), "Chao")){

                                updateMessages("Alarma del pager " + getSocket().getPort()%2000 + " desactivada");

                            }
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);

                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!");

                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {

                    Log.e(CLIENT_TAG, "Server loop error: ", e);


                }
            }
        }

        //Teardown the server socket
        public void tearDown() {
            try {
                if (getSocket()!=null) {
                    getSocket().close();
                    //((OrderActivity)mActivity).DeleteOrderToArrayList(((OrderActivity)mActivity).mArrayList.size()-1);
                }
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }

        //Create a PrintWriter buffer and flush the message that is in the buffer

        public void  sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }

                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(getSocket().getOutputStream())), true);
                    out.println(msg);
                    out.flush();
                    //updateMessages(msg, true);

            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + msg);
        }




    }
}
