package com.example.kuba.yourbills.Fragments;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kuba.yourbills.Adapters.BillsListAdapter;
import com.example.kuba.yourbills.Utilities.DBHelper;
import com.example.kuba.yourbills.R;
import com.example.kuba.yourbills.Models.Bill;
import com.example.kuba.yourbills.Unused.NotificationPublisher;
import com.example.kuba.yourbills.Utilities.NotificationScheduler;
import com.example.kuba.yourbills.Utilities.SwipeController;
import com.example.kuba.yourbills.Utilities.SwipeControllerActions;
import com.example.kuba.yourbills.Utilities.UploadWorker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static android.app.Activity.RESULT_OK;

public class FragmentBills extends Fragment {

    private Fragment fragmentNewBill;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DBHelper mydb;
    private Button buttonRefresh;
    private ArrayList<Bill> billsList, billsListToShow;
    private Toolbar toolbarBills;
    private Bill.SortCategory currentSortCategory = Bill.SortCategory.DEADLINE;
    private CheckBox hidePaidCheckBox;

    private LinearLayout filtersLayout;
    private AppCompatSpinner filterSelector;

    private Calendar calendarMonthToDisplay;
    private TextView nameMonthToDisplay;
    private static SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private String monthName;
    private ImageView oneMonthForwardButton, oneMonthBackwardButton;

    private SwipeController swipeController = null;

    private ItemTouchHelper itemTouchHelper;

    private View fragmentBillsView;
    private RecyclerView.SimpleOnItemTouchListener recyclerTouchConsumer;

    public static String FRAGMENT_TAG = "Bills";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bills, container, false);

        fragmentBillsView = view;

        init(view);


        return view;
    }


    private void init(View view){
        mydb = new DBHelper(view.getContext());
        billsList = getBillsFromDatabase();

        initiateToolbar(view);
        initiateMonthDisplay(view);

        recyclerView = view.findViewById(R.id.bills_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerTouchConsumer = new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
                return true;
            }
        };
        swipeController = new SwipeController(getContext(), new SwipeControllerActions() {
            @Override
            public void onRightDeleteClicked(int position) {
                Bill billToRemove = billsListToShow.get(position);
                mydb.removeBill(billToRemove);
                billsListToShow.remove(position);
                billsList.remove(billToRemove);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
                Log.v("usunalem ", Integer.toString(position));
                //super.onRightDeleteClicked(position);
            }

            @Override
            public void onRightPaidClicked(final int position){
                final Bill bill = billsListToShow.get(position);
                bill.setPaid(!bill.isPaid());
                // tutaj wywolanie metody zmiany statusu PAID w bazie
                mydb.updatePaidData(bill);

                for(int i=0; i<billsList.size(); i++){
                    if(billsList.get(i).getDatabaseId() == bill.getDatabaseId())
                        billsList.get(i).setPaid(bill.isPaid());
                }
                mAdapter.notifyDataSetChanged();



                if(bill.isPaid() && hidePaidCheckBox.isChecked()){
                    recyclerView.addOnItemTouchListener(recyclerTouchConsumer);


                    Thread thread = new Thread(){
                        public void run(){
                            try {

                                Thread.sleep(getContext().getResources().getInteger(R.integer.hide_paid_after_time));
                                billsListToShow.remove(position);
                                swipeController.removeButtons();
                                mAdapter.notifyItemRemoved(position);
                                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
                                Thread.sleep(500);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                           // Thread.sleep(2000);
                                            //itemTouchHelper.attachToRecyclerView(recyclerView);
                                            recyclerView.removeOnItemTouchListener(recyclerTouchConsumer);

                                    }
                                });
                            } catch(InterruptedException e){}
                        }
                    };
                    thread.start();




                    //billsListToShow = getMonthBillsList();
                    //mAdapter.notifyDataSetChanged();
                }

                Log.v("Zaplacono ", "rachunek");
            }

            @Override
            public void onLeftClicked(int position) {

                super.onLeftClicked(position);
            }
        });

        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });




        showBillsListFirstTime();

        buttonRefresh = view.findViewById(R.id.button_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //NotificationScheduler notificationScheduler = new NotificationScheduler(getContext());
                //notificationScheduler.scheduleNotificationWorker(billsListToShow.get(0));
                Log.v("maxId: ", Integer.toString(mydb.getMaxIdFromBills()));


            }
        });

    }


    private void initiateToolbar(View view){
        toolbarBills = view.findViewById(R.id.toolbar_bills);
        toolbarBills.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbarBills.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        toolbarBills.inflateMenu(R.menu.menu_bills_toolbar);
        toolbarBills.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.filters){
                    if(filtersLayout.getVisibility() == View.GONE)
                        filtersLayout.setVisibility(View.VISIBLE);
                    else
                        filtersLayout.setVisibility(View.GONE);
                    return true;
                }
                if(item.getItemId() == R.id.add_new_bill){
                    fragmentNewBill = new FragmentNewBill();
                    fragmentNewBill.setTargetFragment(FragmentBills.this, FragmentNewBill.FRAGMENT_CODE);

                    FragmentManager fragmentManager = getFragmentManager();
                    //fragmentManager.popBackStack();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_top_right, R.anim.slide_out_top_right, R.anim.slide_in_top_right, R.anim.slide_out_top_right);

                    fragmentTransaction.add(R.id.fragment_placeholder, fragmentNewBill, FragmentNewBill.FRAGMENT_TAG);
                    fragmentTransaction.addToBackStack(FRAGMENT_TAG);
                    fragmentTransaction.commit();

                    return true;
                }

                return false;
            }
        });

        // FILTERS
        filtersLayout = view.findViewById(R.id.filters_view);
        filtersLayout.animate().setDuration(getContext().getResources().getInteger(R.integer.filters_slide_time)).translationY(filtersLayout.getHeight());
        hidePaidCheckBox = view.findViewById(R.id.paid_checkbox);
        hidePaidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                billsListToShow = getMonthBillsList();
                swipeController.removeButtons();
                showBillsList();
            }
        });


        filterSelector = view.findViewById(R.id.filter_selector);
        String[] filters = new String[]{"Deadline", "Amount", "Title"};
        ArrayAdapter<String> filterSelectorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filters);
        filterSelector.setAdapter(filterSelectorAdapter);
        filterSelector.setAdapter(filterSelectorAdapter);
        filterSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (filterSelector.getSelectedItem().equals("Deadline")) {
                    currentSortCategory = Bill.SortCategory.DEADLINE;
                }
                else if (filterSelector.getSelectedItem().equals("Amount")) {
                    currentSortCategory = Bill.SortCategory.AMOUNT;
                }
                else if (filterSelector.getSelectedItem().equals("Title")) {
                    currentSortCategory = Bill.SortCategory.TITLE;
                }
                swipeController.removeButtons();
                showBillsList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }



    private void initiateMonthDisplay(View view){
        calendarMonthToDisplay = monthDateFormat.getCalendar();
        monthDateFormat.format(new Date());
        nameMonthToDisplay = view.findViewById(R.id.month_to_display);
        monthName = monthDateFormat.format(calendarMonthToDisplay.getTime());
        nameMonthToDisplay.setText(monthName);
        oneMonthForwardButton = view.findViewById(R.id.one_month_forward);
        oneMonthBackwardButton = view.findViewById(R.id.one_month_backward);
        oneMonthForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recyclerView.findViewHolderForAdapterPosition(1).itemView.performClick();
                swipeController.removeButtons();
                setLayoutAnimation(recyclerView, R.anim.recycler_animator_to_left);
                oneMonthForward();

                setLayoutAnimation(recyclerView, R.anim.recycler_animator_right_to_left);
            }
        });
        oneMonthBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeController.removeButtons();
                //setLayoutAnimation(recyclerView, R.anim.recycler_animator_left_to_right);
                oneMonthBackward();
                setLayoutAnimation(recyclerView, R.anim.recycler_animator_left_to_right);

            }
        });
    }


    private ArrayList<Bill> getBillsFromDatabase(){
        return mydb.getBillsList();
    }

    private void showBillsListFirstTime(){
        billsListToShow = getMonthBillsList();
        showBillsList();
    }

    //resets hour to calculate bill's deadline properly
    private void resetTime(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void sortBillsBy(Bill.SortCategory sortCategory){
        if(sortCategory == Bill.SortCategory.DEADLINE)
            Collections.sort(billsListToShow, Bill.billDeadlineComparator);
        else if(sortCategory == Bill.SortCategory.TITLE)
            Collections.sort(billsListToShow, Bill.billTitleComparator);
        else if(sortCategory == Bill.SortCategory.AMOUNT)
            Collections.sort(billsListToShow, Bill.billAmountComparator);
    }

    private void oneMonthForward(){
        calendarMonthToDisplay.add(Calendar.MONTH, 1);
        monthName = monthDateFormat.format(calendarMonthToDisplay.getTime());

        ObjectAnimator rotateUpHide= (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),R.animator.rotate_up_hide_animator);
        ObjectAnimator rotateUpShow= (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),R.animator.rotate_up_show_animator);
        rotateUpHide.setTarget(nameMonthToDisplay);
        rotateUpHide.start();

        Thread thread = new Thread(){
            public void run(){
                try {
                    Thread.sleep(getContext().getResources().getInteger(R.integer.month_name_rotate_time));
                    nameMonthToDisplay.setText(monthName);
                } catch(InterruptedException e){}
            }
        };
        thread.start();

        rotateUpShow.setStartDelay(getContext().getResources().getInteger(R.integer.month_name_rotate_time));
        rotateUpShow.setTarget(nameMonthToDisplay);
        rotateUpShow.start();

        billsListToShow = getMonthBillsList();
        showBillsList();
    }

    private void oneMonthBackward(){
        calendarMonthToDisplay.add(Calendar.MONTH, -1);
        monthName = monthDateFormat.format(calendarMonthToDisplay.getTime());

        ObjectAnimator rotateDownHide= (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),R.animator.rotate_down_hide_animator);
        ObjectAnimator rotateDownShow= (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),R.animator.rotate_down_show_animator);

        rotateDownHide.setTarget(nameMonthToDisplay);
        rotateDownHide.start();

        Thread thread = new Thread(){
            public void run(){
                try {
                    Thread.sleep(getContext().getResources().getInteger(R.integer.month_name_rotate_time));
                    nameMonthToDisplay.setText(monthName);
                } catch(InterruptedException e){}
            }
        };
        thread.start();

        rotateDownShow.setStartDelay(getContext().getResources().getInteger(R.integer.month_name_rotate_time));
        rotateDownShow.setTarget(nameMonthToDisplay);
        rotateDownShow.start();

        billsListToShow = getMonthBillsList();
        showBillsList();
    }

    private ArrayList<Bill> getMonthBillsList(){
        ArrayList<Bill> tempMonthBillList = new ArrayList<>();
        for(int i=0; i<billsList.size(); i++){
            if (billsList.get(i).getMonth().equals(monthName)){
                if(!hidePaidCheckBox.isChecked()|| (hidePaidCheckBox.isChecked() && !billsList.get(i).isPaid()))
                    tempMonthBillList.add(billsList.get(i));
            }
        }
        return tempMonthBillList;
    }

    private void setBillsListToShow(ArrayList<Bill> billsListToShow){
        this.billsListToShow = billsListToShow;
    }

    private void showBillsList(){
        sortBillsBy(currentSortCategory);
        mAdapter = new BillsListAdapter(billsListToShow);
        recyclerView.setAdapter(mAdapter);
    }

    private void setLayoutAnimation(final RecyclerView recyclerView, @AnimRes int animation) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, animation);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode==FragmentNewBill.FRAGMENT_CODE){
                Bill bill = (Bill)data.getSerializableExtra("bill");
                Log.v("znalazlem ", bill.getBillTitle());
                billsList.add(bill);
                billsListToShow = getMonthBillsList();
                showBillsList();
            }
        }
    }




/*
    private void scheduleNotificationWorker(Bill bill){
        if (calculateNotificationDelay(bill) > 0) {
            Data inputData = new Data.Builder().putInt(UploadWorker.NOTIFICATION_ID, 1).build();
            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                    .setInitialDelay(calculateNotificationDelay(bill), TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("notification")
                    .build();
            WorkManager.getInstance(getContext()).enqueue(notificationWork);
        }
        else
            Log.e("notification_error ", "Could't create notification because delay is negative. Probably bill is overdued.");
    }

    private long getCurrentTimeInMillis(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        simpleDateFormat.format(new Date());
        Date date = calendar.getTime();
        return date.getTime();
    }


    private long calculateNotificationDelay(Bill bill){
        long oneDayInMillis = 24*60*60*1000;
        long dayBeforePaymentDate = bill.getBillDate().getTime()-oneDayInMillis;
        long delay = dayBeforePaymentDate-getCurrentTimeInMillis();
        Log.v("today_billDeadline ", Integer.toString(bill.getDaysLeft()));
        Log.v("today_delay ", Long.toString(delay));
        Log.v("today_milis ", Long.toString(getCurrentTimeInMillis()));
        Log.v("today_bill ", Long.toString(bill.getBillDate().getTime()));
        Date date = new Date();
        date.setTime(getCurrentTimeInMillis()-oneDayInMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        calendar.setTime(date);
        Log.v("today_calendar ", calendar.toString());
        return 5000;
    }

    private void setNotificationHourat(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }


    /////////////////////////////////////////////////////////////




    private void scheduleNotification(Notification notification, int delay){
        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 0);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //tutaj pobieram billDate.getTimeInMillis() - oneDayInMillis (billDate mam [chyba] zawsze na 15:00)
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.getDefault());
        Calendar calendar = simpleDateFormat.getCalendar();
        Date date = calendar.getTime();
        long futureInMillis = date.getTime() + delay;
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


    private Notification getNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("My notificatiop")
                .setContentText("Eluwinp")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }
 */

}
