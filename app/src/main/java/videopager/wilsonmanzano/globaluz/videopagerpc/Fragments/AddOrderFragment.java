package videopager.wilsonmanzano.globaluz.videopagerpc.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.util.Objects;

import videopager.wilsonmanzano.globaluz.videopagerpc.Activity.OrderActivity;
import videopager.wilsonmanzano.globaluz.videopagerpc.R;
import videopager.wilsonmanzano.globaluz.videopagerpc.shared.object.OrderObject;

/**
 * Created by ${User} on 4/05/2017.
 */
 //This class is the fragment that adds the orders
public class AddOrderFragment extends DialogFragment {


    private int mOrder;
    private int mPager;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the layout inflater
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View inflator = layoutInflater.inflate(R.layout.add_order_fragment, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final EditText editOrder = (EditText) inflator.findViewById(R.id.edit_order);

        final EditText editNumber = (EditText) inflator.findViewById(R.id.edit_pager_number);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflator)
                // Add action buttons
                .setPositiveButton(R.string.añadir, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if ((!Objects.equals(editNumber.getText().toString(), ""))
                                && (!Objects.equals(editOrder.getText().toString(), ""))) {

                            boolean mPagerAvaliable = true;
                            boolean mOrderAvaible = true;
                            int count = 0;

                            while (count < ((OrderActivity) getActivity()).mArrayList.size()) {
                                OrderObject object = ((OrderActivity) getActivity()).mArrayList.get(count);
                                if (object.getPager() == Integer.parseInt(editNumber.getText().toString())) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Ya este Pager esta asignado"
                                            , Toast.LENGTH_SHORT).show();
                                    mPagerAvaliable = false;


                                }
                                if (object.getOrder() == Integer.parseInt(editOrder.getText().toString())) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Ya esta Orden esta asignada"
                                            , Toast.LENGTH_SHORT).show();
                                    mOrderAvaible = false;


                                }
                                count++;
                            }


                            if (mPagerAvaliable && mOrderAvaible) {


                                mOrder = Integer.parseInt(editOrder.getText().toString());
                                mPager = Integer.parseInt(editNumber.getText().toString());
                                if (mPager > 200) {

                                    Toast.makeText(getActivity().getApplicationContext(), "Pager no existe", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                OrderObject orderObject = null;
                                try {
                                    orderObject = new OrderObject(mOrder, mPager, getActivity());
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }

                                ((OrderActivity) getActivity()).AddOrderToArrayList(orderObject);
                                AddOrderFragment.this.getDialog().dismiss();
                            }

                        }

                        else {
                            Toast.makeText(getActivity().getApplicationContext(), "Campos vacios, intentelo nuevamente", Toast.LENGTH_SHORT).show();
                            AddOrderFragment.this.getDialog().dismiss();
                        }


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddOrderFragment.this.getDialog().cancel();


                        AddOrderFragment.this.getDialog().dismiss();
                        Log.i("AddOrderFragment", "cancel");
                    }
                });

        return builder.create();
    }


}
