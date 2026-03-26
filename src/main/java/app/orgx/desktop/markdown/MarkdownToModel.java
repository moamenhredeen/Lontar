package app.orgx.desktop.markdown;

import jfx.incubator.scene.control.richtext.model.SimpleViewOnlyStyledModel;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;

import java.util.*;

public class MarkdownToModel {

    private final Parser parser;

    public MarkdownToModel() {
        List<Extension> extensions = List.of(WikiLinkExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
    }

    public Result convert(String markdown) {
        var document = parser.parse(markdown);
        var builder = new ModelBuilder();
        document.accept(builder);
        return new Result(builder.build(), builder.wikiLinks, builder.headingCount);
    }

    public record Result(StyledTextModel model, Set<String> wikiLinks, int headingCount) {}

    private static class ModelBuilder extends AbstractVisitor {
        private final SimpleViewOnlyStyledModel model = new SimpleViewOnlyStyledModel();
        private final Set<String> wikiLinks = new LinkedHashSet<>();
        private int headingCount = 0;
        private final Deque<StyleAttributeMap> styleStack = new ArrayDeque<>();
        private boolean firstBlock = true;

        ModelBuilder() {
            styleStack.push(StyleAttributeMap.EMPTY);
        }

        StyledTextModel build() {
            // Ensure the model has at least one paragraph — SimpleViewOnlyStyledModel
            // starts with 0 paragraphs, and RichTextArea crashes on empty models.
            if (model.size() == 0) {
                model.addSegment("", StyleAttributeMap.EMPTY);
            }
            return model;
        }

        @Override
        public void visit(Heading heading) {
            if (!firstBlock) model.nl();
            firstBlock = false;

            headingCount++;
            var level = heading.getLevel();
            var fontSize = switch (level) {
                case 1 -> 28.0;
                case 2 -> 24.0;
                case 3 -> 20.0;
                case 4 -> 18.0;
                case 5 -> 16.0;
                default -> 14.0;
            };

            var style = StyleAttributeMap.builder()
                    .setBold(true)
                    .setFontSize(fontSize)
                    .build();
            styleStack.push(style);

            // Add the markdown syntax prefix
            var prefix = "#".repeat(level) + " ";
            model.addSegment(prefix, style);

            visitChildren(heading);
            styleStack.pop();
        }

        @Override
        public void visit(Paragraph node) {
            if (!firstBlock) model.nl();
            firstBlock = false;
            visitChildren(node);
        }

        @Override
        public void visit(Text text) {
            model.addSegment(text.getLiteral(), currentStyle());
        }

        @Override
        public void visit(Emphasis emphasis) {
            var style = StyleAttributeMap.builder().setItalic(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            model.addSegment("*", currentStyle());
            visitChildren(emphasis);
            model.addSegment("*", currentStyle());
            styleStack.pop();
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            var style = StyleAttributeMap.builder().setBold(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            model.addSegment("**", currentStyle());
            visitChildren(strongEmphasis);
            model.addSegment("**", currentStyle());
            styleStack.pop();
        }

        @Override
        public void visit(Code code) {
            var style = StyleAttributeMap.builder()
                    .setFontFamily("Monospace")
                    .build();
            model.addSegment("`" + code.getLiteral() + "`", style);
        }

        @Override
        public void visit(FencedCodeBlock codeBlock) {
            if (!firstBlock) model.nl();
            firstBlock = false;

            var style = StyleAttributeMap.builder()
                    .setFontFamily("Monospace")
                    .build();
            model.addSegment("```" + (codeBlock.getInfo() != null ? codeBlock.getInfo() : ""), style);

            // Add code lines (trim trailing newline from commonmark literal)
            var literal = codeBlock.getLiteral();
            if (literal != null && !literal.isEmpty()) {
                if (literal.endsWith("\n")) {
                    literal = literal.substring(0, literal.length() - 1);
                }
                var lines = literal.split("\n", -1);
                for (var line : lines) {
                    model.nl();
                    model.addSegment(line, style);
                }
            }

            model.nl();
            model.addSegment("```", style);
        }

        @Override
        public void visit(BulletList bulletList) {
            visitChildren(bulletList);
        }

        @Override
        public void visit(OrderedList orderedList) {
            visitChildren(orderedList);
        }

        @Override
        public void visit(ListItem listItem) {
            if (!firstBlock) model.nl();
            firstBlock = false;
            model.addSegment("- ", currentStyle());
            // Visit children but skip the inner paragraph's newline handling
            var child = listItem.getFirstChild();
            while (child != null) {
                if (child instanceof Paragraph) {
                    // Visit paragraph children directly to avoid extra newline
                    var inlineChild = child.getFirstChild();
                    while (inlineChild != null) {
                        inlineChild.accept(this);
                        inlineChild = inlineChild.getNext();
                    }
                } else {
                    child.accept(this);
                }
                child = child.getNext();
            }
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            if (!firstBlock) model.nl();
            firstBlock = false;

            var style = StyleAttributeMap.builder().setItalic(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            model.addSegment("> ", currentStyle());
            // Visit children but skip the inner paragraph's newline handling
            var child = blockQuote.getFirstChild();
            while (child != null) {
                if (child instanceof Paragraph) {
                    var inlineChild = child.getFirstChild();
                    while (inlineChild != null) {
                        inlineChild.accept(this);
                        inlineChild = inlineChild.getNext();
                    }
                } else {
                    child.accept(this);
                }
                child = child.getNext();
            }
            styleStack.pop();
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            if (!firstBlock) model.nl();
            firstBlock = false;
            model.addSegment("---", currentStyle());
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            model.nl();
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            model.nl();
        }

        @Override
        public void visit(CustomNode node) {
            if (node instanceof WikiLinkNode wikiLink) {
                wikiLinks.add(wikiLink.getTarget());
                var style = currentStyle();
                model.addSegment("[[" + wikiLink.getTarget() + "]]", style);
            } else {
                visitChildren(node);
            }
        }

        @Override
        public void visit(Link link) {
            model.addSegment("[", currentStyle());
            visitChildren(link);
            model.addSegment("](" + link.getDestination() + ")", currentStyle());
        }

        @Override
        public void visit(Image image) {
            var alt = image.getTitle() != null ? image.getTitle() : "";
            model.addSegment("![" + alt + "](" + image.getDestination() + ")", currentStyle());
        }

        private StyleAttributeMap currentStyle() {
            return styleStack.peek();
        }

        private StyleAttributeMap mergeStyles(StyleAttributeMap base, StyleAttributeMap overlay) {
            return StyleAttributeMap.builder().merge(base).merge(overlay).build();
        }
    }
}
