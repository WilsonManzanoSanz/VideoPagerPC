package videopager.wilsonmanzano.globaluz.videopagerpc.recycler;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import videopager.wilsonmanzano.globaluz.videopagerpc.Activity.OrderActivity;
import videopager.wilsonmanzano.globaluz.videopagerpc.R;
import videopager.wilsonmanzano.globaluz.videopagerpc.shared.object.OrderObject;


public class RecyclerOrder extends RecyclerView.Adapter<RecyclerOrder.RecyclerViewHolder>  {

    private ArrayList<OrderObject > arrayList = new ArrayList<>();
    private Activity mAcitivity;

    //Constructor of the Recycler
    public RecyclerOrder(ArrayList<OrderObject> arrayList, Activity activity){
        this.arrayList = arrayList;
        this.mAcitivity = activity;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {

        // View
        final OrderObject orderObject = arrayList.get(position);

        //Set in the view the order and the pager
        String setTextOrder, setTextPager;
        setTextOrder = mAcitivity.getApplicationContext().getString(R.string.order)+ ": " + orderObject.getOrder();
        setTextPager = mAcitivity.getApplicationContext().getString(R.string.pager)+ ": " + orderObject.getPager();
        holder.textOrder.setText(setTextOrder);
        holder.textNumber.setText(setTextPager);

        //Remove Order
        holder.buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                orderObject.SocketClose();
                ((OrderActivity)mAcitivity).DeleteOrderToArrayList(position);


            }
        });


        //Send a PING to Pager
        holder.buttonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    //Send and message
                    orderObject.sentMessage("Hola " + orderObject.getPager());
                    final Handler mHandler = new Handler();
                //And wait 500 miliseconds
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //If the pager answers the message, appear a toast confirm the alarm was actived
                            if (!orderObject.isNotify()) {
                                //if not the videoPC tries again creates a new conenction
                                orderObject.createNewConnection();
                                Handler mHandler2 = new Handler();

                                //Wait a delay to wait that the conecction stabilized
                                mHandler2.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //And send the message again and this should be arrived
                                        orderObject.sentMessage("Hola " + orderObject.getPager());
                                        orderObject.setNotify(false);

                                    }
                                }, 500);
                            }

                        }
                    }, 500);



                               /* if (!orderObject.isNotify()) {
                    orderObject.sentMessage("Hola " + orderObject.getPager());
                    orderObject.setNotify(true);
                    holder.buttonNotify.setImageResource(R.drawable.cancel_50);

                }

                else {

                    orderObject.sentMessage("Chao " + orderObject.getPager());
                    orderObject.setNotify(false);
                    holder.buttonNotify.setImageResource(R.drawable.ok_50);

                }

                */



            }
        });



    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }




    static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView textOrder, textNumber;
        private ImageButton buttonFinish, buttonNotify;

        RecyclerViewHolder(View view){

            super (view);
            textOrder = (TextView) view.findViewById(R.id.tvOrder);
            textNumber = (TextView) view.findViewById(R.id.tvPager);
            buttonFinish = (ImageButton) view.findViewById(R.id.btnDelete);
            buttonNotify = (ImageButton) view.findViewById(R.id.buttonNotify);



        }

    }
}