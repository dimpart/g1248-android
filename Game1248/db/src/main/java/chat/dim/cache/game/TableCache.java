package chat.dim.cache.game;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import chat.dim.g1248.dbi.TableDBI;
import chat.dim.g1248.model.Board;
import chat.dim.utils.Log;

public class TableCache implements TableDBI {

    public static final int MAX_BOARDS_COUNT = 4;

    // tid => sorted boards
    private final SparseArray<List<Board>> cachedBoards = new SparseArray<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static void fillBoards(int tid, List<Board> boards) {
        int index;
        boolean exists;
        for (index = 0; index < MAX_BOARDS_COUNT && boards.size() < MAX_BOARDS_COUNT; ++index) {
            exists = false;
            for (Board item : boards) {
                if (item.getBid() == index) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                boards.add(index, new Board(tid, index, Board.DEFAULT_SIZE));
            }
        }
    }

    @Override
    public Board getBoard(int tid, int bid) {
        List<Board> boards = getBoards(tid);
        assert boards.size() == MAX_BOARDS_COUNT : "boards error: " + boards;
        Iterator<Board> iterator = boards.iterator();
        Board item;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getTid() == tid && item.getBid() == bid) {
                return item;
            }
        }
        throw new IndexOutOfBoundsException("failed to get board: tid=" + tid + ", bid=" + bid);
    }

    @Override
    public List<Board> getBoards(int tid) {
        List<Board> boards = cachedBoards.get(tid);
        if (boards == null) {
            boards = new ArrayList<>();
            cachedBoards.put(tid, boards);
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            fillBoards(tid, boards);
        } finally {
            writeLock.unlock();
        }
        if (boards.size() != MAX_BOARDS_COUNT) {
            throw new ArrayIndexOutOfBoundsException("boards error: " + boards);
        }
        return boards;
    }

    @Override
    public boolean updateBoard(int tid, Board board) {
        List<Board> array = cachedBoards.get(tid);
        if (array == null) {
            // no records
            array = new ArrayList<>();
            array.add(board);
            cachedBoards.put(tid, array);
            return true;
        }
        int bid = board.getBid();

        int total = array.size();
        int index;
        Board old;
        for (index = 0; index < total; ++index) {
            old = array.get(index);
            if (old.getBid() < bid) {
                // bid not match
                continue;
            } else if (old.getBid() == bid) {
                // old record exists, check time
                if (board.after(old.getTime())) {
                    // ok, update it
                    array.remove(index);
                } else {
                    Log.warning("Board expired: old time=" + old.getTime() + ", new time=" + board.getTime());
                    return false;
                }
            }
            break;
        }
        array.add(index, board);
        return true;
    }
}
