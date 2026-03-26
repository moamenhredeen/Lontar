package app.orgx.desktop.markdown;

import org.commonmark.Extension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.parser.PostProcessor;

import java.util.regex.Pattern;

public class WikiLinkExtension implements Parser.ParserExtension {
    private static final Pattern WIKI_LINK = Pattern.compile("\\[\\[([^\\]]+)]]");

    private WikiLinkExtension() {}

    public static Extension create() {
        return new WikiLinkExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.postProcessor(new WikiLinkPostProcessor());
    }

    private static class WikiLinkPostProcessor implements PostProcessor {
        @Override
        public Node process(Node node) {
            node.accept(new AbstractVisitor() {
                @Override
                public void visit(Text text) {
                    var literal = text.getLiteral();
                    var matcher = WIKI_LINK.matcher(literal);
                    if (!matcher.find()) return;

                    matcher.reset();
                    var lastEnd = 0;
                    Node insertAfter = text;

                    while (matcher.find()) {
                        if (matcher.start() > lastEnd) {
                            var before = new Text(literal.substring(lastEnd, matcher.start()));
                            insertAfter.insertAfter(before);
                            insertAfter = before;
                        }
                        var wikiLink = new WikiLinkNode(matcher.group(1));
                        insertAfter.insertAfter(wikiLink);
                        insertAfter = wikiLink;
                        lastEnd = matcher.end();
                    }
                    if (lastEnd < literal.length()) {
                        var after = new Text(literal.substring(lastEnd));
                        insertAfter.insertAfter(after);
                    }
                    text.unlink();
                }
            });
            return node;
        }
    }
}
