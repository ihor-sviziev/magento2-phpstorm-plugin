/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class UiComponentFormFieldsetData {

    private final String label;
    private final String sortOrder;

    /**
     * Fieldset data.
     *
     * @param label String
     * @param sortOrder String
     */
    public UiComponentFormFieldsetData(
            final String label,
            final String sortOrder
    ) {
        this.label = label;
        this.sortOrder = sortOrder;
    }

    public String getLabel() {
        return label;
    }

    public String getSortOrder() {
        return sortOrder;
    }
}
