package chat.dim.game1248.table;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import chat.dim.cache.game.TableCache;
import chat.dim.g1248.PlayerOne;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.Step;
import chat.dim.game1248.R;
import chat.dim.utils.Log;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TableActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = this::hide;
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = this::onTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_table);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


//        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        if (savedInstanceState == null) {

            SharedDatabase db = SharedDatabase.getInstance();

            // get extra info
            Intent intent = getIntent();
            int tid = intent.getIntExtra("tid", 0);
            int bid = intent.getIntExtra("bid", -1);
            if (bid < 0) {
                // seek empty seat
                bid = 0;
                List<Board> boards = db.getBoards(tid);
                if (boards != null) {
                    for (Board item : boards) {
                        // TODO: or equals current user
                        if (item.getPlayer() == null) {
                            bid = item.getBid();
                            break;
                        }
                    }
                }
            }
            Log.info("[GAME] enter tid: " + tid + ", bid: " + bid);

            boardFragment = new MainBoardFragment(tid, bid % TableCache.MAX_BOARDS_COUNT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_board, boardFragment)
                    .commitNow();
            boardsFragment1 = new BoardFragment(tid, (bid + 1) % TableCache.MAX_BOARDS_COUNT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.board1, boardsFragment1)
                    .commitNow();
            boardsFragment2 = new BoardFragment(tid, (bid + 2) % TableCache.MAX_BOARDS_COUNT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.board2, boardsFragment2)
                    .commitNow();
            boardsFragment3 = new BoardFragment(tid, (bid + 3) % TableCache.MAX_BOARDS_COUNT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.board3, boardsFragment3)
                    .commitNow();

            View trackPad = findViewById(R.id.trackpad);
            trackPad.setOnTouchListener(this::onTouch);

            PlayerOne theOne = PlayerOne.getInstance();
            theOne.table = db.getTable(tid);
            theOne.board = db.getBoard(tid, bid);
            Log.info("playing table: " + theOne.table + ", board: " + theOne.board);
        }

        gestureDetector = new GestureDetector(TableActivity.this, this);
    }

    @Override
    protected void onDestroy() {

        PlayerOne theOne = PlayerOne.getInstance();
        theOne.table = null;
        theOne.board = null;

        super.onDestroy();
    }


    private MainBoardFragment boardFragment = null;
    private BoardFragment boardsFragment1 = null;
    private BoardFragment boardsFragment2 = null;
    private BoardFragment boardsFragment3 = null;

    GestureDetector gestureDetector = null;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

//        // Trigger the initial hide() shortly after the activity has been
//        // created, to briefly hint to the user that UI controls
//        // are available.
//        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private boolean onTouch(View view, MotionEvent motionEvent) {
        if (gestureDetector.onTouchEvent(motionEvent)) {
            return true;
        }
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
        return false;
    }

    //
    //  GestureDetector.OnGestureListener
    //

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {

        Step.Direction direction;

        float offsetX = motionEvent.getX() - motionEvent1.getX();
        float offsetY = motionEvent.getY() - motionEvent1.getY();
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            if (offsetX > 0) {
                direction = Step.Direction.LEFT;
            } else {
                direction = Step.Direction.RIGHT;
            }
        } else {
            if (offsetY > 0) {
                direction = Step.Direction.UP;
            } else {
                direction = Step.Direction.DOWN;
            }
        }

        boardFragment.onSwipe(direction);

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }
}
