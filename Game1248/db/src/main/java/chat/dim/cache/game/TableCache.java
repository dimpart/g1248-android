package chat.dim.cache.game;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import chat.dim.g1248.dbi.TableDBI;
import chat.dim.g1248.model.Board;

public class TableCache implements TableDBI {

    // tid => sorted boards
    private final SparseArray<List<Board>> cachedBoards = new SparseArray<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private void fillBoards(List<Board> boards) {
        int index;
        boolean exists;
        for (index = 0; index < 4 && boards.size() < 4; ++index) {
            exists = false;
            for (Board item : boards) {
                if (item.getBid() == index) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                boards.add(index, new Board(index, 4));
            }
        }
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
            fillBoards(boards);
        } finally {
            writeLock.unlock();
        }
        if (boards.size() != 4) {
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
        int oid;
        for (index = 0; index < total; ++index) {
            oid = array.get(index).getBid();
            if (oid < bid) {
                continue;
            } else if (oid == bid) {
                // old record exists, remove it
                array.remove(index);
            }
            break;
        }
        array.add(index, board);
        return true;
    }
}
