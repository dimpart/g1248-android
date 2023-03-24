package chat.dim.g1248;

import chat.dim.fsm.AutoMachine;
import chat.dim.fsm.Context;

public class StateMachine extends AutoMachine<StateMachine, StateTransition, PlayerState> implements Context {

    final PlayerOne theOne;

    public StateMachine(PlayerOne one) {
        super();
        theOne = one;
        // init states
        PlayerState.Builder builder = createStateBuilder();
        addState(builder.getDefaultState());
        addState(builder.getSeekingState());
        addState(builder.getWatchingState());
        addState(builder.getPlayingState());
    }

    protected PlayerState.Builder createStateBuilder() {
        return new PlayerState.Builder(new StateTransition.Builder());
    }

    @Override
    protected StateMachine getContext() {
        return this;
    }
}
