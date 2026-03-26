package app.orgx.desktop.markdown;

import org.commonmark.node.CustomNode;

public class WikiLinkNode extends CustomNode {
    private final String target;

    public WikiLinkNode(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
