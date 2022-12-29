package chat.dim.g1248;

import chat.dim.fsm.BaseTransition;
import chat.dim.g1248.model.Board;
import chat.dim.protocol.ID;

/**
 *  Player States
 *  ~~~~~~~~~~~~~
 *
 *      +--------------+                +--------------+
 *      |              |                |              |
 *      |  0. Default  | .............> |  1. Seeking  |
 *      |              |                |              |
 *      +--------------+                +--------------+
 *                                          A    A  :
 *                                          :    :  :
 *              ............................:    :  :
 *              :                                :  :
 *              :                                :  V
 *      +--------------+                +--------------+
 *      |              | <............. |              |
 *      |  3. Playing  |                |  2.Watching  |
 *      |              | .............> |              |
 *      +--------------+                +--------------+
 *
 *  Transitions
 *  ~~~~~~~~~~~
 *
 *      0.1 - when local user ID set, change state to 'seeking' tables in the hall;
 *
 *      1.2 - when entered a table, change state to 'watching';
 *
 *      2.1 - when left the table (back to the hall), 'seeking' tables again;
 *      2.3 - when current board is available, change state to 'playing';
 *
 *      3.1 - when left the table (back to the hall), 'seeking' tables again;
 *      3.2 - when click another board that is occupied, 'watching' another player playing.
 */
abstract class StateTransition extends BaseTransition<StateMachine> {

    StateTransition(String target) {
        super(target);
    }

    /**
     *  Transition Builder
     *  ~~~~~~~~~~~~~~~~~~
     */
    static class Builder {

        /**
         *  Default -> Seeking
         *  ~~~~~~~~~~~~~~~~~~
         */
        StateTransition getDefaultSeekingTransition() {
            return new StateTransition(PlayerState.SEEKING) {
                @Override
                public boolean evaluate(StateMachine ctx) {
                    return ctx.theOne.user != null;
                }
            };
        }

        /**
         *  Seeking -> Watching
         *  ~~~~~~~~~~~~~~~~~~~
         */
        StateTransition getSeekingWatchingTransition() {
            return new StateTransition(PlayerState.WATCHING) {
                @Override
                public boolean evaluate(StateMachine ctx) {
                    return ctx.theOne.table != null;
                }
            };
        }

        /**
         *  Watching -> Seeking
         *  ~~~~~~~~~~~~~~~~~~~
         */
        StateTransition getWatchingSeekingTransition() {
            return new StateTransition(PlayerState.SEEKING) {
                @Override
                public boolean evaluate(StateMachine ctx) {
                    return ctx.theOne.table == null;
                }
            };
        }

        /**
         *  Watching -> Playing
         *  ~~~~~~~~~~~~~~~~~~~
         */
        StateTransition getWatchingPlayingTransition() {
            return new StateTransition(PlayerState.PLAYING) {
                @Override
                public boolean evaluate(StateMachine ctx) {
                    Board board = ctx.theOne.board;
                    if (board == null) {
                        return false;
                    }
                    ID current = board.getPlayer();
                    return current == null || ctx.theOne.equals(current);
                }
            };
        }

        /**
         *  Playing -> Seeking
         *  ~~~~~~~~~~~~~~~~~~
         */
        StateTransition getPlayingSeekingTransition() {
            return new StateTransition(PlayerState.SEEKING) {
                @Override
                public boolean evaluate(StateMachine ctx) {
                    return ctx.theOne.table == null;
                }
            };
        }

        /**
         *  Playing -> Watching
         *  ~~~~~~~~~~~~~~~~~~~
         */
        StateTransition getPlayingWatchingTransition() {
            return new StateTransition(PlayerState.WATCHING) {
                @Override
                public boolean evaluate(StateMachine ctx) {
                    Board board = ctx.theOne.board;
                    if (board == null) {
                        return true;
                    }
                    ID current = board.getPlayer();
                    return current != null && !ctx.theOne.equals(current);
                }
            };
        }
    }
}
