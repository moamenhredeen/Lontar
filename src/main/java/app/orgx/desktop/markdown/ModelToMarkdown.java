package app.orgx.desktop.markdown;

import jfx.incubator.scene.control.richtext.model.StyledTextModel;

public class ModelToMarkdown {

    public String convert(StyledTextModel model) {
        // Since the model contains the full markdown source (including syntax markers),
        // we extract the plain text content which IS the markdown.
        var sb = new StringBuilder();
        var paragraphCount = model.size();
        for (int i = 0; i < paragraphCount; i++) {
            if (i > 0) sb.append("\n");
            sb.append(model.getPlainText(i));
        }
        return sb.toString();
    }
}
