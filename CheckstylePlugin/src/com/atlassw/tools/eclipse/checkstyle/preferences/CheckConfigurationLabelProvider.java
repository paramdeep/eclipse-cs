//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.preferences;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;

/**
 * Provides the labels for the audit configuration list display.
 */
public class CheckConfigurationLabelProvider extends LabelProvider implements ITableLabelProvider
{

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        String text = super.getText(element);
        if (element instanceof ICheckConfiguration)
        {
            text = ((ICheckConfiguration) element).getName();
        }

        return text;
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element)
    {
        return getColumnImage(element, 0);
    }

    /**
     * @see ITableLabelProvider#getColumnText(Object, int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        String result = element.toString();
        if (element instanceof ICheckConfiguration)
        {
            ICheckConfiguration cfg = (ICheckConfiguration) element;
            if (columnIndex == 0)
            {
                result = cfg.getName();
            }
            if (columnIndex == 1)
            {
                result = cfg.getLocation();
            }
            if (columnIndex == 2)
            {
                result = cfg.getType().getName();
            }
        }
        return result;
    }

    /**
     * @see ITableLabelProvider#getColumnImage(Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        Image image = null;
        if (element instanceof ICheckConfiguration && columnIndex == 0)
        {
            image = ((ICheckConfiguration) element).getType().getTypeImage();
        }

        return image;
    }
}