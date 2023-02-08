package chat.dim.g1248;

import chat.dim.fsm.BaseState;
import chat.dim.fsm.State;
import chat.dim.network.SessionState;

/**
 *  Player State
 *  ~~~~~~~~~~~~
 *
 *  Defined for indicating player states
 *
 *      DEFAULT  - initialized
 *      SEEKING  - browsing rooms in the hall
 *      WATCHING - watching another player playing the game
 *      PLAYING  - playing game in a room (with board)
 */
public class PlayerState extends BaseState<StateMachine, StateTransition> {

    public static final String DEFAULT  = "default";
    public static final String SEEKING  = "seeking";
    public static final String PLAYING  = "playing";
    public static final String WATCHING = "watching";

    public final String name;

    PlayerState(String stateName) {
        super();
        name = stateName;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof SessionState) {
            return ((SessionState) other).name.equals(name);
        } else if (other instanceof String) {
            return other.equals(name);
        } else {
            return false;
        }
    }
    public boolean equals(String other) {
        return name.equals(other);
    }

    @Override
    public void onEnter(State<StateMachine, StateTransition> previous, StateMachine machine, long now) {

    }

    @Override
    public void onExit(State<StateMachine, StateTransition> next, StateMachine machine, long now) {

    }

    @Override
    public void onPause(StateMachine ctx) {

    }

    @Override
    public void onResume(StateMachine ctx) {

    }

    /**
     *  State Builder
     *  ~~~~~~~~~~~~~
     */
    static class Builder {

        private final StateTransition.Builder stb;

        Builder(StateTransition.Builder builder) {
            super();
            stb = builder;
        }

        PlayerState getDefaultState() {
            PlayerState state = new PlayerState(PlayerState.DEFAULT);
            state.addTransition(stb.getDefaultSeekingTransition());
            return state;
        }

        PlayerState getSeekingState() {
            PlayerState state = new PlayerState(PlayerState.SEEKING);
            state.addTransition(stb.getSeekingWatchingTransition());
            return state;
        }

        PlayerState getWatchingState() {
            PlayerState state = new PlayerState(PlayerState.WATCHING);
            state.addTransition(stb.getWatchingSeekingTransition());
            state.addTransition(stb.getWatchingPlayingTransition());
            return state;
        }

        PlayerState getPlayingState() {
            PlayerState state = new PlayerState(PlayerState.PLAYING);
            state.addTransition(stb.getPlayingSeekingTransition());
            state.addTransition(stb.getPlayingWatchingTransition());
            return state;
        }
    }
}
