package app.orgx.desktop.markdown;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.application.Platform;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownToModelTest {

    private MarkdownToModel converter;

    @BeforeAll
    static void initToolkit() {
        try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    void setUp() {
        converter = new MarkdownToModel();
    }

    @Test
    void convertsPlainText() {
        var result = converter.convert("Hello world");
        assertNotNull(result);
        assertNotNull(result.model());
    }

    @Test
    void extractsWikiLinks() {
        var result = converter.convert("See [[My Note]] and [[Other]]");
        assertEquals(2, result.wikiLinks().size());
        assertTrue(result.wikiLinks().contains("My Note"));
        assertTrue(result.wikiLinks().contains("Other"));
    }

    @Test
    void handlesHeadings() {
        var result = converter.convert("# Heading 1\n\nSome text\n\n## Heading 2");
        assertNotNull(result.model());
        assertTrue(result.headingCount() >= 2);
    }

    @Test
    void handlesEmptyInput() {
        var result = converter.convert("");
        assertNotNull(result.model());
    }

    @Test
    void handlesCodeBlock() {
        var result = converter.convert("```java\nint x = 1;\n```");
        assertNotNull(result.model());
    }
}
