package app.orgx.desktop.markdown;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.application.Platform;

import static org.junit.jupiter.api.Assertions.*;

class ModelToMarkdownTest {

    private MarkdownToModel toModel;
    private ModelToMarkdown toMarkdown;

    @BeforeAll
    static void initToolkit() {
        try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    void setUp() {
        toModel = new MarkdownToModel();
        toMarkdown = new ModelToMarkdown();
    }

    @Test
    void roundTripsPlainText() {
        assertRoundTrip("Hello world");
    }

    @Test
    void roundTripsHeading() {
        assertRoundTrip("# Heading 1");
    }

    @Test
    void roundTripsBold() {
        assertRoundTrip("Some **bold** text");
    }

    @Test
    void roundTripsItalic() {
        assertRoundTrip("Some *italic* text");
    }

    @Test
    void roundTripsWikiLink() {
        assertRoundTrip("See [[My Note]] here");
    }

    @Test
    void roundTripsCodeBlock() {
        assertRoundTrip("```java\nint x = 1;\n```");
    }

    @Test
    void roundTripsBulletList() {
        assertRoundTrip("- Item one\n- Item two");
    }

    private void assertRoundTrip(String markdown) {
        var result = toModel.convert(markdown);
        var output = toMarkdown.convert(result.model());
        assertEquals(markdown.strip(), output.strip());
    }
}
