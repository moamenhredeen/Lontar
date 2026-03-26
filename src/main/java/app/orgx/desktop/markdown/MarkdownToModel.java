package app.orgx.desktop.markdown;

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
        private final EditableNoteModel model = new EditableNoteModel();
        private final Set<String> wikiLinks = new LinkedHashSet<>();
        private int headingCount = 0;
        private final Deque<StyleAttributeMap> styleStack = new ArrayDeque<>();
        private boolean firstBlock = true;

        ModelBuilder() {
            styleStack.push(StyleAttributeMap.EMPTY);
        }

        StyledTextModel build() {
            return model;
        }

        private void addText(String text, StyleAttributeMap style) {
            model.appendText(text, style);
        }

        private void newLine() {
            model.appendLineBreak();
        }

        @Override
        public void visit(Heading heading) {
            if (!firstBlock) newLine();
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

            var prefix = "#".repeat(level) + " ";
            addText(prefix, style);

            visitChildren(heading);
            styleStack.pop();
        }

        @Override
        public void visit(Paragraph node) {
            if (!firstBlock) newLine();
            firstBlock = false;
            visitChildren(node);
        }

        @Override
        public void visit(Text text) {
            addText(text.getLiteral(), currentStyle());
        }

        @Override
        public void visit(Emphasis emphasis) {
            var style = StyleAttributeMap.builder().setItalic(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            addText("*", currentStyle());
            visitChildren(emphasis);
            addText("*", currentStyle());
            styleStack.pop();
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            var style = StyleAttributeMap.builder().setBold(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            addText("**", currentStyle());
            visitChildren(strongEmphasis);
            addText("**", currentStyle());
            styleStack.pop();
        }

        @Override
        public void visit(Code code) {
            var style = StyleAttributeMap.builder()
                    .setFontFamily("Monospace")
                    .build();
            addText("`" + code.getLiteral() + "`", style);
        }

        @Override
        public void visit(FencedCodeBlock codeBlock) {
            if (!firstBlock) newLine();
            firstBlock = false;

            var style = StyleAttributeMap.builder()
                    .setFontFamily("Monospace")
                    .build();
            addText("```" + (codeBlock.getInfo() != null ? codeBlock.getInfo() : ""), style);

            var literal = codeBlock.getLiteral();
            if (literal != null && !literal.isEmpty()) {
                if (literal.endsWith("\n")) {
                    literal = literal.substring(0, literal.length() - 1);
                }
                var lines = literal.split("\n", -1);
                for (var line : lines) {
                    newLine();
                    addText(line, style);
                }
            }

            newLine();
            addText("```", style);
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
            if (!firstBlock) newLine();
            firstBlock = false;
            addText("- ", currentStyle());
            var child = listItem.getFirstChild();
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
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            if (!firstBlock) newLine();
            firstBlock = false;

            var style = StyleAttributeMap.builder().setItalic(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            addText("> ", currentStyle());
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
            if (!firstBlock) newLine();
            firstBlock = false;
            addText("---", currentStyle());
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            newLine();
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            newLine();
        }

        @Override
        public void visit(CustomNode node) {
            if (node instanceof WikiLinkNode wikiLink) {
                wikiLinks.add(wikiLink.getTarget());
                addText("[[" + wikiLink.getTarget() + "]]", currentStyle());
            } else {
                visitChildren(node);
            }
        }

        @Override
        public void visit(Link link) {
            addText("[", currentStyle());
            visitChildren(link);
            addText("](" + link.getDestination() + ")", currentStyle());
        }

        @Override
        public void visit(Image image) {
            var alt = image.getTitle() != null ? image.getTitle() : "";
            addText("![" + alt + "](" + image.getDestination() + ")", currentStyle());
        }

        private StyleAttributeMap currentStyle() {
            return styleStack.peek();
        }

        private StyleAttributeMap mergeStyles(StyleAttributeMap base, StyleAttributeMap overlay) {
            return StyleAttributeMap.builder().merge(base).merge(overlay).build();
        }
    }
}
