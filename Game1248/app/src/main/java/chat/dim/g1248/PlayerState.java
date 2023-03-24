package chat.dim.g1248;

import chat.dim.fsm.BaseState;
import chat.dim.fsm.State;

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

    public enum Order {
        DEFAULT,  // = 0
        SEEKING,
        PLAYING,
        WATCHING
    }

    public final String name;

    PlayerState(Order stateOrder) {
        super(stateOrder.ordinal());
        name = stateOrder.name();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PlayerState) {
            if (this == other) {
                return true;
            }
            PlayerState state = (PlayerState) other;
            return state.index == index;
        } else if (other instanceof PlayerState.Order) {
            return ((PlayerState.Order) other).ordinal() == index;
        } else {
            return false;
        }
    }
    public boolean equals(Order other) {
        return other.ordinal() == index;
    }

    @Override
    public void onEnter(State<StateMachine, StateTransition> previous, StateMachine machine, long now) {

    }

    @Override
    public void onExit(State<StateMachine, StateTransition> next, StateMachine machine, long now) {

    }

    @Override
    public void onPause(StateMachine ctx, long now) {

    }

    @Override
    public void onResume(StateMachine ctx, long now) {

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
            PlayerState state = new PlayerState(PlayerState.Order.DEFAULT);
            state.addTransition(stb.getDefaultSeekingTransition());
            return state;
        }

        PlayerState getSeekingState() {
            PlayerState state = new PlayerState(PlayerState.Order.SEEKING);
            state.addTransition(stb.getSeekingWatchingTransition());
            return state;
        }

        PlayerState getWatchingState() {
            PlayerState state = new PlayerState(PlayerState.Order.WATCHING);
            state.addTransition(stb.getWatchingSeekingTransition());
            state.addTransition(stb.getWatchingPlayingTransition());
            return state;
        }

        PlayerState getPlayingState() {
            PlayerState state = new PlayerState(PlayerState.Order.PLAYING);
            state.addTransition(stb.getPlayingSeekingTransition());
            state.addTransition(stb.getPlayingWatchingTransition());
            return state;
        }
    }
}
