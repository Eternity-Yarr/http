package com.wizzardo.http.framework.template;

import java.util.Map;

/**
 * @author: moxa
 * Date: 7/3/13
 */
public abstract class Tag extends RenderableList {

    public Tag(Map<String, String> attrs, Body body, String offset) {
    }

    protected void prepareAttrs(Map<String, String> attrs) {
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            append(" ");
            ViewRenderer.prepare(attr.getKey(), this);
            append("=\"");
            ViewRenderer.prepare(attr.getValue(), this);
            append("\"");
        }
    }
}
