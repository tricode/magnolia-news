/*
 *      Tricode News module
 *      Is a News app for Magnolia CMS.
 *      Copyright (C) 2015  Tricode Business Integrators B.V.
 *
 * 	  This program is free software: you can redistribute it and/or modify
 *		  it under the terms of the GNU General Public License as published by
 *		  the Free Software Foundation, either version 3 of the License, or
 *		  (at your option) any later version.
 *
 *		  This program is distributed in the hope that it will be useful,
 *		  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *		  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *		  GNU General Public License for more details.
 *
 *		  You should have received a copy of the GNU General Public License
 *		  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tricode.magnolia.news.column;

import com.vaadin.ui.Table;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import nl.tricode.magnolia.news.NewsNodeTypes;

import javax.jcr.Item;

public class NewsTitleColumnFormatter extends AbstractColumnFormatter<PropertyColumnDefinition> {

    public NewsTitleColumnFormatter(PropertyColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(final Table source, final Object itemId, final Object columnId) {
        final Item jcrItem = getJcrItem(source, itemId);

        return GenericColumnFormatter.generateCellHelper(jcrItem, NewsNodeTypes.News.NAME, NewsNodeTypes.News.PROPERTY_TITLE);
    }
}