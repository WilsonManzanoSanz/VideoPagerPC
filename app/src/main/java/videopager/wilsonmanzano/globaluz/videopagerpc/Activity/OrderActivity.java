package videopager.wilsonmanzano.globaluz.videopagerpc.Activity;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

import videopager.wilsonmanzano.globaluz.videopagerpc.Background.DownloadVideoManager;
import videopager.wilsonmanzano.globaluz.videopagerpc.DataBase.DataBaseManager;
import videopager.wilsonmanzano.globaluz.videopagerpc.Fragments.AddOrderFragment;
import videopager.wilsonmanzano.globaluz.videopagerpc.R;
import videopager.wilsonmanzano.globaluz.videopagerpc.recycler.RecyclerOrder;
import videopager.wilsonmanzano.globaluz.videopagerpc.shared.object.OrderObject;

public class OrderActivity extends AppCompatActivity implements View.OnClickListener {

    //Arraylist that save the orders
    public ArrayList<OrderObject> mArrayList = new ArrayList<>();
    //Handler that communicate the socket class with the main class
    public Handler mUpdateHandler;

    //My classes
    private RecyclerOrder mRecyclerAdapter;
    private DownloadVideoManager mDownloadVideoManager;
    private DataBaseManager mDataBaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        //Only if the socket connection isn't stabilize
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                if (Objects.equals(message, "No se pudo establecer conexion")) {

                    //Before when the connection doesn't stabilize the order was delete, is better don't delete anything
                    //DeleteOrderToArrayList(mArrayList.size() - 1);
                }
                Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };

        //Views
        FloatingActionButton btnAddOrder = (FloatingActionButton) findViewById(R.id.buttonADD);
        //Listener
        btnAddOrder.setOnClickListener(this);

        //Download manager

        mDownloadVideoManager = new DownloadVideoManager(OrderActivity.this);
        mDownloadVideoManager.registerDownloadReceiver();

        //DataBaseManager
        initializeDataBaseManager();
        getAllOrdersStoredInDataBase();

        //recyclerOrderInitialize (LIST)
        recyclerOrderInitialize();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDownloadVideoManager.unRegisterDownloadReceiver();
        //Maybe i should detele this
        mDataBaseManager.closeDataBaseConnection();
    }

    //Inflate the download button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return true;
    }

    //Menu button listener

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.download) {

            mDownloadVideoManager.processThisDownloadRequest(getString(R.string.video_download_example));

        }
        return super.onOptionsItemSelected(item);
    }

    //Button that display the Fragment that add a new order

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonADD:

                //Display Fragment
                AddOrderFragment addOrderFragment = new AddOrderFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                addOrderFragment.show(fragmentManager, "Add Order Fragment");

                break;
        }
    }

    //Inflate and initialize the order

    private void recyclerOrderInitialize() {
        //Inflate view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerOrder);
        //As list
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //As Grid
        //GridLayoutManager glm = new GridLayoutManager(cxt, 2);
        recyclerView.setLayoutManager(layoutManager);
        //@param hasFixedSize true if mRecyclerAdapter changes cannot affect the size of the RecyclerView.
        recyclerView.setHasFixedSize(true);
        //set Arraylist
        mRecyclerAdapter = new RecyclerOrder(mArrayList, this);
        recyclerView.setAdapter(mRecyclerAdapter);

    }

    public void initializeDataBaseManager() {
        try {
            mDataBaseManager = new DataBaseManager(this);
        } catch (Exception error) {

        }
    }

    //Get all Order sotred in the DataBase

    public void getAllOrdersStoredInDataBase() {


        String queryString = "select order_order,order_pager from orders";
        //String userState = "place/get/all";
        Vector<Object[]> result = mDataBaseManager.executeQuery(queryString);
        if (result.size() > 1) {
            for (int index = 1; index < result.size(); index++) {
                int mOrder = (Integer) result.get(index)[0];
                int mPager = (Integer) result.get(index)[1];
                OrderObject newOrder = null;
                try {
                    newOrder = new OrderObject(mOrder, mPager, this);
                } catch (UnknownHostException e) {
                    Log.e("getAllPlacesStored", e.toString());
                }
                mArrayList.add(newOrder);
            }
        }

    }

    //Add order in the arraylist and the store in the database

    public void AddOrderToArrayList(OrderObject orderObject) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("order_order", orderObject.getOrder());
        contentValues.put("order_pager", orderObject.getPager());
        mDataBaseManager.insertIntoTable("orders", contentValues);
        mArrayList.add(orderObject);
        mRecyclerAdapter.notifyDataSetChanged();

    }

    //Add order in the arraylist and the delete in the database

    public void DeleteOrderToArrayList(int position) {

        if (position > -1 && mArrayList.size() > 0) {
            mDataBaseManager.deleteFromTable("orders", "order_pager = " + mArrayList.get(position).getPager());
            mArrayList.remove(position);
            mRecyclerAdapter.notifyDataSetChanged();
        }


    }





}
