package app.orgx.desktop.vault;

import app.orgx.desktop.model.Note;
import java.util.ArrayList;
import java.util.List;

public class NavigationHistory {
    private final List<Note> stack = new ArrayList<>();
    private int cursor = -1;

    public void push(Note note) {
        while (stack.size() > cursor + 1) { stack.removeLast(); }
        stack.add(note);
        cursor = stack.size() - 1;
    }

    public boolean canGoBack() { return cursor > 0; }
    public boolean canGoForward() { return cursor < stack.size() - 1; }

    public Note back() {
        if (!canGoBack()) return null;
        cursor--;
        return stack.get(cursor);
    }

    public Note forward() {
        if (!canGoForward()) return null;
        cursor++;
        return stack.get(cursor);
    }

    public void clear() { stack.clear(); cursor = -1; }
}
