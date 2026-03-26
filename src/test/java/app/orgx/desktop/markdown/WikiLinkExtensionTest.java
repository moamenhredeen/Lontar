package app.orgx.desktop.markdown;

import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WikiLinkExtensionTest {
    private Parser parser;

    @BeforeEach
    void setUp() {
        parser = Parser.builder().extensions(List.of(WikiLinkExtension.create())).build();
    }

    @Test
    void parsesSimpleWikiLink() {
        var doc = parser.parse("Check [[My Note]] for details");
        var para = (Paragraph) doc.getFirstChild();
        var first = para.getFirstChild();
        assertInstanceOf(Text.class, first);
        var link = first.getNext();
        assertInstanceOf(WikiLinkNode.class, link);
        assertEquals("My Note", ((WikiLinkNode) link).getTarget());
        var last = link.getNext();
        assertInstanceOf(Text.class, last);
    }

    @Test
    void parsesMultipleWikiLinks() {
        var doc = parser.parse("See [[Note A]] and [[Note B]]");
        var para = (Paragraph) doc.getFirstChild();
        int count = 0;
        var child = para.getFirstChild();
        while (child != null) {
            if (child instanceof WikiLinkNode) count++;
            child = child.getNext();
        }
        assertEquals(2, count);
    }

    @Test
    void doesNotParseIncompleteWikiLink() {
        var doc = parser.parse("This is [[not closed");
        var para = (Paragraph) doc.getFirstChild();
        var child = para.getFirstChild();
        while (child != null) {
            assertFalse(child instanceof WikiLinkNode, "Expected no WikiLinkNode");
            child = child.getNext();
        }
    }

    @Test
    void parsesWikiLinkAtStartOfLine() {
        var doc = parser.parse("[[First]] word");
        var para = (Paragraph) doc.getFirstChild();
        assertInstanceOf(WikiLinkNode.class, para.getFirstChild());
    }
}
