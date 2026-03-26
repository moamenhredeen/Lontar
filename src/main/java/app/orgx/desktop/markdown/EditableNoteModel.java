package app.orgx.desktop.markdown;

import jfx.incubator.scene.control.richtext.model.RichTextModel;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

/**
 * Extends RichTextModel to expose protected insert methods for
 * programmatic model building.
 */
public class EditableNoteModel extends RichTextModel {

    public void appendText(String text, StyleAttributeMap attrs) {
        int lastPara = size() - 1;
        int offset = getParagraphLength(lastPara);
        insertTextSegment(lastPara, offset, text, attrs);
    }

    public void appendLineBreak() {
        int lastPara = size() - 1;
        int offset = getParagraphLength(lastPara);
        insertLineBreak(lastPara, offset);
    }
}
